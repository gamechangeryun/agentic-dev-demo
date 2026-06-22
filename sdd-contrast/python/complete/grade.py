# -*- coding: utf-8 -*-
"""학습자가 만든 otp.py를 같은 수용기준으로 채점한다 — 라이브 데모의 핵심 도구.

이 파일은 '심판'이다. 라운드마다 학습자가 Claude Code로 otp.py(Otp 클래스)를 만든
직후 이걸 돌리면, AC-1~AC-4를 채점해 점수를 보여 준다.

  $ python3 grade.py
    ...
    점수: 1/4    # 명세 없이(바이브) 짠 경우 — 보통 happy path만 통과
    점수: 4/4    # spec.md대로 짠 경우 — 만료·잠금·멱등까지 통과

채점 기준(acceptance.py)은 두 라운드가 똑같다. 점수가 갈리는 건 오직 '명세가
있었느냐'뿐이다. 그게 이 데모로 보여 주려는 한 가지다: 명세가 '무엇이 맞는가'를
정의하고, 게이트가 그것을 검증한다.

otp.py가 아직 없으면(=아직 안 짰으면) 친절히 알려 주고 멈춘다. 양쪽 기대 결과를
한 번에 보고 싶으면(멱등 검증·강사 폴백) grade.py 대신 contrast.py를 쓴다.
"""
import importlib
import sys

from acceptance import CRITERIA, run


def main():
    # 학습자가 방금 만든 otp.py에서 Otp 클래스를 가져온다.
    try:
        otp = importlib.import_module("otp")
        Otp = getattr(otp, "Otp")
    except (ModuleNotFoundError, AttributeError):
        print("otp.py에 Otp 클래스가 아직 없습니다.")
        print("먼저 Claude Code로 구현하세요 — README.md의 프롬프트를 그대로 붙여넣으면 됩니다.")
        return 2

    # 같은 채점기로 학습자 코드를 AC-1~AC-4 판정한다.
    result = run(lambda: Otp())
    for name, _desc in CRITERIA:
        print(f"  {name:<20} {'PASS' if result[name] else 'FAIL'}")

    score = sum(result.values())
    total = len(CRITERIA)
    print(f"\n점수: {score}/{total}")

    if score < total:
        # 어떤 기준이 빠졌는지 짚어 준다 — '명세하지 않은 것이 빠진다'를 체감하게.
        missing = [name for name, _ in CRITERIA if not result[name]]
        print("빠진 기준: " + ", ".join(missing))
        print("→ 명세에 없던 동작은 구현에서도 빠집니다. spec.md를 주고 다시 시켜 보세요.")
    else:
        print("→ 명세가 정의한 '맞는 동작'을 모두 만족했습니다.")

    # happy path 하나도 못 넘기면(=otp.py가 비정상) 비정상 종료로 구분.
    return 0 if score == total else 1


if __name__ == "__main__":
    sys.exit(main())
