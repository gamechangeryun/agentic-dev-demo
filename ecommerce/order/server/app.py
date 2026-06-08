# -*- coding: utf-8 -*-
"""Flask 진입점 — 10강 실습: 주문 API 라우트를 구현하세요."""
from flask import Flask

app = Flask(__name__)

# TODO: OrderService 인스턴스 생성
# TODO: GET  /orders
# TODO: POST /orders
# TODO: PATCH /orders/<id>/process
# TODO: PATCH /orders/<id>/fulfill
# TODO: PATCH /orders/<id>/cancel

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=True)
