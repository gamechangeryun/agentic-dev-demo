# -*- coding: utf-8 -*-
"""AC-R: product 기능 추가 후 auth·order 서비스 회귀 없음 확인."""
import pathlib
import subprocess
import sys

AUTH_DIR = pathlib.Path(__file__).resolve().parents[3] / "auth"
ORDER_DIR = pathlib.Path(__file__).resolve().parents[2] / "order"


def test_auth_proof_still_passes():
    if not AUTH_DIR.exists():
        import pytest; pytest.skip("auth 서비스 디렉터리 없음")
    result = subprocess.run(
        [sys.executable, "proof/run_proof.py"],
        capture_output=True,
        cwd=str(AUTH_DIR),
    )
    assert result.returncode == 0, f"auth 회귀:\n{result.stdout.decode()}"


def test_order_proof_still_passes():
    if not ORDER_DIR.exists():
        import pytest; pytest.skip("order 서비스 디렉터리 없음")
    result = subprocess.run(
        [sys.executable, "-m", "pytest", "tests/", "-x", "-q"],
        capture_output=True,
        cwd=str(ORDER_DIR),
    )
    assert result.returncode == 0, f"order 회귀:\n{result.stdout.decode()}"
