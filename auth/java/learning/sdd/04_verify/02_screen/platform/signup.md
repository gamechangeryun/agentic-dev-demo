# signup_otp 화면 · 검증 (retained)

> 명세 `01_planning/02_screen/signup_otp_screen_spec.md`(SC-1~SC-4)의 AC를 retained 증거에 매핑.
> 이번 세션에서 `./gradlew test` 직접 재실행 — 아래는 그 실측 산출 기준.

## 캐노니컬 게이트
- 스냅샷: `sdd/04_verify/10_test/ui_parity/signup_otp.html`
- 자바 게이트: `./gradlew test` → `SignupScreenParityTest` 2/2 PASS (`tmp/proof-results.json`, `status: PASS`).
  - `servedScreen_containsCanonicalOtpSnapshot` — 서빙 화면이 스냅샷 영역을 verbatim 포함(SC-1).
  - `servedScreen_wiresOtpEndpoints` — 화면이 `/auth/otp/issue`·`/auth/signup` 연결(SC-2·SC-3).
- python `run_ui_parity.py`는 python 변형 전용(`server.contexts.auth` 의존) → 이 자바 레포에선 비가용, 자바 게이트로 대체.

## Surface
- 요소: 제목('인증번호 입력'), 안내문, 6자리 입력(`maxlength="6"`, `inputmode="numeric"`), 확인 버튼 — 스냅샷과 일치.
- 흐름: 이메일 발급 단계 → OTP 입력 단계 → 가입 결과(성공/거부) 표시.

## 회귀 4분면
| 분면 | 검증 대상 | 수용기준 | 결과 |
| --- | --- | --- | --- |
| 화면 | signup_otp 스냅샷 일치 | SC-1 | PASS (`servedScreen_containsCanonicalOtpSnapshot`) |
| 연동 | 화면→OTP 발급·가입 호출 | SC-2·SC-3 | PASS (`servedScreen_wiresOtpEndpoints` + `AuthFlowTest` 백엔드 계약) |
| 기능 | 발급→검증→가입 happy path | AC-1·AC-2 | PASS (`happyPath_issueOtp_signup_login_allReturn2xx`) |
| 보안 | 오입력 거부 | AC-3 | PASS (`signup_withWrongOtp_returns4xx`) |
| 회귀 | 기존 `/auth/**` 계약 무손상 | shared | PASS (REST 계약 변경 없음, AuthFlowTest 2/2 green) |

총 게이트: `./gradlew test` 4/4 PASS.

## 회귀 범위 (선정·근거)
- direct: 가입 OTP 화면(스냅샷 정합) + 화면→백엔드 호출 계약.
- shared: 기존 REST 컨트롤러(`/auth/**`) — `HomeController`는 `/`·`/signup`만 매핑해 `/auth/**`와 충돌 없음.
- 근거: `02_plan/10_test/regression_verification.md` — 가입 화면 direct, 로그인은 계정 저장소 공유로 shared.

## Residual Risk
- 실 브라우저(Playwright) 픽셀·반응형은 미검증 — 결정적 HTML 스냅샷 정합으로 대체(브라우저 비가용).
- 데모 편의로 발급 code를 화면 힌트로 노출 — 실서비스에서는 메일/SMS 채널 전용.
- 인메모리 저장(재시작 시 OTP·계정 소실), 잠금 해제·재발급 쿨다운 정책 미정(미범위).
