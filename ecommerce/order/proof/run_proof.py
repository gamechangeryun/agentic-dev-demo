# -*- coding: utf-8 -*-
"""결정적 proof 게이트: pytest(AC-1~AC-8 + 회귀)를 돌려 tmp/proof-results.json 산출.

contract.json 의 `proof` 가 가리키는 명령. exit 0 = 전 게이트 통과.
"""
import json
import os
import pathlib
import sys
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[1]

# venv가 있으면 자동으로 venv python으로 재실행합니다.
_VENV_PY = ROOT / ".venv/bin/python3"
if _VENV_PY.exists() and pathlib.Path(sys.executable).resolve() != _VENV_PY.resolve():
    os.execv(str(_VENV_PY), [str(_VENV_PY)] + sys.argv)

import subprocess  # noqa: E402
TMP = ROOT / "tmp"


def _pytest_cmd():
    venv_pytest = ROOT / ".venv/bin/pytest"
    if venv_pytest.exists():
        return [str(venv_pytest)]
    return [sys.executable, "-m", "pytest"]


def main():
    TMP.mkdir(exist_ok=True)
    junit = TMP / "junit.xml"
    proc = subprocess.run(
        _pytest_cmd() + ["-q", "--no-header",
                         f"--junitxml={junit}", str(ROOT / "tests")],
        cwd=str(ROOT), capture_output=True, text=True,
    )
    out = (proc.stdout or "") + (proc.stderr or "")

    tests, passed, failed = [], 0, 0
    if junit.exists():
        root = ET.parse(junit).getroot()
        for tc in root.iter("testcase"):
            ok = tc.find("failure") is None and tc.find("error") is None
            tests.append({
                "name": tc.get("name"),
                "file": tc.get("classname"),
                "time_s": round(float(tc.get("time", 0)), 4),
                "status": "PASS" if ok else "FAIL",
            })
            passed += int(ok)
            failed += int(not ok)

    result = {
        "gate": "pytest",
        "feature": "order lifecycle (주문 라이프사이클)",
        "acceptance": ["AC-1", "AC-2", "AC-3", "AC-4", "AC-5", "AC-6", "AC-7", "AC-8", "회귀"],
        "exit_code": proc.returncode,
        "total": len(tests),
        "passed": passed,
        "failed": failed,
        "status": "PASS" if proc.returncode == 0 and failed == 0 else "FAIL",
        "tests": tests,
    }
    (TMP / "proof-results.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(out.rstrip())
    print(f"[proof] {result['status']} · {passed}/{len(tests)} passed "
          f"→ tmp/proof-results.json")
    return proc.returncode


if __name__ == "__main__":
    sys.exit(main())
