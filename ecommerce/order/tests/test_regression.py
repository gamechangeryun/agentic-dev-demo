# -*- coding: utf-8 -*-
"""AC-R: order 기능 추가 후 auth 서비스 회귀 없음 확인."""
import pathlib
import subprocess
import sys

AUTH_DIR = pathlib.Path(__file__).resolve().parents[3] / "auth"


def test_auth_proof_still_passes():
    """auth proof가 여전히 exit 0으로 통과하는지 확인."""
    if not AUTH_DIR.exists():
        import pytest; pytest.skip("auth 서비스 디렉터리 없음")
    result = subprocess.run(
        [sys.executable, "proof/run_proof.py"],
        capture_output=True,
        cwd=str(AUTH_DIR),
    )
    assert result.returncode == 0, f"auth 회귀 발생:\n{result.stdout.decode()}"
