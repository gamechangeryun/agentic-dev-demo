# 이커머스 모놀리식 데모 — 완성 버전 (complete)

10강 실습의 완성본입니다. 여섯 컨텍스트가 모두 구현되어 단위 14 + E2E 9 = 23/23 을
통과하고, DDD 경계 게이트도 PASS 합니다. 정답·대조용입니다.

## 검증

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./lab.sh verify     # 경계 게이트 PASS + 단위 14 + E2E 9 = 23/23
```

## 구조
- `src/main/java/kr/elice/shop/` : 여섯 컨텍스트(catalog·inventory·cart·ordering·payment·checkout) + shared
- 각 컨텍스트는 domain·application·infrastructure·web 네 레이어로 분리
- 저장소는 인메모리 어댑터(포트/어댑터). 외부 DB·브로커 의존 없음

## 핵심 흐름

상품 등록 → 장바구니 담기 → 체크아웃(재고 예약 + 주문 생성) → 결제(주문 PAID +
예약 확정으로 물리 재고 차감) → 이행(FULFILLED). 취소하면 예약이 풀리고, 이미
결제된 주문이면 환불까지 보상합니다. 동시에 들어온 주문이 가용 재고를 넘으면
뒤 주문을 거부해 초과 판매를 막습니다.

> 이 폴더는 완성본입니다. `./lab.sh reset` 을 실행하면 구현이 비워지니 주의하세요.
