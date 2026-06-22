# 회원가입 OTP · todos + 실행 계획

## Scope
이메일 OTP 회원가입 한 기능을 발급·검증·만료·잠금·멱등 + 화면 parity까지 구현·검증.

## Acceptance Criteria
- AC-1~AC-6 (`sdd/01_planning/01_feature/auth_feature_spec.md`) 전부 테스트 통과.
- 회귀(로그인) green. proof 게이트 exit 0 = 완료.

## Execution Checklist (비중첩)
- [x] T1 @backend-dev  OTP 발급·검증·만료·잠금 (`src/main/java/com/datasense/auth/service/OtpService.java`)
- [x] T2 @backend-dev  가입 + 멱등 (`src/main/java/com/datasense/auth/service/SignupService.java`)
- [x] T3 @frontend-dev OTP 입력 화면 정합 (`sdd/04_verify/10_test/ui_parity/`)
- [x] T4 @test-dev     proof 게이트 + UI parity (`src/test/java/com/datasense/auth/AuthFlowTest.java`)

## Regression Scope
- direct: 가입·OTP 흐름
- shared: 로그인(`src/main/java/com/datasense/auth/service/LoginService.java`), 계정 저장소
- 근거: `sdd/02_plan/10_test/regression_verification.md`

## Validation
- `./gradlew test` → AuthFlowTest 2/2 PASS (`tmp/proof-results.json`)
- `./gradlew uiParity` → ui_parity 1/1
