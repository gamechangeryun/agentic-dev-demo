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
