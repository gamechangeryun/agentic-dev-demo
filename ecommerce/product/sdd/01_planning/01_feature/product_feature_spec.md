# 상품 카탈로그 · Acceptance Criteria (EARS)

> 01_planning: 요구사항을 검증 가능한 EARS로 정제. 이 명세가 가드레일.

**AC-1** When 상품 생성 요청 시, the system shall ACTIVE 상태의 상품을 생성하고 product_id를 반환한다.

**AC-2** When price <= 0 이면, the system shall 상품 생성을 거부한다 (status=rejected).

**AC-3** When ACTIVE 상품에 add_stock(qty) 호출 시, the system shall stock_quantity를 qty만큼 증가시킨다.

**AC-4** When reduce_stock(qty) 요청이고 qty > stock_quantity 이면, the system shall 거부한다 (insufficient_stock).

**AC-5** When ACTIVE 상품에 archive() 호출 시, the system shall 상태를 ARCHIVED로 전환한다.

**AC-6** When ARCHIVED 상품에 재고 변경 호출 시, the system shall 거부한다.

**AC-7** When 같은 idempotency_key로 재요청 시, the system shall 기존 상품을 반환한다 (중복 생성 없음).

**AC-8(화면)** The product_list 화면은 shall 승인된 디자인 스냅샷과 일치한다 (UI parity).

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1·AC-2 | `tests/test_product_create.py` |
| AC-3·AC-4 | `tests/test_product_stock.py` |
| AC-5·AC-6 | `tests/test_product_stock.py::test_archived_blocks_stock` |
| AC-7 | `tests/test_product_create.py::test_product_idempotent` |
| AC-8 | `tests/test_screen_parity.py` |
| 회귀 | `tests/test_regression.py` |
