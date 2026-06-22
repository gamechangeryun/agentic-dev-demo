# 회원가입(Signup) 기능 명세서

> 01_planning · feature. `auth_feature_spec.md`(EARS 가드레일)와 `00_sources/02_requirements/auth-signup.md`(요구사항 원문)를 구체화한 회원가입 전체 명세.
> 강의 데모용 가상 기능 — 실재 개인정보·실명 없음. 이메일은 불투명 예시값(`a@x.com`)만 사용.

## 1. 목적 · 범위

- **목적**: 이메일 OTP(6자리) 검증을 통과한 사용자만 계정을 생성한다.
- **범위(In)**: OTP 발급, OTP 검증 후 가입, TTL 만료, 시도 제한 잠금, 멱등 가입.
- **범위(Out)**: 비밀번호 로그인 자격증명 발급, 소셜 로그인, 실제 메일 발송(데모는 응답에 code 포함), 영속 DB(인메모리 저장).
- **불변(회귀)**: 기존 로그인 흐름(`POST /auth/login`)은 회원가입 추가로 깨지지 않는다.

## 2. 인수 기준 매핑 (EARS)

본 명세는 `auth_feature_spec.md`의 AC를 구현 단위로 구체화한다.

| AC | 요지 | 구현 위치 |
| --- | --- | --- |
| AC-1 | (email, purpose)에 6자리 OTP 발급 + TTL 300초 | `OtpService.issue` |
| AC-2 | 유효 OTP 입력 시 계정 생성 | `SignupService.signup` → `AccountRepository.save` |
| AC-3 | 5회 연속 오입력 시 OTP 잠금 | `OtpService.verify`(`incrementAttempts`/`lock`) |
| AC-4 | TTL 300초 경과 시 만료 거부 | `OtpService.verify`(`expired`) |
| AC-5 | 동일 사용자 재요청 시 멱등(중복 계정 금지) | `IdempotencyStore.issueOnce` |
| AC-6 | signup_otp 화면이 디자인 스냅샷과 일치 | `04_verify/02_screen/platform/signup.md` |

## 3. 도메인 정책 (설정값)

`application.yml` 기준 — 모든 값은 외부화되어 환경별 오버라이드 가능.

| 정책 | 키 | 기본값 |
| --- | --- | --- |
| OTP 자릿수 | (고정) | 6자리 숫자(`%06d`) |
| OTP 유효시간(TTL) | `auth.otp.ttl-seconds` | `300`(5분) |
| 최대 시도 횟수 | `auth.otp.max-attempts` | `5` |
| 기본 purpose | (DTO) | `signup` |
| OTP 키 | (내부) | `email + " " + purpose` |
| 멱등 키 | (내부) | `idemKey` 우선, 없으면 `sha256({"email": "<email>"})` |

## 4. API 계약

### 4.1 OTP 발급 — `POST /auth/otp/issue` (AC-1)

요청:

```json
{ "email": "a@x.com", "purpose": "signup" }   // purpose 생략 시 "signup"
```

응답 `200 OK` (데모 편의상 발급 code 포함):

```json
{ "email": "a@x.com", "purpose": "signup", "code": "012345" }
```

### 4.2 회원가입 — `POST /auth/signup` (AC-2·3·4·5)

요청:

```json
{ "email": "a@x.com", "code": "012345", "purpose": "signup", "idemKey": null }
```

- `email`: `@NotBlank @Email`
- `code`: `@NotBlank @Pattern(\d{6})` — 6자리 숫자
- `purpose`: 생략 시 `signup`
- `idemKey`: 생략 시 email 기반 sha256 멱등 키 자동 생성

성공 응답 `201 Created`:

```json
{ "status": "created", "reason": "ok", "email": "a@x.com",
  "idempotencyKey": "<sha256>", "replay": false }
```

- 동일 키 재요청 시: `201 Created`, `replay: true`, 계정은 추가 생성되지 않음(AC-5).

거부 응답 `422 Unprocessable Entity`:

```json
{ "status": "rejected", "reason": "<reason>", "email": "a@x.com",
  "idempotencyKey": "", "replay": false }
```

입력 검증 실패 `400 Bad Request` (`ApiExceptionHandler`):

```json
{ "status": "rejected", "reason": "code: OTP는 6자리 숫자여야 합니다" }
```

## 5. 거부 사유 코드 (reason)

`OtpService.verify`가 반환하고 `SignupResult.rejected`로 전달된다.

| reason | 발생 조건 | AC |
| --- | --- | --- |
| `no_otp` | 발급 이력 없는 (email, purpose) 검증 시도 | — |
| `locked` | 5회 오입력으로 잠긴 OTP | AC-3 |
| `expired` | 발급 후 TTL(300초) 초과 | AC-4 |
| `wrong_code` | code 불일치(시도 누적, 5회째에 잠금 전이) | AC-3 |

## 6. 처리 흐름

```
issue ── (email,purpose) → 6자리 code 발급, issuedAt 기록, TTL 시작
  │
signup ── verify(email, code, purpose)
  ├─ 레코드 없음          → rejected(no_otp)       422
  ├─ 잠김                 → rejected(locked)       422
  ├─ TTL 초과             → rejected(expired)      422
  ├─ code 불일치          → attempts++             422 (rejected wrong_code)
  │                          attempts≥5 → lock
  └─ 검증 성공
        └─ idem.issueOnce(key)
             ├─ 최초    → accounts.save(email), replay=false   201 created
             └─ 재요청  → 저장된 결과 반환, replay=true          201 created
```

## 7. 검증 (테스트 매핑)

| AC | 검증 |
| --- | --- |
| AC-1·AC-2 | `AuthFlowTest` issue→signup 성공 경로 |
| AC-3 | wrong_code 5회 → `locked` 전이 |
| AC-4 | TTL 경과 → `expired` |
| AC-5 | 동일 입력 재요청 → `replay=true`, 계정 1개 |
| AC-6 | `run_ui_parity.py` → signup_otp parity 1/1 PASS |
| 회귀 | `POST /auth/login` 정상 동작 유지 |

## 8. 잔여 리스크 / 비범위 메모

- 인메모리 저장(`ConcurrentHashMap`): 프로세스 재시작 시 OTP·계정·멱등 기록 소실 — 데모 한정, 영속화는 비범위.
- 데모 편의로 발급 code를 응답에 노출 — 실서비스에서는 메일/SMS 채널로만 전달해야 함.
- 잠금 해제·재발급(쿨다운) 정책 미정의 — 현재는 새 `issue`로 레코드를 덮어써야 재시도 가능.
- 멱등 키 기본값이 email만 해싱 — 동일 email의 의도적 재가입과 우발적 재요청을 구분하지 않음.
