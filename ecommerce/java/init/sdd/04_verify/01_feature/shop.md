# 04_verify · 이커머스 쇼핑 검증 결과

> verify 단계는 "사람이 봤다"가 아니라 "코드가 통과시켰다"를 완료 기준으로 씁니다.
> 모든 AC 는 결정적 JUnit 테스트로 증명됩니다.

## 검증 방식

- 도메인 규칙은 애그리거트 단위 테스트로 빠르게 검증합니다(Spring 컨텍스트 없음).
- 컨텍스트를 가로지르는 흐름은 @SpringBootTest + MockMvc 로 실제 HTTP 경계를 통과시켜 검증합니다.
- 결제 게이트웨이는 포트/어댑터로 분리해 거절 경로를 결정적으로 재현합니다.

## AC 커버리지

27개 AC 전부가 최소 하나의 테스트로 매핑됩니다. 상세 매핑은
`01_planning/01_feature/shop_feature_spec.md` 의 검증 열에 있습니다.

## 결과

`10_test/proof_evidence.md` 참조. 23/23 PASS, exit 0.

## 게이트 통과 기준

- 컴파일 성공: `./gradlew build` 가 BUILD SUCCESSFUL.
- 테스트 전부 green: failures·errors 가 0.
- E2E 9개가 모두 PASS: 요구사항 원문의 모든 비즈니스 룰이 API 경계에서 동작함을 의미합니다.

## 실제 검증 실행 기록 (2026-06-23, init · 전체 완료)

build 단계를 구역 단위로 진행: ① 도메인 구역(섹션 1~6: shared + catalog·inventory·cart·
ordering·payment) → ② checkout 오케스트레이션(섹션 7: 체크아웃 + 결제·환불 취소 보상).
두 구역 완료로 전체 시스템이 green.

- 환경: Windows 11 · Temurin JDK 17.0.19 · Gradle Wrapper · Python 3.14
- 구현: `src/main/java/kr/elice/shop` 44개 `.java` (6 컨텍스트 + shared 커널), 엔드포인트 21/21
- 게이트 1 — DDD 경계: `python sdd/99_toolchain/01_automation/run_arch_check.py`
  → domain 순수성 위반 0건, 컨텍스트 의존 엣지 9개(checkout→전부, payment→ordering·inventory,
  cart·inventory→catalog), 순환 없음, `RESULT: arch_check PASS`
- 게이트 2 — 테스트: `./gradlew.bat test --rerun-tasks --console=plain` (캐시 무효화 강제 실행)
  - 단위: ProductTest 4 · InventoryServiceTest 5 · OrderTest 5 → **14/14 PASS**
  - E2E: ShopE2ETest 9 → **9/9 PASS** (E2E-1 전체여정 · 2·3 취소+환불 · 4 oversell ·
    6 멱등 · 8 이행가드 · 9 결제거절 포함)
  - 집계: tests=23, failures=0, errors=0 → **23/23 PASS**
- 회귀 범위: 모놀리식 전체(6 컨텍스트 + shared)를 단위 14 + E2E 9로 직접 커버. checkout
  추가는 결제·환불·예약해제를 가로지르므로 E2E 전 시나리오를 재실행(부분 회귀 제외 없음).
- 잔여 리스크: 인메모리 어댑터·데모 PG 게이트웨이(운영 전환은 데모 범위 밖). 롤아웃 수행 안 함
  (로컬 검증만) — `05_operate` 의 인도 상태는 로컬 검증 스냅샷이며 실제 배포는 미수행.
