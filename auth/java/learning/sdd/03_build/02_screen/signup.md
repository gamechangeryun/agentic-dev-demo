# 회원가입 OTP 화면(signup_otp) · current-state (build)

> 03_build: Overwrite Rule(지금 상태 1벌). 명세 `01_planning/02_screen/signup_otp_screen_spec.md` 기준.

## Absorbed Planning
- `01_planning/02_screen/signup_otp_screen_spec.md` (화면 명세, SC-1~SC-4)
- `02_plan/01_feature/auth_todos.md` (통합 plan, 화면 T5~T8)
- 가드레일 `01_feature/auth_feature_spec.md` AC-6

## Runtime Assembly
- `GET /` · `GET /signup` → `HomeController` forward → `static/signup.html`
- 화면 JS → `POST /auth/otp/issue`(발급) → `POST /auth/signup`(검증·가입) — 기존 OTP 백엔드 재사용
- 정적 리소스라 빌드 스텝 없음(vanilla JS). REST `/auth/**` 계약은 불변.

## Modules
| 모듈 | 책임 | AC |
| --- | --- | --- |
| `src/main/resources/static/signup.html` | 이메일 발급 단계 + OTP 입력 단계(캐노니컬 스냅샷 verbatim) + 결과 표시 | SC-1·SC-2·SC-3·SC-4 |
| `controller/HomeController.java` | `/`·`/signup` → signup.html forward | - |
| `test/.../SignupScreenParityTest.java` | 스냅샷 정합 + 엔드포인트 연결 게이트(자바) | SC-1·SC-2·SC-3 |

## Current Behavior
- 이메일 입력 → "인증번호 받기" → `/auth/otp/issue` 200이면 OTP 입력 단계로 전환(데모: 응답 code를 힌트로 노출).
- 6자리 입력 → "확인" → `/auth/signup`. 201이면 "가입 완료"(replay=true면 "이미 가입됨"), 422면 reason별 한국어 메시지, 400/형식오류면 안내.
- OTP 입력 영역 마크업은 `04_verify/10_test/ui_parity/signup_otp.html` 스냅샷과 1:1 일치(verbatim 삽입).

## Notes
- 화면 정합 게이트는 자바 `SignupScreenParityTest`가 캐노니컬. python `run_ui_parity.py`는 python 변형 전용(`server.contexts.auth` 의존)이라 이 자바 레포에서는 실행 대상이 아님.
