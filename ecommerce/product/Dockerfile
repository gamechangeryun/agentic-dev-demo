FROM python:3.11-slim AS base
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .

FROM base AS dev
ENV FLASK_ENV=development
ENV FLASK_DEBUG=1
CMD ["python", "server/app.py"]

FROM base AS prod
ENV FLASK_ENV=production
CMD ["gunicorn", "--bind", "0.0.0.0:8000", "--workers", "2", "server.app:app"]
