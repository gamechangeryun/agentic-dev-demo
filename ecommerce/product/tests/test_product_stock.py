def test_add_stock(product_svc):
    r = product_svc.create("P", 100, 5); assert product_svc.add_stock(r.product.product_id, 10).stock_quantity == 15
def test_reduce_stock(product_svc):
    r = product_svc.create("P", 100, 10); assert product_svc.reduce_stock(r.product.product_id, 3).stock_quantity == 7
def test_reduce_insufficient(product_svc):
    r = product_svc.create("P", 100, 3); assert product_svc.reduce_stock(r.product.product_id, 5).reason == "insufficient_stock"
def test_archive(product_svc):
    r = product_svc.create("P", 100); product_svc.archive(r.product.product_id)
    assert product_svc.get(r.product.product_id).status == "ARCHIVED"
def test_archived_blocks_stock(product_svc):
    r = product_svc.create("P", 100); pid = r.product.product_id
    product_svc.archive(pid); assert product_svc.add_stock(pid, 1).reason == "product_archived"
