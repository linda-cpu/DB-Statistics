import os
import pickle
import numpy as np
import pandas as pd
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Input
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.losses import Huber
from tensorflow.keras.callbacks import EarlyStopping

from src.service.import_service import ImportService

class TrainService:
    BASE_URL = os.environ.get("DB_BASE_URL")
    API_KEY = os.environ.get("DB_API_KEY")
    CSV_PATH = os.environ.get("DB_CSV_PATH", "resources/bahndaten_training.csv")
    
    MODEL_DIR = "resources/models"
    MODEL_PATH = os.path.join(MODEL_DIR, "delay_model.keras")
    ARTIFACTS_PATH = os.path.join(MODEL_DIR, "training_artifacts.pkl")

    if not BASE_URL or not API_KEY:
        raise RuntimeError("Please set the DB_BASE_URL and DB_API_KEY environment variables.")

    def __init__(self):
        os.makedirs(self.MODEL_DIR, exist_ok=True)

    def train(self) -> None:
        """
        Main training pipeline following the high-sensitivity 'No Strike + M-Estimate' architecture.
        """
        import_service = ImportService(self.BASE_URL, self.API_KEY, self.CSV_PATH)
        import_service.fetch_all_ai_data_resumable()
        
        dataset = self._load_and_clean_dataset()
        
        dataset = self._apply_downsampling(dataset)
        
        train_data, test_data, train_labels, test_labels, artifacts = self._prepare_features_and_split(dataset)
        
        normed_train, normed_test = self._normalize_data(train_data, test_data, artifacts["train_stats"])
        
        model = self._build_neural_network(input_shape=len(normed_train.columns))
        
        early_stop = EarlyStopping(monitor='val_loss', patience=15, restore_best_weights=True)
        
        model.fit(
            normed_train, train_labels,
            epochs=200,
            validation_split=0.2,
            verbose=0,
            batch_size=512,
            callbacks=[early_stop]
        )

        # Unpack only 2 values (loss and mae) instead of 3
        loss, mae = model.evaluate(normed_test, test_labels, verbose=0)
        print(f"-> Testing set Mean Abs Error: {mae:.2f} Minutes")
        
        model.save(self.MODEL_PATH)
        with open(self.ARTIFACTS_PATH, "wb") as f:
            pickle.dump(artifacts, f)
            
        print("✅ Training complete. Model successfully deployed!")

    def _load_and_clean_dataset(self) -> pd.DataFrame:
        column_names = [
            "id", "eva", "line", "minutesOfDay", "dayOfWeek", 
            "schoolHoliday", "publicHoliday", "strike", 
            "avgTemperature", "avgHumidity", "totalRain", 
            "totalSnowfall", "maxWindGusts", "avgWindSpeed", 
            "avgRadiation", "delay"
        ]
        raw_dataset = pd.read_csv(self.CSV_PATH, skiprows=1, names=column_names,
                                  na_values="?", comment='\t', sep=",", 
                                  skipinitialspace=True, low_memory=False)

        dataset = raw_dataset.copy()
        dataset['delay'] = pd.to_numeric(dataset['delay'], errors='coerce')
        if 'id' in dataset.columns:
            dataset.pop('id')
        dataset = dataset.dropna()

        # Enforce integers for boolean values
        bool_cols = ['publicHoliday', 'schoolHoliday']
        for col in bool_cols:
            dataset[col] = dataset[col].astype(int)

        dataset['eva'] = dataset['eva'].astype(str)
        dataset['line'] = dataset['line'].astype(str)
        return dataset[(dataset['delay'] >= -5) & (dataset['delay'] <= 120)]

    def _apply_downsampling(self, dataset: pd.DataFrame) -> pd.DataFrame:
        puenktlich_mask = dataset['delay'] <= 2
        dataset_puenktlich = dataset[puenktlich_mask]
        dataset_verspaetet = dataset[~puenktlich_mask]

        n_verspaetet = len(dataset_verspaetet)
        if len(dataset_puenktlich) > n_verspaetet:
            dataset_puenktlich_downsampled = dataset_puenktlich.sample(n=n_verspaetet, random_state=42)
            return pd.concat([dataset_puenktlich_downsampled, dataset_verspaetet]).sample(frac=1, random_state=42).reset_index(drop=True)
        return dataset

    def _prepare_features_and_split(self, dataset: pd.DataFrame) -> tuple:
        """Applies cyclical time mapping and computes smooth M-Estimate target encodings."""
        # Cyclical transformations
        dataset['sin_time'] = np.sin(2 * np.pi * dataset['minutesOfDay'] / 1440.0)
        dataset['cos_time'] = np.cos(2 * np.pi * dataset['minutesOfDay'] / 1440.0)
        dataset['sin_day'] = np.sin(2 * np.pi * dataset['dayOfWeek'] / 7.0)
        dataset['cos_day'] = np.cos(2 * np.pi * dataset['dayOfWeek'] / 7.0)

        # 80/20 Train-Test-Split
        train_dataset = dataset.sample(frac=0.8, random_state=42).copy()
        test_dataset = dataset.drop(train_dataset.index).copy()

        # --- NATIVE M-ESTIMATE ENCODING (m=10.0) ---
        global_mean = float(train_dataset['delay'].mean())
        m = 10.0

        # Calculate M-Estimate for EVA
        eva_stats = train_dataset.groupby('eva')['delay'].agg(['count', 'mean'])
        target_means_eva = ((eva_stats['count'] * eva_stats['mean'] + m * global_mean) / (eva_stats['count'] + m)).to_dict()

        # Calculate M-Estimate for Line
        line_stats = train_dataset.groupby('line')['delay'].agg(['count', 'mean'])
        target_means_line = ((line_stats['count'] * line_stats['mean'] + m * global_mean) / (line_stats['count'] + m)).to_dict()

        # Map learned encodings
        train_dataset['eva_m_encoded'] = train_dataset['eva'].map(target_means_eva).fillna(global_mean)
        test_dataset['eva_m_encoded'] = test_dataset['eva'].map(target_means_eva).fillna(global_mean)
        
        train_dataset['line_m_encoded'] = train_dataset['line'].map(target_means_line).fillna(global_mean)
        test_dataset['line_m_encoded'] = test_dataset['line'].map(target_means_line).fillna(global_mean)

        # Isolate targets
        train_labels = train_dataset.pop('delay')
        test_labels = test_dataset.pop('delay')

        # CRITICAL: Drop 'strike' along with raw strings to force network sensitivity!
        drop_cols = ['eva', 'line', 'minutesOfDay', 'dayOfWeek', 'strike']
        train_dataset = train_dataset.drop(columns=drop_cols, errors='ignore')
        test_dataset = test_dataset.drop(columns=drop_cols, errors='ignore')

        # Extract stats for normalization
        train_stats = train_dataset.describe().to_dict()

        artifacts = {
            "target_means_eva": target_means_eva,
            "target_means_line": target_means_line,
            "global_mean": global_mean,
            "train_stats": train_stats
        }
        return train_dataset, test_dataset, train_labels, test_labels, artifacts

    def _normalize_data(self, train_df: pd.DataFrame, test_df: pd.DataFrame, train_stats: dict) -> tuple:
        normed_train = train_df.copy()
        normed_test = test_df.copy()
        binary_features = ['publicHoliday', 'schoolHoliday']

        for col in train_df.columns:
            if col in binary_features:
                continue
            mean = train_stats[col]["mean"]
            std = train_stats[col]["std"]
            std = std if std > 0 else 1.0 
            
            normed_train[col] = (train_df[col] - mean) / std
            normed_test[col] = (test_df[col] - mean) / std
        return normed_train, normed_test

    def _build_neural_network(self, input_shape: int) -> Sequential:
        # Deep architecture that unlocked the performance plateau (128-64-32)
        model = Sequential([
            Input(shape=(input_shape,)),
            Dense(128, activation='relu'),
            Dense(64, activation='relu'),
            Dense(32, activation='relu'),
            Dense(1)
        ])
        model.compile(
            loss=Huber(delta=1.0),
            optimizer=Adam(learning_rate=0.001), # Standardized learning rate from ranking script
            metrics=['mean_absolute_error']
        )
        return model