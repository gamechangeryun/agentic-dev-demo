# 이커머스 모놀리식 도메인 (파이썬 포팅)

자바 Spring Boot DDD solution(`../java/`)의 도메인 규칙을 순수 파이썬으로 옮긴 포팅입니다. 프레임워크를 1:1로 이식하지 않고, 6개 bounded context의 **비즈니스 규칙 동등성**을 목표로 합니다. 외부 DB·브로커 없이 인메모리 저장소(dict)로 동작합니다.

## 구조

```
python/
  shop/
    shared/      금액(Money)·오류코드(ErrorCode)·도메인예외·페이지 값 객체
    catalog/     상품(Product) 애그리거트 · 카탈로그 유스케이스(생성 멱등·검색·페이징)
    inventory/   재고 예약(Reservation) · oversell 방지 게이트(동시성 락)
    cart/        장바구니(Cart) · 수량 합산·항목 제거
    ordering/    주문(Order) 상태머신 · 가격 스냅샷 줄 항목
    payment/     결제(Payment) · 멱등 결제 · 데모 게이트웨이 · 환불
    checkout/    체크아웃 오케스트레이션(예약→주문) · 취소 보상(해제+환불)
    app.py       ShopApp: Spring DI 대신 명시적 배선
  tests/         pytest 단위 + E2E 수용기준
  run_proof.py   결정적 proof 게이트(pytest 자동 부트스트랩)
```

## 핵심 비즈니스 규칙 (자바와 동일)

- **멱등 결제**: 같은 idempotency_key 재요청은 기존 결제를 그대로 돌려주어 이중 청구를 막습니다.
- **재고 차감**: 체크아웃은 예약만 잡고 물리 재고는 그대로 둡니다. 결제 확정 시 예약을 confirm 하여 물리 재고를 실제로 차감합니다.
- **oversell 방지**: 가용 재고(물리 - 활성 예약)를 초과하는 예약은 INSUFFICIENT_STOCK 으로 거부합니다. 동시 예약은 락으로 보호합니다.
- **주문 상태머신**: CREATED → PAID → FULFILLED 한 방향이며, CREATED·PAID 에서만 취소가 가능합니다. 결제된 주문 취소는 환불까지 보상합니다.
- **아카이브 가드**: ARCHIVED 상품은 장바구니에 담거나 재고를 예약·변경할 수 없습니다.

## 실행법

도메인 코드는 **표준 라이브러리만** 사용하므로 런타임 의존성이 없습니다. 테스트만 pytest 가 필요합니다.

### 1) 컴파일 점검 (build)

```bash
python3 -m compileall -q shop tests
```

### 2) 테스트 (proof)

가장 간단한 방법은 자동 부트스트랩 러너입니다. pytest 가 없으면 로컬 `.venv` 에 한 번만 설치한 뒤 실행합니다(시스템 파이썬을 더럽히지 않습니다).

```bash
python3 run_proof.py
```

pytest 가 이미 설치돼 있다면 곧장 실행해도 됩니다.

```bash
python3 -m pip install -r requirements.txt   # 최초 1회
python3 -m pytest -q
```

기대 결과: **25 passed** (단위 16 + E2E 9). 자바 23개(단위 14 + E2E 9)와 핵심 수용기준이 동등합니다.
