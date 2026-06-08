# 05_operate · 배포·롤백 runbook

> 데모 기준 운영 절차입니다. 단일 모놀리식이므로 배포 단위가 하나입니다.

## 배포 (DEV)

```
./gradlew clean build          # 컴파일 + 테스트 (23/23 green 확인)
./gradlew bootRun              # 8080 포트로 기동 (인메모리 저장소)
```

기동 후 헬스 확인:

```
curl -s localhost:8080/api/products            # 빈 목록 {items:[],total:0,...}
```

## 스모크 시나리오 (운영 점검)

1. POST /api/products 로 상품을 만든다.
2. POST /api/carts → POST /api/carts/{id}/items 로 담는다.
3. POST /api/checkout 로 주문을 만든다(CREATED).
4. POST /api/payments 로 결제한다(PAID).
5. POST /api/orders/{id}/fulfill 로 이행한다(FULFILLED).

## 롤백

- 모놀리식 단일 아티팩트이므로 직전 빌드 jar 로 교체 후 재기동합니다.
- 인메모리 저장소는 재기동 시 초기화됩니다. 운영 전환 시 JPA 어댑터와 영속 DB 로 교체합니다.

## 운영 전환 시 교체 지점

- `infrastructure/InMemory*Repository` → JPA 어댑터
- `infrastructure/DemoPaymentGateway` → 실제 PG 연동 어댑터
- 멱등 저장(애플리케이션 서비스의 ConcurrentHashMap) → 분산 멱등 저장소
