# -*- coding: utf-8 -*-
"""E2E 브라우저 테스트: order·product 서비스 시나리오 검증.

Playwright로 실제 브라우저를 조작해 AC를 검증합니다.
"""
import json
import sys
import urllib.request

from playwright.sync_api import sync_playwright

ORDER_BASE = "http://localhost:8000"
PRODUCT_BASE = "http://localhost:8001"


def api(method, url, data=None):
    body = json.dumps(data).encode() if data else None
    headers = {"Content-Type": "application/json"} if data else {}
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as r:
            return json.loads(r.read())
    except urllib.error.HTTPError as e:
        return json.loads(e.read())


def run_order_e2e(page):
    print("\n[order E2E]")

    # AC-1: 주문 생성
    r = api("POST", f"{ORDER_BASE}/orders", {"items": ["상품A"], "total_amount": 50000})
    assert r["status"] == "created", f"AC-1 실패: {r}"
    oid = r["order"]["order_id"]
    print(f"  AC-1 PASS: 주문 생성 ({oid})")

    # AC-2: 총액 0원 거부
    r = api("POST", f"{ORDER_BASE}/orders", {"items": [], "total_amount": 0})
    assert r["status"] == "rejected", f"AC-2 실패: {r}"
    print("  AC-2 PASS: 총액 0원 거부")

    # AC-3: CREATED → PROCESSING
    r = api("PATCH", f"{ORDER_BASE}/orders/{oid}/process")
    assert r["order"]["status"] == "PROCESSING", f"AC-3 실패: {r}"
    print("  AC-3 PASS: PROCESSING 전환")

    # AC-4: PROCESSING → FULFILLED
    r = api("PATCH", f"{ORDER_BASE}/orders/{oid}/fulfill")
    assert r["order"]["status"] == "FULFILLED", f"AC-4 실패: {r}"
    print("  AC-4 PASS: FULFILLED 전환")

    # AC-5: FULFILLED 취소 불가
    r = api("PATCH", f"{ORDER_BASE}/orders/{oid}/cancel")
    assert r["status"] == "rejected", f"AC-5 실패: {r}"
    print("  AC-5 PASS: FULFILLED 취소 거부")

    # AC-7: 멱등성
    r1 = api("POST", f"{ORDER_BASE}/orders", {"items": ["X"], "total_amount": 100, "idem_key": "e2e-key-1"})
    r2 = api("POST", f"{ORDER_BASE}/orders", {"items": ["X"], "total_amount": 100, "idem_key": "e2e-key-1"})
    assert r1["order"]["order_id"] == r2["order"]["order_id"], f"AC-7 실패"
    assert r2["replay"] is True
    print("  AC-7 PASS: 멱등성 보장")

    # AC-8: 브라우저에서 order_list.html 렌더링 확인
    page.goto(f"{ORDER_BASE}/order_list.html")
    assert page.locator("h1").inner_text() == "주문 목록"
    assert page.locator("button.btn-create").count() == 1
    print("  AC-8 PASS: order_list 브라우저 렌더링 확인")

    print("  [order] 모든 시나리오 PASS")


def run_product_e2e(page):
    print("\n[product E2E]")

    # AC-1: 상품 생성
    r = api("POST", f"{PRODUCT_BASE}/products", {"name": "노트북", "price": 1500000, "stock_quantity": 10})
    assert r["status"] == "created", f"AC-1 실패: {r}"
    pid = r["product"]["product_id"]
    print(f"  AC-1 PASS: 상품 생성 ({pid})")

    # AC-2: 가격 0원 거부
    r = api("POST", f"{PRODUCT_BASE}/products", {"name": "X", "price": 0})
    assert r["status"] == "rejected", f"AC-2 실패: {r}"
    print("  AC-2 PASS: 가격 0원 거부")

    # AC-3: 재고 추가
    r = api("PATCH", f"{PRODUCT_BASE}/products/{pid}/stock/add", {"qty": 5})
    assert r["stock_quantity"] == 15, f"AC-3 실패: {r}"
    print("  AC-3 PASS: 재고 추가 (10→15)")

    # AC-4: 재고 차감
    r = api("PATCH", f"{PRODUCT_BASE}/products/{pid}/stock/reduce", {"qty": 3})
    assert r["stock_quantity"] == 12, f"AC-4 실패: {r}"
    print("  AC-4 PASS: 재고 차감 (15→12)")

    # AC-5: 아카이브
    r = api("PATCH", f"{PRODUCT_BASE}/products/{pid}/archive")
    assert r["status"] == "ok", f"AC-5 실패: {r}"
    print("  AC-5 PASS: 아카이브 전환")

    # AC-6: ARCHIVED 재고 변경 거부
    r = api("PATCH", f"{PRODUCT_BASE}/products/{pid}/stock/add", {"qty": 1})
    assert r["status"] == "rejected", f"AC-6 실패: {r}"
    print("  AC-6 PASS: ARCHIVED 재고 변경 거부")

    # AC-7: 멱등성
    r1 = api("POST", f"{PRODUCT_BASE}/products", {"name": "Z", "price": 100, "idem_key": "e2e-p-1"})
    r2 = api("POST", f"{PRODUCT_BASE}/products", {"name": "Z", "price": 100, "idem_key": "e2e-p-1"})
    assert r1["product"]["product_id"] == r2["product"]["product_id"]
    assert r2["replay"] is True
    print("  AC-7 PASS: 멱등성 보장")

    # AC-8: 브라우저에서 product_list.html 렌더링 확인
    page.goto(f"{PRODUCT_BASE}/product_list.html")
    assert page.locator("h1").inner_text() == "상품 목록"
    assert page.locator("button.btn-create").count() == 1
    print("  AC-8 PASS: product_list 브라우저 렌더링 확인")

    print("  [product] 모든 시나리오 PASS")


def main():
    with sync_playwright() as pw:
        browser = pw.chromium.launch(headless=False)  # headless=False: 브라우저 화면 표시
        page = browser.new_page()
        try:
            run_order_e2e(page)
            run_product_e2e(page)
            print("\n[E2E] order·product 전체 PASS")
        finally:
            browser.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
