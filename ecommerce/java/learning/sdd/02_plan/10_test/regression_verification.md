# 02_plan · 회귀 검증 계획

> 새 기능이 기존 기능을 깨지 않는지 확인하는 회귀 4분면입니다. 모든 테스트는
> ./gradlew test 한 번으로 결정적으로 재현됩니다.

## 회귀 4분면

| 분면 | 무엇을 지키는가 | 테스트 |
| --- | --- | --- |
| 기능 회귀 | 상태머신·재고·결제 규칙이 유지되는가 | ProductTest, OrderTest, InventoryServiceTest |
| 통합 회귀 | 컨텍스트 간 흐름(체크아웃·결제·취소)이 유지되는가 | ShopE2ETest 1·2·3 |
| 경계 회귀 | 거부 경로(oversell·아카이브·이행가드·결제거절)가 유지되는가 | ShopE2ETest 4·7·8·9 |
| 멱등 회귀 | 중복 요청이 한 번만 반영되는가 | ShopE2ETest 6 |

## 실행

```
./gradlew test            # 단위 14 + E2E 9 = 23개
./gradlew test --tests 'kr.elice.shop.e2e.*'   # E2E 9개만
```

## 기준

- exit 0 이고 failures·errors 가 0이어야 통과입니다.
- 동시성 테스트는 재고 50개에 100건 요청 시 정확히 50건 성공, 가용분 0을 단언합니다.
