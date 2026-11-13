@echo off
set FLASK_APP=src/index.py
uv run python -m flask --debug run -h 0.0.0.0