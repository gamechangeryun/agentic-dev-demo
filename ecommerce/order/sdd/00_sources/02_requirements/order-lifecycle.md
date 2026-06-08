# 주문 라이프사이클: 요구사항 원문 (강의 데모용 가상)

> 실재 개인정보·실명 없음. 주문 ID는 불투명 예시값(`ord-001`)만 사용.

- 주문은 품목(items)과 총액(total_amount)으로 생성되며, 생성 즉시 CREATED 상태가 된다.
- 총액이 0원 이하이면 주문 생성을 거부한다.
- 주문은 CREATED → PROCESSING → FULFILLED 순서로 전진한다.
- CREATED 또는 PROCESSING 상태에서만 취소(CANCELLED)할 수 있다.
- FULFILLED 주문은 취소할 수 없다.
- CANCELLED 주문은 어떠한 상태로도 전환할 수 없다.
- 같은 idempotency_key로 중복 주문 요청이 오면 기존 주문을 반환한다(중복 생성 없음).
- 주문 목록 화면(order_list)은 디자인 스냅샷과 일치해야 한다.
- 기존 auth 서비스 흐름은 주문 기능 추가로 깨지지 않는다.
