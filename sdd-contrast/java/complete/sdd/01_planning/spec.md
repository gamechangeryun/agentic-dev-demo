# 회원가입 OTP 명세 (EARS, 검증 가능)

`Otp` 인터페이스: issue(email,t) / verify(email,code,t) / signup(email,code,t) / created()
시간은 정수 t(초)로 주입한다(테스트가 sleep 없이 만료를 재현).

## AC-1 정상 발급·검증
When 유효한 OTP로 가입을 시도하면, the system shall 가입을 성공시킨다.

## AC-2 만료 OTP 거부
When 발급 후 TTL(300초)이 지난 OTP로 검증하면, the system shall 그 OTP를 거부한다.

## AC-3 5회 오류 잠금
When 같은 사용자가 5회 연속 오답을 내면, the system shall 이후 정답을 넣어도 거부한다(무차별 대입 차단).

## AC-4 재요청 멱등
When 같은 사용자가 두 번 가입하면, the system shall 계정을 1개만 생성한다.
