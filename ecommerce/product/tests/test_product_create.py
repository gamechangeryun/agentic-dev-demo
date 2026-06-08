def test_create_returns_active_status(product_svc):
    r = product_svc.create("A", 1000); assert r.status=="created" and r.product.status=="ACTIVE"
def test_create_rejects_zero_price(product_svc):
    assert product_svc.create("X", 0).status == "rejected"
def test_create_rejects_negative_price(product_svc):
    assert product_svc.create("X", -1).status == "rejected"
def test_product_idempotent(product_svc):
    r1 = product_svc.create("Y", 100, idem_key="k1"); r2 = product_svc.create("Y", 100, idem_key="k1")
    assert r1.product.product_id == r2.product.product_id and r2.replay
