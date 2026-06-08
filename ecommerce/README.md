# shop · 이커머스 모놀리식 데모 (10강)

10강 "이커머스 예제로 SDD 5단계를 처음부터 끝까지 실행" 데모의 타깃 레포입니다.
Java 17 · Spring Boot 3.5 · Gradle 로 만든 단일 배포 단위(모놀리식)이며, 내부는
DDD bounded context 로 분리되어 있습니다.

## 한눈에 보기

- 컨텍스트 6개: `catalog`(상품) · `inventory`(재고 예약) · `cart`(장바구니) · `ordering`(주문) · `payment`(결제) · `checkout`(오케스트레이션)
- 레이어: 각 컨텍스트가 `domain` · `application` · `infrastructure` · `web` 으로 분리
- 저장소: 인메모리 어댑터(포트/어댑터). 외부 DB·브로커 의존 없음
- API 21개. 요구사항과 AC는 `sdd/01_planning/01_feature/shop_feature_spec.md`, 구현 작업 단위는 `sdd/02_plan/01_feature/shop_todos.md` 참조

## 빌드·검증

```
./gradlew clean build                                # 컴파일 + 테스트 23개
./gradlew test --tests 'kr.elice.shop.e2e.*'         # E2E 9개만
python3 sdd/99_toolchain/01_automation/run_arch_check.py   # DDD 경계 게이트
./gradlew bootRun                                    # 8080 기동
```

구현 완료 후 검증 기준: JDK 17 + Gradle 8.5 에서 **23/23 PASS**, 경계 게이트 **PASS**.
(클린 시작 상태에서는 구현이 없어 컴파일 단계에서 멈춥니다. Claude Code로 구현을 채운 뒤 통과합니다.)

## 실습 반복 (멱등)

이 레포는 학습자가 clone하면 바로 시작할 수 있는 클린 초기 상태입니다. 설계 문서
(00_sources~02_plan)와 테스트 스펙·경계 게이트만 제공되고, 구현(src/main)과
03_build·04_verify 결과물은 비어 있습니다. PPT를 보며 Claude Code 발화로 채웁니다.

```
./lab.sh status    # 구현 존재 여부 확인 (시작 시 '구현 없음')
./lab.sh reset     # 다시 깨끗한 시작 상태로 (구현만 비움, 설계·테스트·게이트 유지)
./lab.sh verify    # 경계 게이트 + 단위·E2E 테스트 실행 (구현 완료 후)
```

reset→build(Claude Code)→verify 루프를 반복해도, 구현이 테스트 스펙을 통과하는 한
매번 23/23 으로 수렴합니다. 빌드·테스트는 난수·실시간에 의존하지 않고(id는 시퀀스,
테스트는 @DirtiesContext 로 매번 초기화), API 는 Idempotency-Key 로 중복 요청을
한 번만 반영하므로 결정적입니다.

## 핵심 흐름

상품 등록 → 장바구니 담기 → 체크아웃(재고 예약 + 주문 생성) → 결제(주문 PAID +
예약 확정으로 물리 재고 차감) → 이행(FULFILLED). 취소하면 예약이 풀리고, 이미
결제된 주문이면 환불까지 보상합니다. 동시에 들어온 주문이 가용 재고를 넘으면
뒤 주문을 거부해 초과 판매를 막습니다.

## SDD 트리

클린 시작 상태에서는 `sdd/` 아래 00_sources → 01_planning → 02_plan (설계)와
99_toolchain (게이트)만 제공됩니다. 03_build(current-state) · 04_verify(검증 결과) ·
05_operate(운영)는 학습자가 build·verify 단계를 진행하며 채웁니다. 요구사항 원문
한 줄이 AC 한 줄, 테스트 한 케이스로 끊기지 않고 이어집니다.
