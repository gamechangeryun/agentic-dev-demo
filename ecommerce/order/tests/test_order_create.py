def test_create_returns_created_status(order_svc):
    r = order_svc.create(items=["item-a"], total_amount=10000)
    assert r.status == "created" and r.order.status == "CREATED"

def test_create_rejects_zero_amount(order_svc):
    assert order_svc.create(items=[], total_amount=0).status == "rejected"

def test_create_rejects_negative_amount(order_svc):
    assert order_svc.create(items=[], total_amount=-1).status == "rejected"

def test_order_idempotent(order_svc):
    r1 = order_svc.create(items=["x"], total_amount=500, idem_key="k1")
    r2 = order_svc.create(items=["x"], total_amount=500, idem_key="k1")
    assert r1.order.order_id == r2.order.order_id and r2.replay
