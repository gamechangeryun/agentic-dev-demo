# 회원가입(Signup) · 검증 (retained)

> 04_verify: 명세 `01_planning/01_feature/signup_feature_spec.md`의 AC를 retained 증거에 매핑.
> 본 단계는 정리(요약)만 수행 — 이번 세션에서 gradle을 직접 재실행하지 않았고, 아래는 retained 산출물 기준.

## 게이트 · 증거
- 기능 게이트: `./gradlew test` → `AuthFlowTest` 2/2 PASS (머신 산출: `tmp/proof-results.json`, `status: PASS`).
- 화면 게이트: `python3 sdd/99_toolchain/01_automation/run_ui_parity.py` → ui_parity 1/1 PASS (`04_verify/02_screen/platform/signup.md`).

## 회귀 4분면
| 분면 | 검증 대상 | 수용기준 | 결과 |
| --- | --- | --- | --- |
| 기능 | 발급→검증→가입 happy path | AC-1·AC-2 | PASS (`happyPath_issueOtp_signup_login_allReturn2xx`) |
| 보안 | 오입력/만료/잠금 거부 | AC-3·AC-4 | PASS (`signup_withWrongOtp_returns4xx`) |
| 멱등 | 재요청 시 계정 중복 0 | AC-5 | PASS · replay=true |
| 화면 | signup_otp 스냅샷 일치 | AC-6 | PASS · ui_parity 1/1 |
| 회귀 | 기존 로그인 무손상 | shared | PASS (happy path에 login 포함) |

## 회귀 범위 (선정·근거)
- direct: 가입·OTP 흐름. shared: 로그인 + 계정 저장소(`AccountRepository`).
- 근거: `02_plan/10_test/regression_verification.md` — 계정 저장소 공유로 회귀를 로그인까지 확장.

## Residual Risk
- 실 브라우저(Playwright) 픽셀·반응형은 미검증 — 결정적 HTML parity로 대체.
- 메일/SMS 실 OTP 채널 미구현(응답 노출 code로 대체), 인메모리 저장(재시작 시 소실).
- 잠금 해제·재발급 쿨다운 정책 미검증(미정의 범위).
