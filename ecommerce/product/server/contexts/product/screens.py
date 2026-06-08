# -*- coding: utf-8 -*-
"""상품 화면 렌더러 — 10강 실습: render() 함수가 스냅샷과 일치하도록 구현하세요.

UI parity 게이트: python3 sdd/99_toolchain/01_automation/run_ui_parity.py
"""


def render(screen_name: str) -> str:
    if screen_name == "product_list":
        raise NotImplementedError("product_list 화면을 구현하세요.")
    raise ValueError(f"알 수 없는 screen: {screen_name}")
