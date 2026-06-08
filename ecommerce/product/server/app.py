# -*- coding: utf-8 -*-
"""Flask 진입점 — 10강 실습: 상품 API 라우트를 구현하세요."""
from flask import Flask

app = Flask(__name__)

# TODO: ProductService 인스턴스 생성
# TODO: GET  /products
# TODO: POST /products
# TODO: PATCH /products/<id>/stock/add
# TODO: PATCH /products/<id>/stock/reduce
# TODO: PATCH /products/<id>/archive

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=True)
