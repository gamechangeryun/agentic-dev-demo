# 회원가입 OTP · todos + 실행 계획 (통합)

> 02_plan. 회원가입 OTP 한 기능의 백엔드 + 화면을 통합 추적하는 단일 plan.
> 명세: `01_planning/01_feature/{auth_feature_spec,signup_feature_spec}.md`,
> `01_planning/02_screen/signup_otp_screen_spec.md`.
> (세부 plan `signup_plan.md`·`02_screen/signup_otp_screen_plan.md`를 본 파일로 합본.)

## Scope
이메일 OTP 회원가입 한 기능을, 발급·검증·만료·잠금·멱등 백엔드 + 회원가입 OTP **화면**(서빙·정합)까지 구현·검증.
- Out: 비밀번호 자격증명, 소셜 로그인, 실제 메일 발송, 영속 DB, 픽셀/반응형 정합.

## Assumptions
- 저장은 인메모리(`ConcurrentHashMap`) — 데모 한정, 영속화 비범위.
- 데모 편의로 발급 code를 응답/화면 힌트에 노출(메일 채널 미연동).
- 정책값은 `application.yml` 외부화: TTL 300초, 최대 5회.
- 계정 저장소(`AccountRepository`)를 로그인과 공유 → 회귀를 로그인까지 확장.
- 화면은 정적 리소스(`static/signup.html`) + vanilla JS 서빙(빌드 스텝 없음).

## Acceptance Criteria
- 기능 AC-1~AC-6 (`auth_feature_spec.md` / `signup_feature_spec.md`) 전부 테스트 통과.
- 화면 SC-1~SC-4 (`signup_otp_screen_spec.md`): 스냅샷 verbatim 정합 + OTP 엔드포인트 연결.
- 거부 사유 4종(`no_otp`/`locked`/`expired`/`wrong_code`) 명세 일치, 상태코드 계약 일치(201/422/400).
- 회귀: `POST /auth/login` green, 기존 `/auth/**` 계약 무손상.
- proof 게이트 `./gradlew test` exit 0 = 완료.

## Execution Checklist (비중첩, 하나만 in-progress)
### 백엔드 (완료)
- [x] T1 @backend-dev  OTP 발급·검증·만료·잠금 (`service/OtpService.java`)
- [x] T2 @backend-dev  가입 + 멱등 (`service/SignupService.java`, `service/IdempotencyStore.java`)
- [x] T3 @backend-dev  API 계약·검증 변환 (`controller/AuthController.java`, `ApiExceptionHandler.java`, `dto/SignupRequest.java`)
- [x] T4 @test-dev     proof 게이트 (`test/.../AuthFlowTest.java`)
### 화면 (완료)
- [x] T5 @frontend-dev 화면 명세 (`01_planning/02_screen/signup_otp_screen_spec.md`)
- [x] T6 @frontend-dev 회원가입 OTP 화면 구현 (`resources/static/signup.html`) — 스냅샷 verbatim 포함, 이메일 발급→인증창 노출(2단계) 흐름, OTP 입력→가입 연결
- [x] T7 @frontend-dev 루트 라우트 (`controller/HomeController.java` → `/`·`/signup` forward)
- [x] T8 @test-dev     화면 정합 게이트 (`test/.../SignupScreenParityTest.java`)

## Regression Scope
- direct: 가입·OTP 흐름(발급/검증/가입/멱등) + 가입 화면(스냅샷 정합·엔드포인트 연결).
- shared: 로그인(`service/LoginService.java`), 계정 저장소(`AccountRepository`), 기존 REST `/auth/**`.
- 근거: `02_plan/10_test/regression_verification.md` — 계정 저장소 공유로 회귀를 로그인까지 확장.
- `HomeController`는 `/`·`/signup`만 매핑 → `/auth/**` 계약과 충돌 없음.

## Current Notes
- 백엔드는 기존 구현·검증 완료, 본 세션에서 화면(T5~T8)을 추가 구현.
- 화면 정합 캐노니컬 게이트는 자바 `SignupScreenParityTest`. python `run_ui_parity.py`는 python 변형 전용(`server.contexts.auth` 의존)이라 이 자바 레포에선 비가용.
- 잠금 해제·재발급(쿨다운) 정책 미정 → 후속 plan 후보(현재는 새 `issue`로 레코드 덮어쓰기로만 재시도).

## Validation
- `./gradlew test` → `AuthFlowTest`(2) + `SignupScreenParityTest`(2) = **4/4 PASS** (`tmp/proof-results.json`, status PASS).
- `./gradlew uiParity` → test 의존 회귀 게이트 재실행.
- retained 검증 요약: `04_verify/01_feature/{auth,signup}.md`, `04_verify/02_screen/platform/signup.md`.
