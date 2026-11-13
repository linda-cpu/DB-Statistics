import datetime
import os
import threading
import yaml
from flask import Flask, request, jsonify
from flask_swagger_ui import get_swaggerui_blueprint
from apscheduler.schedulers.background import BackgroundScheduler
import datetime as dt

from src.service.train_service import TrainService
from src.service.predict_service import PredictService

app = Flask(__name__)

BASE_URL = os.environ.get("DB_BASE_URL")
API_KEY = os.environ.get("DB_API_KEY")

SWAGGER_URL = '/swagger'
API_URL = '/swagger/spec'

swaggerui_blueprint = get_swaggerui_blueprint(
    SWAGGER_URL,
    API_URL,
    config={'app_name': "DB Delay Prediction ML-Service"}
)
app.register_blueprint(swaggerui_blueprint, url_prefix=SWAGGER_URL)

@app.route('/swagger/spec')
def swagger_spec():
    """Serves the local yaml specification converted into json format."""
    yaml_path = os.path.join(os.path.dirname(__file__), 'swagger.yaml')
    with open(yaml_path, 'r') as f:
        return jsonify(yaml.safe_load(f))
    
# --------------------------------

predict_service = None
try:
    if BASE_URL and API_KEY:
        predict_service = PredictService(base_url=BASE_URL, api_key=API_KEY)
except Exception as e:
    print(f"⚠️ Prediction endpoint disabled until first model training: {str(e)}")

def execute_scheduled_training() -> None:
    """
    Wrapper function used by the scheduler to invoke the training pipeline.
    Runs completely decoupled from any HTTP request context.
    """
    print("⏰ Scheduled event triggered: Starting daily model update...")
    try:
        train_service = TrainService()
        train_service.train()
    except Exception as e:
        print(f"❌ Scheduled training failed: {str(e)}")


# --- AUTOMATED SCHEDULER SETUP ---
# BackgroundScheduler runs on a separate thread automatically
scheduler = BackgroundScheduler()

# Schedule the training job to run every day at 23:00 (11 PM)
scheduler.add_job(
    func=execute_scheduled_training,
    trigger='cron',
    hour=23,
    minute=0,
    id='daily_train_job'
)

# Start the scheduler when Flask initializes
scheduler.start()
print("📅 Background scheduler active: Training set to run daily at 23:00.")

@app.route("/train", methods=["POST"])
def train() -> tuple[dict[str, str], int]:
    train_service = TrainService()
    training_thread = threading.Thread(target=train_service.train)
    training_thread.start()
    
    return {
        "status": "accepted",
        "message": "Training started in the background."
    }, 202

@app.route("/predict", methods=['POST'])
def predict():
    global predict_service
    if predict_service is None:
        try:
            predict_service = PredictService(base_url=BASE_URL, api_key=API_KEY)
        except Exception as e:
            return jsonify({"error": "Model not ready. Run training first.", "details": str(e)}), 400

    data = request.get_json()
    if not data:
        return jsonify({"error": "Missing JSON payload"}), 400

    # 1. Minimal fields validation
    required_fields = ["eva", "line", "dateTime"]
    missing_fields = [field for field in required_fields if field not in data]
    if missing_fields:
        return jsonify({"error": f"Missing minimal fields for request: {missing_fields}"}), 400

    # 2. Robust DateTime boundary validation using Unix Timestamps
    try:
        # Clean input string and handle UTC 'Z' token
        input_time_str = str(data["dateTime"]).strip().replace("Z", "+00:00")
        request_time = dt.datetime.fromisoformat(input_time_str)
        
        # Convert both to float timestamps (seconds since 1970)
        request_timestamp = request_time.timestamp()
        current_timestamp = dt.datetime.now(dt.timezone.utc).timestamp()

        # Check if the date is in the past
        if request_timestamp < current_timestamp:
            return jsonify({
                "error": "Invalid dateTime",
                "details": "The requested departure time lies in the past. Predictions are only available for future trips."
            }), 400

        # SAFE BUFFER: 15 days + 23 hours in seconds (15 * 24 * 3600 + 23 * 3600)
        # This prevents edge-case timezone shifts from crashing the external weather API.
        max_future_seconds = 1378800

        # Check if the date is too far in the future
        if (request_timestamp - current_timestamp) > max_future_seconds:
            return jsonify({
                "error": "Invalid dateTime",
                "details": "The requested departure time is too far in the future. Weather forecasts are strictly limited to 16 days."
            }), 400

    except Exception as parse_error:
        return jsonify({
            "error": "Invalid dateTime format",
            "details": f"Could not parse UTC datetime string. Please use strict ISO-8601 (e.g., '2026-07-20T14:30:00Z'). Error: {str(parse_error)}"
        }), 400

    # 3. Execution Phase
    try:
        print(f"📡 Fetching features for EVA {data['eva']}, Line {data['line']}...")
        complete_features = predict_service.fetch_prediction_features(
            eva=data["eva"],
            line=data["line"],
            date_time=data["dateTime"]
        )
        predicted_delay = predict_service.predict_delay(complete_features)
        
        return jsonify({
            "status": "success",
            "requested_input": data,
            "predicted_delay_minutes": round(predicted_delay, 2)
        }), 200
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({"error": "An error occurred during prediction", "details": str(e)}), 500