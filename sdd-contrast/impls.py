# -*- coding: utf-8 -*-
"""참고용 두 구현 — '명세 없이(vibe)'와 '명세 기반(SDD)'의 전형적인 결과물.

⚠ 이 파일은 사실상 '정답지'다. 라이브 데모에서 학습자는 이 코드를 보지 않고,
Claude Code로 직접 otp.py를 만든다(README.md 참고). 이 파일이 쓰이는 곳은 둘뿐이다.

  - contrast.py: 양쪽을 한 번에 채점해 '기대 결과(vibe 1/4 vs SDD 4/4)'를 재현한다.
                 멱등 검증과 강사 폴백(라이브가 막혔을 때)에 쓴다.
  - 학습자가 만든 otp.py와 비교해 볼 때의 레퍼런스.

두 클래스는 채점기(acceptance.py)가 호출하는 같은 인터페이스를 따른다.

  issue(email, t=0)        -> code   OTP 발급
  verify(email, code, t=0) -> bool   코드가 맞고 유효하면 True
  signup(email, code, t=0) -> bool   검증을 통과하면 가입시키고 True
  created                            가입된 사용자 모음 (AC-4 멱등 채점에 사용)

두 구현의 차이는 코드 실력이 아니라 '무엇을 맞는 동작으로 정의했는가'다. 시간은
정수 t(초)로 주입해 결정적으로 동작한다(실시간·난수 비의존).
"""


class VibeOtp:
    """명세 없이 'happy path'만 짠 전형적 결과 — 채점하면 보통 1/4.

    '회원가입 OTP 만들어줘'라는 한 마디만 듣고 짜면 대개 이렇게 나온다. 코드가
    일치하는지만 보고 바로 가입시킨다. 만료·잠금·멱등은 '누가 말해 주지 않아'
    통째로 빠진다. 그래서 AC-1(정상)만 통과하고 AC-2·3·4는 떨어진다.

    못 짜서가 아니다. '무엇이 맞는지'를 아무도 정의하지 않았기 때문이다.
    """
    TTL = 300

    def __init__(self):
        self.codes = {}
        self.created = []          # list라 같은 사람을 두 번 append → 멱등이 깨진다

    def issue(self, email, t=0):
        self.codes[email] = ("123456", t)
        return "123456"

    def verify(self, email, code, t=0):
        rec = self.codes.get(email)
        # 코드 일치만 본다 — 만료(t)도, 시도 횟수도 검사하지 않는다.
        return bool(rec) and rec[0] == code

    def signup(self, email, code, t=0):
        if not self.verify(email, code, t):
            return False
        self.created.append(email)  # 중복 가입을 막는 장치가 없다.
        return True


class SddOtp:
    """EARS 수용기준대로 짠 결과 — 채점하면 4/4.

    spec.md의 AC-2(만료)·AC-3(잠금)·AC-4(멱등)가 그대로 코드로 내려온 모습이다.
    바이브 코드와 실력 차이가 아니라, '무엇이 맞는지'를 명세가 먼저 정의해 줬다는
    차이뿐이다. 같은 채점기로 전부 통과한다.
    """
    TTL = 300
    MAX_ATTEMPTS = 5

    def __init__(self):
        self.codes = {}
        self.created = set()        # set이라 같은 사람을 두 번 넣어도 1건 (AC-4 멱등)

    def issue(self, email, t=0):
        # 발급 시각(t)·실패 횟수·잠금 여부를 함께 들고 다닌다.
        self.codes[email] = {"code": "123456", "t": t, "fails": 0, "locked": False}
        return "123456"

    def verify(self, email, code, t=0):
        r = self.codes.get(email)
        if not r or r["locked"]:                 # 발급 안 됐거나 이미 잠긴 상태
            return False
        if t - r["t"] > self.TTL:                # AC-2: 발급 후 TTL 초과 → 만료
            return False
        if code != r["code"]:                    # 오답이면 실패 횟수를 센다
            r["fails"] += 1
            if r["fails"] >= self.MAX_ATTEMPTS:  # AC-3: 5회째 오답이면 잠금
                r["locked"] = True
            return False
        return True

    def signup(self, email, code, t=0):
        if not self.verify(email, code, t):
            return False
        self.created.add(email)                  # AC-4: set이라 자동으로 멱등
        return True
