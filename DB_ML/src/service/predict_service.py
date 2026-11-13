import os
import pickle
import numpy as np
import pandas as pd
import requests
import tensorflow as tf

class PredictService:
    MODEL_DIR = "resources/models"
    MODEL_PATH = os.path.join(MODEL_DIR, "delay_model.keras")
    ARTIFACTS_PATH = os.path.join(MODEL_DIR, "training_artifacts.pkl")

    def __init__(self, base_url: str, api_key: str):
        self.base_url = base_url
        self.api_key = api_key
        self.headers = {
            "SERVICE-API-KEY": self.api_key,
            "Accept": "application/json"
        }

        if not os.path.exists(self.MODEL_PATH) or not os.path.exists(self.ARTIFACTS_PATH):
            raise RuntimeError("🚨 Trained model files not found. Please trigger /train first.")
        
        self.model = tf.keras.models.load_model(self.MODEL_PATH)
        with open(self.ARTIFACTS_PATH, "rb") as f:
            self.artifacts = pickle.load(f)
            
        self.target_means_eva = self.artifacts["target_means_eva"]
        self.target_means_line = self.artifacts["target_means_line"]
        self.global_mean = self.artifacts["global_mean"]
        self.train_stats = self.artifacts["train_stats"]

    def fetch_prediction_features(self, eva: str, line: str, date_time: str) -> dict:
        endpoint = f"{self.base_url}/predictionData"
        params = {
            "dateTime": str(date_time).strip(),
            "stationEva": int(eva),
            "line": str(line).strip(),
            "aggHours": 2
        }
        response = requests.get(endpoint, headers=self.headers, params=params)
        if response.status_code != 200:
            raise RuntimeError(f"External API error ({response.status_code}): {response.text}")
        return response.json()

    def predict_delay(self, raw_features: dict) -> float:
        """
        Transforms incoming live features to match the high-sensitivity 
        M-Estimate model configuration (without strike feature).
        """
        df = pd.DataFrame([raw_features])
        
        # 1. Cyclical Feature Mapping via .iloc[0]
        minutes_of_day = pd.to_numeric(df['minutesOfDay']).iloc[0]
        day_of_week = pd.to_numeric(df['dayOfWeek']).iloc[0]

        df['sin_time'] = np.sin(2 * np.pi * minutes_of_day / 1440.0)
        df['cos_time'] = np.cos(2 * np.pi * minutes_of_day / 1440.0)
        df['sin_day'] = np.sin(2 * np.pi * day_of_week / 7.0)
        df['cos_day'] = np.cos(2 * np.pi * day_of_week / 7.0)
        
        # 2. Apply smooth M-Estimate Encodings
        df['eva'] = df['eva'].astype(str)
        df['line'] = df['line'].astype(str)
        df['eva_m_encoded'] = df['eva'].map(self.target_means_eva).fillna(self.global_mean)
        df['line_m_encoded'] = df['line'].map(self.target_means_line).fillna(self.global_mean)
        
        # Format binary flags
        binary_features = ['publicHoliday', 'schoolHoliday']
        for col in binary_features:
            df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0).astype(int)

        # Drop columns matching training blueprint (Notice 'strike' is evicted here!)
        drop_cols = ['eva', 'line', 'minutesOfDay', 'dayOfWeek', 'strike']
        df = df.drop(columns=drop_cols, errors='ignore')
        
        # 3. Z-Score Normalization
        normed_df = df.copy()
        
        # Features that must NOT be normalized (Binary flags and target encodings)
        skip_normalization = ['publicHoliday', 'schoolHoliday', 'eva_m_encoded', 'line_m_encoded']

        for col in df.columns:
            if col in skip_normalization:
                continue  # Keep their original scaled values
                
            # Safe guard: If a column from the API response isn't in our training profile, skip it
            if col not in self.train_stats or "mean" not in self.train_stats[col]:
                continue

            mean = self.train_stats[col]["mean"]
            std = self.train_stats[col]["std"]
            std = std if std > 0 else 1.0
            
            normed_df[col] = (df[col] - mean) / std
            
        # 4. Neural Network Inference
        prediction = self.model.predict(normed_df, verbose=0).flatten()[0]
        return float(prediction)