import os
import time
import pandas as pd
import requests

class ImportService:
    COLUMNS_ORDER = [
        "id", "eva", "line", "minutesOfDay", "dayOfWeek", 
        "schoolHoliday", "publicHoliday", "strike", 
        "avgTemperature", "avgHumidity", "totalRain", 
        "totalSnowfall", "maxWindGusts", "avgWindSpeed", 
        "avgRadiation", "delay"
    ]

    def __init__(self, base_url: str, api_key: str, output_path: str, page_size: int = 2000):
        self.base_url = base_url
        self.api_key = api_key
        self.output_path = output_path
        self.page_size = page_size
        self.headers = {
            "SERVICE-API-KEY": self.api_key,
            "Accept": "application/json"
        }

    def _determine_start_page(self) -> tuple[int, bool]:
        # Checks for existing data and determines the next page to fetch.
        if os.path.exists(self.output_path):
            try:
                existing_df = pd.read_csv(self.output_path, usecols=['id'])
                existing_rows = len(existing_df)
                current_page = existing_rows // self.page_size
                print(f"Existing data found with {existing_rows} rows. Set current page to {current_page}.")
                return current_page, False
            except Exception:
                print("CSV-Document not ok. Start from page 0.")
        
        print("Start from page 0.")
        return 0, True

    def fetch_all_ai_data_resumable(self, start_date: str = "2025-12-18T00:00:00Z", weather_hours: int = 5) -> None:
        # Load Trip data page by page and catch server errors.
        current_page, is_first_page = self._determine_start_page()
        consecutive_errors = 0

        while True:
            params = {
                "from": start_date,
                "page": current_page,
                "size": self.page_size,
                "weatherHours": 2
            }

            # HTTP Request with Retry-Logic
            response = self._execute_request_with_retry(params, current_page)
            if response is None:
                current_page += 1
                consecutive_errors += 1
                if consecutive_errors > 5:
                    print("\nToo many consecutive errors. Exiting...")
                    break
                continue

            consecutive_errors = 0
            
            try:
                data = response.json()
                records = data.get("content", [])

                if not records:
                    print(f"\nAll data fetched. Exiting at Page {current_page}.")
                    break

                # Data processing via Pandas
                self._process_and_save_chunk(records, is_first_page)
                is_first_page = False

                current_page += 1
                time.sleep(0.1)

            except Exception as e:
                print(f"\nError at Page {current_page}: {str(e)}")
                current_page += 1

    def _execute_request_with_retry(self, params: dict, current_page: int) -> requests.Response | None:
        # HTTP Request with Retry-Logic
        for attempt in range(3):
            try:
                endpoint = f"{self.base_url}/aiData"
                response = requests.get(endpoint, headers=self.headers, params=params)
                if response.status_code == 200:
                    return response
                elif response.status_code == 500:
                    print(f"\nServererror at Page {current_page}. Pause...")
                    time.sleep(2)
            except requests.exceptions.RequestException:
                time.sleep(2)
        
        print(f"\n Page {current_page} request failed 3 times. Skip Page!")
        return None

    def _process_and_save_chunk(self, records: list, is_first_page: bool) -> None:
        # Clear Data chunk and write to CSV
        df = pd.DataFrame(records)
        df = df.reindex(columns=self.COLUMNS_ORDER)
        df = df.dropna()

        if is_first_page:
            df.to_csv(self.output_path, index=False, mode='w', encoding='utf-8')
        else:
            df.to_csv(self.output_path, index=False, mode='a', header=False, encoding='utf-8')