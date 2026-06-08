FROM python:3.11-slim AS base
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .

# ── 개발계: Flask dev server (hot-reload, bind-mount로 소스 변경 즉시 반영)
FROM base AS dev
ENV FLASK_ENV=development
ENV FLASK_DEBUG=1
CMD ["python", "server/app.py"]

# ── 운영계: Gunicorn (멀티워커, 디버그 비활성)
FROM base AS prod
ENV FLASK_ENV=production
CMD ["gunicorn", "--bind", "0.0.0.0:8000", "--workers", "2", "server.app:app"]
