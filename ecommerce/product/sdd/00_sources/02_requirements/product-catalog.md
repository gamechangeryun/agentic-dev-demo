# 상품 카탈로그: 요구사항 원문 (강의 데모용 가상)

> 실재 개인정보·실명 없음. 상품 ID는 불투명 예시값(`prod-001`)만 사용.

- 상품은 이름(name)·가격(price)·재고(stock_quantity)로 생성되며, 생성 즉시 ACTIVE 상태가 된다.
- 가격이 0원 이하이면 상품 생성을 거부한다.
- 재고는 add_stock() 으로 증가, reduce_stock() 으로 감소시킨다.
- 재고가 부족하면 reduce_stock()을 거부한다 (stock_quantity < 요청 수량).
- ARCHIVED 상품은 재고를 변경할 수 없다.
- 같은 idempotency_key로 중복 생성 요청이 오면 기존 상품을 반환한다.
- 상품 목록 화면(product_list)은 디자인 스냅샷과 일치해야 한다.
- 기존 auth·order 서비스 흐름은 상품 기능 추가로 깨지지 않는다.
