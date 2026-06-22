# 회원가입(Signup) · current-state (build)

> 03_build: Overwrite Rule(지금 상태 1벌). 명세 `01_planning/01_feature/signup_feature_spec.md` 기준.

## Absorbed Planning
- `01_planning/01_feature/signup_feature_spec.md` (전체 명세)
- `01_planning/01_feature/auth_feature_spec.md` (AC-1~AC-6 가드레일)
- `02_plan/01_feature/auth_todos.md` (통합 plan, 백엔드 T1~T4)

## Runtime Assembly
- `AuthController.signup` → `SignupService.signup(email, code, purpose, idemKey)`
  → `OtpService.verify` → 통과 시 `IdempotencyStore.issueOnce`로 `AccountRepository.save`(멱등)
- OTP 발급: `AuthController.issueOtp` → `OtpService.issue` (응답에 code 포함, 데모)
- 입력 검증 실패는 `ApiExceptionHandler`가 `400`으로 변환

## Modules
| 모듈 | 책임 | AC |
| --- | --- | --- |
| `service/OtpService.java` | OTP 발급·검증·만료·잠금(TTL·시도제한) | 1·3·4 |
| `service/SignupService.java` | 가입 흐름 조립 + 멱등 | 2·5 |
| `service/IdempotencyStore.java` | sha256 멱등 키, issueOnce(replay) | 5 |
| `controller/AuthController.java` | `/auth/otp/issue`·`/auth/signup`·`/auth/login` | 1·2·5 |
| `controller/ApiExceptionHandler.java` | 검증 실패 → 400 | - |
| `dto/SignupRequest.java`,`IssueOtpRequest.java` | 입력·검증(@Email/@Pattern) | - |
| `service/LoginService.java` | 기존 로그인(회귀) | - |

## Current Behavior
- 발급 → 검증(만료·잠금 처리) → 통과 시 계정 생성, 동일 키 재요청은 `replay=true`로 계정 추가 없음.
- 상태코드: 성공 `201`, 검증 거부 `422`(reason: no_otp/locked/expired/wrong_code), 입력 오류 `400`.
- 정책값은 `application.yml`(`auth.otp.ttl-seconds=300`, `max-attempts=5`)에서 주입.
