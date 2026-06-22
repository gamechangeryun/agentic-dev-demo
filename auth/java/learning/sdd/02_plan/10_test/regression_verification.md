# 회귀 검증 범위 (retained)

| 표면 | 분류 | 검증 |
| --- | --- | --- |
| 가입·OTP | direct | `test_otp.py`, `test_signup.py` |
| 로그인(기존) | shared | `test_regression.py` |
| 가입 화면 | direct | (java) `SignupScreenParityTest` — 스냅샷 verbatim 정합 + 엔드포인트 연결 |

선정 근거: 회원가입은 계정 저장소를 로그인과 공유하므로 회귀 범위를 로그인까지 넓힌다.
