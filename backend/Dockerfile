FROM python:3.11-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Cloud Run sets PORT env variable (default 8080)
EXPOSE 8080

# Use PORT from environment (Cloud Run sets this to 8080)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
