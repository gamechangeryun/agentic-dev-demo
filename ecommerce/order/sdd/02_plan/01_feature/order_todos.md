# 주문 라이프사이클 · todos + 실행 계획

- Owner: order-team · Status: planned
- canonical: sdd/01_planning/01_feature/order_feature_spec.md

---

## Scope

주문 생성·상태 전환(CREATED→PROCESSING→FULFILLED/CANCELLED)·멱등·화면 parity

---

## Acceptance Criteria

| Code  | 조건 (EARS) | 테스트 |
| ----- | ----------- | ------ |
| AC-1  | 주문 생성 요청 시 CREATED 상태 주문 반환 | `test_order_create.py` |
| AC-2  | total_amount < 1 이면 거부 | `test_order_create.py::test_create_rejects_zero_amount` |
| AC-3  | CREATED → process() → PROCESSING | `test_order_lifecycle.py::test_forward_transitions` |
| AC-4  | PROCESSING → fulfill() → FULFILLED | `test_order_lifecycle.py::test_forward_transitions` |
| AC-5  | FULFILLED → cancel() 거부 | `test_order_lifecycle.py::test_cancel_fulfilled_rejected` |
| AC-6  | CANCELLED 주문 전환 불가 (terminal) | `test_order_lifecycle.py::test_cancelled_is_terminal` |
| AC-7  | 동일 idempotency_key 재요청 시 기존 주문 반환 | `test_order_create.py::test_order_idempotent` |
| AC-8  | order_list 화면이 디자인 스냅샷과 일치 | `test_screen_parity.py` |
| AC-R  | 기존 auth 서비스 회귀 없음 | `test_regression.py` |

---

## Execution Checklist

### 1. 주문 도메인 모델 (`server/contexts/order/order.py`)
- [ ] `Order` dataclass — `order_id, items, total_amount, status, idempotency_key`
- [ ] `OrderResult` dataclass — `status, reason, order`
- [ ] `OrderService` — in-memory store (`orders: dict`)

### 2. 주문 생성 (`server/contexts/order/order.py`)
- [ ] `create(items, total_amount, *, idem_key)` — 유효성 검사, CREATED 생성
- [ ] total_amount < 1 → `OrderResult("rejected", "invalid_amount")`
- [ ] 멱등: 동일 idem_key 재요청 시 기존 주문 반환 + `replay=True`

### 3. 상태 전환 (`server/contexts/order/order.py`)
- [ ] `process(order_id)` — CREATED → PROCESSING
- [ ] `fulfill(order_id)` — PROCESSING → FULFILLED
- [ ] `cancel(order_id)` — CREATED/PROCESSING → CANCELLED, FULFILLED 거부
- [ ] 잘못된 전환 → `TransitionResult("rejected", "invalid_transition")`

### 4. 화면 parity (`sdd/04_verify/10_test/ui_parity/order_list.html`)
- [ ] 디자인 스냅샷(`order_list.html`) 기준으로 렌더링 확인
- [ ] `run_ui_parity.py` 구현 후 실행 — diff 0 통과

### 5. 테스트 (`tests/`)
- [ ] `test_order_create.py` — 생성·유효성·멱등 케이스 전체 통과
- [ ] `test_order_lifecycle.py` — 상태 전환·규칙 위반 케이스 전체 통과
- [ ] `test_screen_parity.py` — UI parity 통과
- [ ] `test_regression.py` — auth 서비스 회귀 없음 확인

### 6. DEV 게이트
- [ ] 전체 테스트 스위트 green
- [ ] `proof/run_proof.py` exit 0 통과
- [ ] 스키마 drift 0 확인

---

## Latest Verification

- proof: 미실행
