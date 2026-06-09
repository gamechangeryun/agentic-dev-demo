# 04_verify · proof 증빙

> `./gradlew build` 실행 결과입니다. 이 워크스페이스(JDK 17 + Gradle 8.5)에서 실제로
> 컴파일·테스트가 통과한 기록입니다. exit 0, failures 0, errors 0.

## 요약

```
BUILD SUCCESSFUL
total tests = 23 · passed = 23 · failed = 0 · errors = 0
ProductTest           4/4
OrderTest             5/5
InventoryServiceTest  5/5
ShopE2ETest           9/9  (실제 HTTP 라우팅 · @SpringBootTest + MockMvc)
```

## E2E 시나리오 (9개 · 전부 PASS)

| 시나리오 | 검증 내용 | AC |
| --- | --- | --- |
| E2E-1 전체 여정 | 상품→장바구니→체크아웃→결제→이행, 가용 재고 감소 확인 | AC-O1·O4·P1 |
| E2E-2 결제 전 취소 | 예약 해제로 가용 재고 복원 | AC-I3 |
| E2E-3 결제 후 취소 | 결제 REFUNDED + 재고 복원 | AC-P4 |
| E2E-4 oversell 방지 | 가용분 초과 둘째 체크아웃 409 거부, 보상 해제 | AC-I4·O2 |
| E2E-5 검색·페이징 | 이름 검색 + 페이지 크기 동작 | AC-C5 |
| E2E-6 멱등성 | 같은 키 체크아웃·결제 한 번만 반영 | AC-O7·P3 |
| E2E-7 아카이브 차단 | ARCHIVED 상품 담기 409 거부 | AC-T3 |
| E2E-8 이행 가드 | 결제 전 이행 409 거부 | AC-O5 |
| E2E-9 결제 거절 | declined 수단 402, 주문 CREATED 유지 | AC-P2 |

## 단위 테스트 (14개 · 전부 PASS)

- ProductTest: 가격 거부·재고 가감·재고 부족·아카이브 차단 (4)
- OrderTest: 총액 거부·정방향 전환·이행 가드·취소 가드·환불 신호 (5)
- InventoryServiceTest: 예약·확정·해제·oversell·동시성 100건 (5)

## 동시성 증빙

InventoryServiceTest.concurrentReservationsNeverOversell 는 재고 50개에 100개 스레드가
동시에 1개씩 예약을 시도했을 때 정확히 50건만 성공하고 가용분이 0이 됨을 단언합니다.
reserve 메서드의 동기화로 초과 판매가 발생하지 않습니다.
