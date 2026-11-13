# --- Build Stage ---
FROM ghcr.io/astral-sh/uv:python3.13-bookworm-slim AS builder

# Set the working directory for the build process
WORKDIR /app

# Enable bytecode compilation for faster execution
ENV UV_COMPILE_BYTECODE=1

# Copy project configuration files first to leverage Docker caching layers
COPY pyproject.toml uv.lock ./

# Install dependencies strictly according to the lockfile (without the root package itself)
RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync --frozen --no-install-project --no-dev

# --- Runtime Stage ---
FROM python:3.13-slim-bookworm

WORKDIR /app

# Ensure Python outputs logs directly to the console without buffering
ENV PYTHONUNBUFFERED=1

# Copy the pre-installed virtual environment from the builder stage
COPY --from=builder /app/.venv /app/.venv

# Prepend the virtual environment binaries to the system PATH
ENV PATH="/app/.venv/bin:$PATH"

# Copy the application source code and resources into the container
COPY src/ ./src
COPY resources/ ./resources

# Expose the standard Flask port
EXPOSE 5000

# Run the Flask app using Gunicorn for production-ready stability and performance
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "1", "--threads", "4", "--timeout", "120", "src.index:app"]