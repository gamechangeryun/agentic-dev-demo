# 상품 카탈로그 · todos + 실행 계획

- Owner: product-team · Status: planned
- canonical: sdd/01_planning/01_feature/product_feature_spec.md

---

## Scope

상품 생성·재고 관리(add/reduce)·아카이브·멱등·화면 parity

---

## Acceptance Criteria

| Code  | 조건 (EARS) | 테스트 |
| ----- | ----------- | ------ |
| AC-1  | 상품 생성 요청 시 ACTIVE 상태 상품 반환 | `test_product_create.py` |
| AC-2  | price <= 0 이면 거부 | `test_product_create.py::test_create_rejects_zero_price` |
| AC-3  | add_stock(qty) → stock_quantity 증가 | `test_product_stock.py::test_add_stock` |
| AC-4  | reduce_stock(qty > stock) → insufficient_stock 거부 | `test_product_stock.py::test_reduce_insufficient` |
| AC-5  | archive() → ARCHIVED 전환 | `test_product_stock.py::test_archive` |
| AC-6  | ARCHIVED 상품 재고 변경 거부 | `test_product_stock.py::test_archived_blocks_stock` |
| AC-7  | 동일 idempotency_key 재요청 시 기존 상품 반환 | `test_product_create.py::test_product_idempotent` |
| AC-8  | product_list 화면이 디자인 스냅샷과 일치 | `test_screen_parity.py` |
| AC-R  | auth·order 서비스 회귀 없음 | `test_regression.py` |

---

## Execution Checklist

### 1. 상품 도메인 모델 (`server/contexts/product/product.py`)
- [ ] `Product` dataclass — `product_id, name, price, stock_quantity, status, idempotency_key`
- [ ] `ProductResult` dataclass — `status, reason, product`
- [ ] `StockResult` dataclass — `status, reason, stock_quantity`
- [ ] `ProductService` — in-memory store (`products: dict`)

### 2. 상품 생성 (`server/contexts/product/product.py`)
- [ ] `create(name, price, stock_quantity, *, idem_key)` — 유효성 검사, ACTIVE 생성
- [ ] price <= 0 → `ProductResult("rejected", "invalid_price")`
- [ ] 멱등: 동일 idem_key 재요청 시 기존 상품 반환 + `replay=True`

### 3. 재고 관리 (`server/contexts/product/product.py`)
- [ ] `add_stock(product_id, qty)` — stock_quantity += qty
- [ ] `reduce_stock(product_id, qty)` — 부족 시 `StockResult("rejected", "insufficient_stock")`
- [ ] `archive(product_id)` — status → ARCHIVED
- [ ] ARCHIVED 상품 재고 변경 → `StockResult("rejected", "product_archived")`

### 4. 화면 parity (`sdd/04_verify/10_test/ui_parity/product_list.html`)
- [ ] 디자인 스냅샷(`product_list.html`) 기준으로 렌더링 확인
- [ ] `run_ui_parity.py` 구현 후 실행 — diff 0 통과

### 5. 테스트 (`tests/`)
- [ ] `test_product_create.py` — 생성·유효성·멱등 케이스 전체 통과
- [ ] `test_product_stock.py` — 재고·아카이브 케이스 전체 통과
- [ ] `test_screen_parity.py` — UI parity 통과
- [ ] `test_regression.py` — auth·order 회귀 없음 확인

### 6. DEV 게이트
- [ ] 전체 테스트 스위트 green
- [ ] `proof/run_proof.py` exit 0 통과
- [ ] 스키마 drift 0 확인

---

## Latest Verification

- proof: 미실행
