# 주문 라이프사이클 · Acceptance Criteria (EARS)

> 01_planning: 요구사항을 검증 가능한 EARS로 정제. 이 명세가 가드레일.

**AC-1** When 주문 생성 요청 시, the system shall CREATED 상태의 주문을 생성하고 order_id를 반환한다.

**AC-2** When total_amount < 1 이면, the system shall 주문을 거부한다 (status=rejected).

**AC-3** When CREATED 주문에 process() 호출 시, the system shall PROCESSING으로 전환한다.

**AC-4** When PROCESSING 주문에 fulfill() 호출 시, the system shall FULFILLED로 전환한다.

**AC-5** When FULFILLED 주문에 cancel() 호출 시, the system shall 거부한다.

**AC-6** When CANCELLED 주문에 어떤 전환 호출 시, the system shall 거부한다.

**AC-7** When 같은 idempotency_key로 재요청 시, the system shall 기존 주문을 반환한다 (중복 생성 없음).

**AC-8(화면)** The order_list 화면은 shall 승인된 디자인 스냅샷과 일치한다 (UI parity).

## 검증 매핑

| AC | 테스트 |
| --- | --- |
| AC-1·AC-2 | `tests/test_order_create.py` |
| AC-3·AC-4 | `tests/test_order_lifecycle.py::test_forward_transitions` |
| AC-5 | `tests/test_order_lifecycle.py::test_cancel_fulfilled_rejected` |
| AC-6 | `tests/test_order_lifecycle.py::test_cancelled_is_terminal` |
| AC-7 | `tests/test_order_create.py::test_order_idempotent` |
| AC-8 | `tests/test_screen_parity.py` |
| 회귀 | `tests/test_regression.py` |
