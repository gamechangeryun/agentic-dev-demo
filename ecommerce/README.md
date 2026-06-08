# shop · 이커머스 모놀리식 데모 (10강)

10강 "이커머스 예제로 SDD 5단계를 처음부터 끝까지 실행" 데모의 타깃 레포입니다.
Java 17 · Spring Boot 3.5 · Gradle 로 만든 단일 배포 단위(모놀리식)이며, 내부는
DDD bounded context 로 분리되어 있습니다.

## 한눈에 보기

- 컨텍스트 6개: `catalog`(상품) · `inventory`(재고 예약) · `cart`(장바구니) · `ordering`(주문) · `payment`(결제) · `checkout`(오케스트레이션)
- 레이어: 각 컨텍스트가 `domain` · `application` · `infrastructure` · `web` 으로 분리
- 저장소: 인메모리 어댑터(포트/어댑터). 외부 DB·브로커 의존 없음
- API 21개. 전체 목록은 `sdd/03_build/01_feature/shop.md` 참조

## 빌드·검증

```
./gradlew clean build                                # 컴파일 + 테스트 23개
./gradlew test --tests 'kr.elice.shop.e2e.*'         # E2E 9개만
python3 sdd/99_toolchain/01_automation/run_arch_check.py   # DDD 경계 게이트
./gradlew bootRun                                    # 8080 기동
```

검증 실적: 이 워크스페이스(JDK 17 + Gradle 8.5)에서 **23/23 PASS**, 경계 게이트 **PASS**.

## 실습 반복 (멱등)

실습은 몇 번이든 같은 결과로 반복됩니다. `lab.sh` 로 시작 상태와 정답을 오갑니다.

```
./lab.sh reset     # 구현(src/main)을 비워 깨끗한 시작 상태로 (sdd·테스트·게이트는 유지)
./lab.sh solve     # 정답 구현(solution/)을 복원 (라이브 폴백·완성본 확인)
./lab.sh verify    # 경계 게이트 + 단위·E2E 테스트 실행
./lab.sh status    # 구현 존재 여부 확인
```

reset→build(또는 solve)→verify→reset 루프를 반복해도 매번 23/23 으로 수렴합니다.
빌드·테스트는 난수·실시간에 의존하지 않고(id는 시퀀스, 테스트는 @DirtiesContext 로
매번 초기화), API 는 Idempotency-Key 로 중복 요청을 한 번만 반영하므로 결정적입니다.

## 핵심 흐름

상품 등록 → 장바구니 담기 → 체크아웃(재고 예약 + 주문 생성) → 결제(주문 PAID +
예약 확정으로 물리 재고 차감) → 이행(FULFILLED). 취소하면 예약이 풀리고, 이미
결제된 주문이면 환불까지 보상합니다. 동시에 들어온 주문이 가용 재고를 넘으면
뒤 주문을 거부해 초과 판매를 막습니다.

## SDD 트리

`sdd/` 아래에 00_sources → 01_planning → 02_plan → 03_build → 04_verify →
05_operate + 99_toolchain 이 모두 채워져 있습니다. 요구사항 원문 한 줄이 AC 한 줄,
테스트 한 케이스로 끊기지 않고 이어집니다.
