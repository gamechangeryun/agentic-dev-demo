# -*- coding: utf-8 -*-
"""UI parity 게이트: product_list 화면을 스냅샷과 대조합니다.

exit 0 = 일치 (통과), exit 1 = 불일치 (실패).
"""
import os
import pathlib
import sys

# venv가 있으면 자동으로 venv python으로 재실행합니다.
_ROOT = pathlib.Path(__file__).resolve().parents[3]
_VENV_PY = _ROOT / ".venv/bin/python3"
if _VENV_PY.exists() and pathlib.Path(sys.executable).resolve() != _VENV_PY.resolve():
    os.execv(str(_VENV_PY), [str(_VENV_PY)] + sys.argv)

sys.path.insert(0, str(_ROOT))

from server.contexts.product import screens  # noqa: E402

SNAP = _ROOT / "sdd/04_verify/10_test/ui_parity/product_list.html"


def main():
    want = SNAP.read_text(encoding="utf-8").strip()
    try:
        got = screens.render("product_list").strip()
    except NotImplementedError as e:
        print("화면: product_list")
        print(f"RESULT: ui_parity 0/1  ·  FAIL  ({e})")
        return 1
    ok = want == got
    print("화면: product_list")
    print(f"스냅샷: {SNAP.relative_to(_ROOT)}")
    print("UI parity (스냅샷 대조):", "일치" if ok else "불일치")
    if not ok:
        print("  기대값:", repr(want[:120]))
        print("  실제값:", repr(got[:120]))
    print(f"RESULT: ui_parity {'1/1' if ok else '0/1'}  ·  {'PASS' if ok else 'FAIL'}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
