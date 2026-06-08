import pathlib
SNAPSHOT = pathlib.Path("sdd/04_verify/10_test/ui_parity/order_list.html")

def test_order_list_html_exists(): assert SNAPSHOT.exists()

def test_order_list_parity():
    from server.contexts.order.screens import render
    assert SNAPSHOT.read_text().strip() == render("order_list").strip()
