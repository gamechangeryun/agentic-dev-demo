# 3강 데모 — Claude Code로 바이브코딩 vs SDD 직접 대조

같은 기능('회원가입 OTP')을 **Claude Code로 직접 두 번** 만들어 본다. 한 번은 명세 없이
(바이브코딩), 한 번은 명세(`spec.md`)를 주고. 그리고 **같은 채점기(`grade.py`)** 로 본인이
점수를 비교한다. 보통 바이브는 1/4, SDD는 4/4가 나온다.

핵심은 코드 실력이 아니라 **'무엇을 맞는 동작으로 정의했는가'** 다.

## Claude Code 설치 (윈도우 · 맥)

| | Windows · PowerShell | macOS · 터미널 |
| --- | --- | --- |
| 설치(권장) | `irm https://claude.ai/install.ps1 \| iex` | `curl -fsSL https://claude.ai/install.sh \| bash` |
| 대안 | `winget install Anthropic.ClaudeCode` | `brew install --cask claude-code` |
| 실행 | `claude` | `claude` |

첫 실행 시 로그인은 Claude 구독(claude.com) 또는 Anthropic Console API 키(console.anthropic.com) 중 택1.
윈도우는 CMD가 아닌 **PowerShell**에서. (출처: code.claude.com/docs)

## 준비
```bash
git clone https://github.com/say828/agentic-dev-demo.git
cd agentic-dev-demo/sdd-contrast
# 의존성 없음(파이썬 표준 라이브러리만). Claude Code만 있으면 된다.
```
채점기 `acceptance.py`·`grade.py`와 명세 `spec.md`는 **그대로 둔다**.
학습자가 만드는 건 `otp.py` 하나다(.gitignore 처리 — 마음껏 지우고 다시 만들어도 됨).

## 라운드 1 — 명세 없이 (바이브코딩)
Claude Code에 **이대로** 입력한다(수용기준은 일부러 주지 않는다):

> 회원가입 OTP 기능 만들어줘. `otp.py`에 `Otp` 클래스로,
> 메서드는 `issue(email, t=0)`·`verify(email, code, t=0)`·`signup(email, code, t=0)`.
> 가입된 사용자는 `created`에 담아줘.
> 단, 폴더의 `spec.md`·`impls.py`는 보지 말고 이 설명만 보고 짜.

다 만들어졌으면 채점한다:
```bash
python3 grade.py
#   AC-1 정상 발급·검증   PASS
#   AC-2 만료 OTP 거부    FAIL
#   AC-3 5회 오류 잠금    FAIL
#   AC-4 재요청 멱등      FAIL
#   점수: 1/4
```
happy path는 '돌아간다'. 그런데 만료·잠금·멱등은 **아무도 말하지 않아 그냥 빠졌다**.
게다가 검증 게이트가 없으면 이 빈틈이 그대로 배포된다.

## 라운드 2 — 명세대로 (SDD)
먼저 `spec.md`의 수용기준(AC-1~4)을 연다. 그리고 Claude Code에 입력한다:

> `spec.md`의 수용기준을 모두 만족하도록 `otp.py`를 다시 구현하고,
> `python3 grade.py`가 4/4가 나올 때까지 고쳐줘.

다시 채점한다:
```bash
python3 grade.py
#   AC-1 PASS · AC-2 PASS · AC-3 PASS · AC-4 PASS
#   점수: 4/4
```
명세가 만료·잠금·멱등을 '맞는 동작'으로 정의했고, 채점기가 그것을 강제했다.

## 교훈
**'돌아간다'와 '맞다'는 다르다.** 명세(spec)가 '무엇이 맞는가'를 정의하고, 게이트(`grade.py`)가 검증한다.
SDD 본 데모(S05~S13)에서 이 게이트가 실제 프로젝트 규모로 작동한다(`auth` proof 10/10, `finance` 14/14).

## 라이브가 막힐 때 (강사 폴백)
`otp.py` 없이도 '기대 결과(양쪽 한 번에)'를 보고 싶으면:
```bash
python3 contrast.py     # 참고 정답으로 vibe 1/4 vs SDD 4/4 재현 (결정적·의존성 없음)
```

| 수용기준 | 명세 없이(vibe) | 명세 기반(SDD) |
| --- | --- | --- |
| AC-1 정상 발급·검증 | PASS | PASS |
| AC-2 만료 OTP 거부 | **FAIL** (만료 코드도 통과) | PASS |
| AC-3 5회 오류 잠금 | **FAIL** (무한 시도) | PASS |
| AC-4 재요청 멱등 | **FAIL** (중복 가입) | PASS |

> 코드·기준은 강의용 축약 예시. 핵심은 "명세 유무가 결과를 가른다"이다.
