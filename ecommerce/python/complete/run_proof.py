#!/usr/bin/env python3
"""결정적 proof 게이트입니다. pytest 로 핵심 수용기준을 모두 검증합니다.

pytest 가 현재 인터프리터에 없으면, 이 디렉토리의 로컬 ``.venv`` 에 한 번만
설치한 뒤 그 인터프리터로 테스트를 재실행합니다. 시스템 파이썬을 더럽히지 않고
어느 환경에서나 같은 결과로 수렴하게 만드는 자급 부트스트랩입니다.

사용법:
    python3 run_proof.py
"""

from __future__ import annotations

import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
VENV_DIR = os.path.join(HERE, ".venv")


def _venv_python() -> str:
    if os.name == "nt":
        return os.path.join(VENV_DIR, "Scripts", "python.exe")
    return os.path.join(VENV_DIR, "bin", "python")


def _has_pytest(python: str) -> bool:
    return subprocess.run(
        [python, "-c", "import pytest"],
        cwd=HERE,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    ).returncode == 0


def _ensure_venv_pytest() -> str:
    python = _venv_python()
    if not os.path.exists(python):
        print("[proof] pytest 가 없어 로컬 .venv 를 만듭니다(최초 1회).")
        subprocess.run([sys.executable, "-m", "venv", VENV_DIR], cwd=HERE, check=True)
    if not _has_pytest(python):
        print("[proof] .venv 에 pytest 를 설치합니다.")
        subprocess.run(
            [python, "-m", "pip", "install", "-q", "-r", "requirements.txt"],
            cwd=HERE,
            check=True,
        )
    return python


def main() -> int:
    python = sys.executable
    if not _has_pytest(python):
        python = _ensure_venv_pytest()
    print(f"[proof] pytest 실행: {python}")
    result = subprocess.run([python, "-m", "pytest", "-q"], cwd=HERE)
    return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
