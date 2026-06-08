def test_forward_transitions(order_svc):
    r = order_svc.create(["a"], 1000); oid = r.order.order_id
    assert order_svc.process(oid).status == "ok"
    assert order_svc.fulfill(oid).status == "ok"
    assert order_svc.get(oid).status == "FULFILLED"

def test_cancel_from_created(order_svc):
    r = order_svc.create(["a"], 1000)
    assert order_svc.cancel(r.order.order_id).status == "ok"

def test_cancel_from_processing(order_svc):
    r = order_svc.create(["a"], 1000); order_svc.process(r.order.order_id)
    assert order_svc.cancel(r.order.order_id).status == "ok"

def test_cancel_fulfilled_rejected(order_svc):
    r = order_svc.create(["a"], 1000); oid = r.order.order_id
    order_svc.process(oid); order_svc.fulfill(oid)
    assert order_svc.cancel(oid).reason == "cannot_cancel_fulfilled"

def test_cancelled_is_terminal(order_svc):
    r = order_svc.create(["a"], 1000); oid = r.order.order_id
    order_svc.cancel(oid)
    assert order_svc.process(oid).status == "rejected"
