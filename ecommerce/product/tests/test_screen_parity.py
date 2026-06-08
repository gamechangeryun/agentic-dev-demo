import pathlib
SNAPSHOT = pathlib.Path("sdd/04_verify/10_test/ui_parity/product_list.html")
def test_product_list_html_exists(): assert SNAPSHOT.exists()
def test_product_list_parity():
    from server.contexts.product.screens import render
    assert SNAPSHOT.read_text().strip() == render("product_list").strip()
