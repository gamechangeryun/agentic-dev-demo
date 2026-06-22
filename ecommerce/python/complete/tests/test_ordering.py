"""주문 상태머신 단위 테스트입니다. 자바 OrderTest 를 옮긴 것입니다."""

import pytest

from shop.ordering.domain import Order, OrderLine, OrderStatus
from shop.shared import DomainException, ErrorCode, Money


def _new_order() -> Order:
    line = OrderLine("p1", "노트북", Money.won(1000), 2)
    return Order.create("ord_1", [line], ["resv_1"])


def test_ac2_rejects_zero_amount():
    """AC-2 총액 0원 이하 주문은 생성 단계에서 거부한다."""
    free = OrderLine("p1", "사은품", Money.won(0), 1)
    with pytest.raises(DomainException) as exc:
        Order.create("ord_x", [free], [])
    assert exc.value.code == ErrorCode.INVALID_AMOUNT


def test_ac3_forward_transitions():
    """AC-3 CREATED → PAID → FULFILLED 정방향으로 전환한다."""
    o = _new_order()
    assert o.status == OrderStatus.CREATED
    o.mark_paid("pay_1")
    assert o.status == OrderStatus.PAID
    o.fulfill()
    assert o.status == OrderStatus.FULFILLED


def test_ac4_cannot_fulfill_before_paid():
    """AC-4 결제 전 주문은 이행할 수 없다."""
    o = _new_order()
    with pytest.raises(DomainException) as exc:
        o.fulfill()
    assert exc.value.code == ErrorCode.PAYMENT_REQUIRED


def test_ac5_cannot_cancel_fulfilled():
    """AC-5 이행 완료된 주문은 취소할 수 없다."""
    o = _new_order()
    o.mark_paid("pay_1")
    o.fulfill()
    with pytest.raises(DomainException) as exc:
        o.cancel()
    assert exc.value.code == ErrorCode.INVALID_STATE_TRANSITION


def test_cancel_paid_signals_refund():
    """결제된 주문 취소는 환불 필요 신호(True)를 돌려준다."""
    o = _new_order()
    o.mark_paid("pay_1")
    assert o.cancel() is True
    assert o.status == OrderStatus.CANCELLED


def test_double_pay_transition_blocked():
    """이미 PAID 인 주문은 다시 markPaid 할 수 없다(상태 가드)."""
    o = _new_order()
    o.mark_paid("pay_1")
    with pytest.raises(DomainException) as exc:
        o.mark_paid("pay_2")
    assert exc.value.code == ErrorCode.INVALID_STATE_TRANSITION
