# -*- coding: utf-8 -*-
"""수용기준(Acceptance Criteria)을 코드로 옮긴 '채점기'다.

이 파일이 3강 데모의 심장이다. 같은 기능을 명세 없이(바이브) 짜든 명세대로(SDD)
짜든, '맞게 동작하는가'는 결국 누군가 정해 둔 기준으로 판정해야 한다. 그 기준을
사람의 눈이 아니라 코드로 박아 둔 것이 아래 CRITERIA와 run()이다.

설계 원칙 두 가지를 일부러 지킨다.

1) 같은 기준을 양쪽에 똑같이 돌린다.
   바이브 구현이든 SDD 구현이든 run()은 동일한 AC-1~AC-4를 그대로 적용한다.
   채점기가 한쪽에만 유리하지 않으므로, 점수 차이는 오직 '구현이 그 기준을
   만족하느냐'에서만 나온다. 이것이 대조(contrast)를 공정하게 만든다.

2) 한 기준이 터져도 나머지는 끝까지 채점한다.
   바이브 코드는 메서드나 속성이 아예 없을 수 있다(예: 잠금 로직이 없어
   AttributeError). 그래서 각 기준을 try/except로 감싸, 예외가 나면 그 기준만
   '불합격(False)'으로 처리하고 다음 기준으로 넘어간다. 라이브 데모에서 학습자가
   만든 어떤 코드를 넣어도 채점기가 죽지 않고 점수를 매긴다.

시간은 실제 시계가 아니라 정수 t(초)로 주입한다. sleep 없이도 '만료'를 결정적으로
재현하기 위해서다. 같은 입력이면 항상 같은 점수가 나온다(멱등).
"""

# 사람이 읽는 기준 이름과 한 줄 설명. 슬라이드·콘솔 출력에 그대로 쓴다.
CRITERIA = [
    ("AC-1 정상 발급·검증", "유효한 OTP로 가입하면 성공한다"),
    ("AC-2 만료 OTP 거부", "발급 후 TTL(300초)이 지난 OTP는 거부한다"),
    ("AC-3 5회 오류 잠금", "5회 연속 틀리면 정답을 넣어도 거부한다(무차별 대입 차단)"),
    ("AC-4 재요청 멱등", "같은 사람이 두 번 가입해도 계정은 1개만 생긴다"),
]


# --- 기준별 판정 함수 --------------------------------------------------------
# make: () -> OTP 구현 인스턴스. 각 기준마다 새 인스턴스로 시작해, 앞 기준이 남긴
# 상태(잠금·생성 기록)가 다음 기준에 새지 않도록 격리한다.

def _ac1_happy(make):
    # 0초에 발급한 코드로 10초에 가입 → 정상 흐름이니 성공해야 한다.
    o = make()
    o.issue("a@x.com", t=0)
    return o.signup("a@x.com", "123456", t=10) is True


def _ac2_expiry(make):
    # 999초는 TTL 300초를 한참 넘긴 시점 → 만료로 거부되어야 정상이다.
    o = make()
    o.issue("a@x.com", t=0)
    return o.signup("a@x.com", "123456", t=999) is False


def _ac3_lock(make):
    # 일부러 5회 오답을 낸 뒤, '정답'을 넣어도 거부되어야 한다(무차별 대입 차단).
    o = make()
    o.issue("a@x.com", t=0)
    for _ in range(5):
        o.signup("a@x.com", "000000", t=10)
    return o.signup("a@x.com", "123456", t=10) is False


def _ac4_idempotent(make):
    # 같은 사람이 두 번 가입 요청해도 계정은 1건이어야 한다.
    o = make()
    o.issue("a@x.com", t=0)
    o.signup("a@x.com", "123456", t=10)
    o.signup("a@x.com", "123456", t=10)
    return len(o.created) == 1


# CRITERIA 순서와 1:1로 맞춘 판정 함수 목록.
_CHECKS = [_ac1_happy, _ac2_expiry, _ac3_lock, _ac4_idempotent]


def run(make):
    """make로 만든 구현을 AC-1~AC-4로 채점해 {기준이름: True/False}를 돌려준다.

    각 판정은 try/except로 감싸므로, 구현에 메서드·속성이 빠져 예외가 나도 그
    기준만 False가 되고 채점은 끝까지 진행된다(라이브 데모 안전장치).
    """
    result = {}
    for (name, _desc), check in zip(CRITERIA, _CHECKS):
        try:
            result[name] = bool(check(make))
        except Exception:
            # 메서드 미구현·속성 없음 등 → '그 기준을 만족하지 못함'으로 본다.
            result[name] = False
    return result
