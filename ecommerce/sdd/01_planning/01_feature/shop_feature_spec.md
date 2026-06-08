# 01_planning · 이커머스 쇼핑 EARS 명세

> 요구사항 원문(`00_sources/02_requirements/ecommerce-shop.md`)을 EARS 패턴으로
> 검증 가능한 AC 로 옮깁니다. 각 AC 는 테스트 한 케이스와 1:1로 매핑됩니다.

## EARS 패턴 표기

- **Ubiquitous**: 시스템은 항상 ~한다.
- **Event-driven**: ~할 때, 시스템은 ~한다.
- **State-driven**: ~인 동안, 시스템은 ~한다.
- **Unwanted**: 만약 ~하면, 시스템은 ~한다.

## catalog · 상품

| AC | EARS | 검증 |
| --- | --- | --- |
| AC-C1 | 상품 등록 요청이 올 때, 시스템은 ACTIVE 상태의 상품을 생성한다. | ProductTest, E2E-1 |
| AC-C2 | 만약 가격이 0원 이하이면, 시스템은 생성을 INVALID_PRICE 로 거부한다. | ProductTest.rejectsNonPositivePrice |
| AC-C3 | ACTIVE 인 동안, 시스템은 재고를 더하거나 뺄 수 있다. | ProductTest.addAndReduceStock |
| AC-C4 | 만약 차감 수량이 보유 재고를 넘으면, 시스템은 INSUFFICIENT_STOCK 으로 거부한다. | ProductTest.reduceBeyondStockRejected |
| AC-C5 | 목록 조회 요청이 올 때, 시스템은 이름 검색·상태 필터·페이징을 적용해 돌려준다. | E2E-5 |
| AC-C6 | ARCHIVED 인 동안, 시스템은 모든 재고 변경을 거부한다. | ProductTest.archivedBlocksStockChanges |
| AC-C7 | 동일 멱등 키 재요청 시, 시스템은 기존 상품을 반환한다. | E2E-6 |

## inventory · 재고 예약

| AC | EARS | 검증 |
| --- | --- | --- |
| AC-I1 | 예약 요청이 올 때, 시스템은 가용분을 묶고 물리 재고는 유지한다. | InventoryServiceTest.reserveHoldsAvailability |
| AC-I2 | 확정 요청이 올 때, 시스템은 물리 재고를 실제로 차감한다. | InventoryServiceTest.confirmReducesPhysicalStock |
| AC-I3 | 해제 요청이 올 때, 시스템은 예약을 풀어 가용분을 되돌린다. | InventoryServiceTest.releaseRestoresAvailability |
| AC-I4 | 만약 예약 합계가 가용분을 넘으면, 시스템은 뒤 예약을 거부한다. | InventoryServiceTest.preventsOversell, E2E-4 |
| AC-I5 | 동시 예약이 몰려도, 시스템은 물리 재고를 초과 판매하지 않는다. | InventoryServiceTest.concurrentReservationsNeverOversell |

## cart · 장바구니

| AC | EARS | 검증 |
| --- | --- | --- |
| AC-T1 | 담기 요청이 올 때, 시스템은 같은 상품의 수량을 합친다. | CartController, E2E-1 |
| AC-T2 | 수량을 0으로 바꿀 때, 시스템은 해당 항목을 제거한다. | Cart.updateQty |
| AC-T3 | 만약 ARCHIVED 상품을 담으면, 시스템은 PRODUCT_ARCHIVED 로 거부한다. | E2E-7 |

## checkout · ordering · 주문

| AC | EARS | 검증 |
| --- | --- | --- |
| AC-O1 | 체크아웃 요청이 올 때, 시스템은 각 줄을 예약하고 주문을 CREATED 로 생성한다. | E2E-1 |
| AC-O2 | 만약 한 줄이라도 재고가 부족하면, 시스템은 잡은 예약을 모두 풀고 거부한다. | E2E-4 |
| AC-O3 | 만약 총액이 0원 이하이면, 시스템은 INVALID_AMOUNT 로 거부한다. | OrderTest.rejectsZeroAmount |
| AC-O4 | 결제 완료 시, 시스템은 주문을 PAID 로 전환한다. | OrderTest.forwardTransitions, E2E-1 |
| AC-O5 | 만약 결제 전에 이행하려 하면, 시스템은 PAYMENT_REQUIRED 로 거부한다. | OrderTest.cannotFulfillBeforePaid, E2E-8 |
| AC-O6 | 만약 FULFILLED 주문을 취소하려 하면, 시스템은 거부한다. | OrderTest.cannotCancelFulfilled |
| AC-O7 | 동일 멱등 키 재요청 시, 시스템은 기존 주문을 반환한다. | E2E-6 |

## payment · 결제·환불

| AC | EARS | 검증 |
| --- | --- | --- |
| AC-P1 | 결제 승인 시, 시스템은 주문을 PAID 로 바꾸고 예약을 확정한다. | E2E-1 |
| AC-P2 | 만약 결제가 거절되면, 시스템은 402 로 거부하고 주문을 CREATED 로 유지한다. | E2E-9 |
| AC-P3 | 동일 멱등 키 재요청 시, 시스템은 기존 결제를 반환한다. | E2E-6 |
| AC-P4 | 결제된 주문 취소 시, 시스템은 결제를 환불하고 재고를 복원한다. | E2E-3 |
| AC-P5 | 만약 캡처되지 않은 결제를 환불하려 하면, 시스템은 REFUND_NOT_ALLOWED 로 거부한다. | Payment.refund |
