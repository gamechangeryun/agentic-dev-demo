# 회원가입 OTP 화면(signup_otp) · 화면 명세서

> 01_planning · screen. 회원가입 OTP 인증 화면의 명세.
> 디자인 스냅샷: `04_verify/10_test/ui_parity/signup_otp.html` (캐노니컬 정합 기준).
> 강의 데모용 가상 화면 — 실재 개인정보 없음. 이메일은 예시값(`a@x.com`)만 사용.

## 1. 목적 · 범위

- **목적**: 사용자가 이메일로 받은 6자리 OTP를 입력해 회원가입을 완료하는 화면을 제공한다.
- **범위(In)**: 이메일 입력 → 인증번호 발급 → 6자리 입력 → 가입 완료까지의 단일 페이지 흐름.
- **범위(Out)**: 비밀번호 입력 UI, 소셜 로그인 버튼, 다국어, 반응형 픽셀 정합(브라우저 비가용 환경).
- **불변(회귀)**: 기존 REST 계약(`/auth/otp/issue`·`/auth/signup`·`/auth/login`)을 화면 추가로 바꾸지 않는다.

## 2. 인수 기준 (EARS)

`auth_feature_spec.md` AC-6(화면 스냅샷 일치)을 화면 단위로 구체화한다.

| AC | 요지 | 검증 위치 |
| --- | --- | --- |
| SC-1 | OTP 입력 영역이 디자인 스냅샷(`signup_otp.html`)과 정확히 일치 | `SignupScreenParityTest` |
| SC-2 | 이메일 입력 후 "인증번호 받기"가 `POST /auth/otp/issue`를 호출 | 화면 JS + `AuthFlowTest` 백엔드 계약 |
| SC-3 | 6자리 입력 후 "확인"이 `POST /auth/signup`을 호출하고 결과(성공/거부)를 표시 | 화면 JS + `AuthFlowTest` 백엔드 계약 |
| SC-4 | 입력은 숫자 6자리로 제한(`inputmode=numeric`, `maxlength=6`) | 스냅샷 정합 |

## 3. 화면 구조 (signup_otp)

캐노니컬 스냅샷(OTP 입력 단계):

```html
<main class="signup"><h1>인증번호 입력</h1><p>이메일로 받은 6자리 인증번호를 입력하세요.</p><input name="otp" inputmode="numeric" maxlength="6"/><button type="submit">확인</button></main>
```

| 요소 | 내용 | 비고 |
| --- | --- | --- |
| 제목 `h1` | `인증번호 입력` | 고정 카피 |
| 안내문 `p` | `이메일로 받은 6자리 인증번호를 입력하세요.` | 고정 카피 |
| 입력 `input[name=otp]` | `inputmode="numeric" maxlength="6"` | 숫자 6자리 |
| 버튼 `button[type=submit]` | `확인` | 가입 요청 트리거 |

## 4. 흐름 · 상태

```
[이메일 입력 단계]
  email 입력 → "인증번호 받기"
     └ POST /auth/otp/issue {email}
          ├ 200 → OTP 입력 단계로 전환(데모: 응답 code를 힌트로 노출)
          └ 4xx → 오류 메시지

[OTP 입력 단계]  ← 캐노니컬 스냅샷 영역
  6자리 입력 → "확인"
     └ POST /auth/signup {email, code}
          ├ 201 created  → "가입 완료" 표시 (replay=true면 "이미 가입됨")
          └ 422 rejected → reason별 메시지(no_otp/locked/expired/wrong_code)
          └ 400          → 입력 형식 오류 메시지
```

거부 사유 → 사용자 메시지 매핑:

| reason | 메시지 |
| --- | --- |
| `wrong_code` | 인증번호가 올바르지 않습니다. |
| `expired` | 인증번호가 만료되었습니다. 다시 받아주세요. |
| `locked` | 5회 초과로 잠겼습니다. 인증번호를 다시 받아주세요. |
| `no_otp` | 먼저 인증번호를 발급받아 주세요. |

## 5. 비범위 · 잔여 리스크

- 실 브라우저 픽셀/반응형 정합은 미검증 — 결정적 HTML 스냅샷 정합(`SignupScreenParityTest`)으로 대체.
- 데모 편의로 발급 code를 화면 힌트로 노출 — 실서비스에서는 메일/SMS 채널로만 전달.
- 인메모리 저장 — 프로세스 재시작 시 OTP·계정 소실(데모 한정).
