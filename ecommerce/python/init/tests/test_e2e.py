"""전체 여정 E2E 테스트입니다. 자바 ShopE2ETest 9개 시나리오를 옮긴 것입니다.

HTTP/MockMvc 대신, Spring DI 를 대신하는 ``ShopApp`` 조립기를 통해 서비스
계층을 직접 호출합니다. 검증하는 비즈니스 규칙은 자바 E2E 와 동일합니다.
"""

import pytest

from shop import ShopApp
from shop.catalog.domain import ProductStatus
from shop.ordering.domain import OrderStatus
from shop.payment.domain import PaymentStatus
from shop.shared import DomainException, ErrorCode


@pytest.fixture
def app():
    return ShopApp()


def test_e2e1_full_journey(app):
    """E2E-1 전체 여정: 상품 → 장바구니 → 체크아웃 → 결제 → 이행."""
    product = app.catalog.create("노트북", 1_000_000, 5, None)
    cart = app.carts.create()
    app.carts.add_item(cart.id, product.id, 2)

    order = app.checkout.checkout(cart.id, None)
    assert order.status == OrderStatus.CREATED
    assert order.total_amount.amount == 2_000_000
    # 예약만 잡힌 상태: 물리 재고는 그대로, 가용은 줄어든다.
    assert app.catalog.get(product.id).stock_quantity == 5
    assert app.inventory.available(product.id) == 3

    payment = app.payments.pay(order.id, "card", None)
    assert payment.status == PaymentStatus.CAPTURED
    assert app.orders.get(order.id).status == OrderStatus.PAID
    # 결제 확정으로 물리 재고가 실제 차감된다.
    assert app.catalog.get(product.id).stock_quantity == 3

    fulfilled = app.orders.fulfill(order.id)
    assert fulfilled.status == OrderStatus.FULFILLED


def test_e2e2_cancel_before_payment(app):
    """E2E-2 결제 전 취소: 예약이 풀려 가용 재고가 복원된다."""
    product = app.catalog.create("노트북", 1_000_000, 5, None)
    cart = app.carts.create()
    app.carts.add_item(cart.id, product.id, 2)
    order = app.checkout.checkout(cart.id, None)
    assert app.inventory.available(product.id) == 3

    cancelled = app.checkout.cancel(order.id)
    assert cancelled.status == OrderStatus.CANCELLED
    assert app.inventory.available(product.id) == 5
    assert app.catalog.get(product.id).stock_quantity == 5


def test_e2e3_cancel_after_payment_refunds(app):
    """E2E-3 결제 후 취소: 결제가 환불되고 재고가 복원된다."""
    product = app.catalog.create("노트북", 1_000_000, 5, None)
    cart = app.carts.create()
    app.carts.add_item(cart.id, product.id, 2)
    order = app.checkout.checkout(cart.id, None)
    payment = app.payments.pay(order.id, "card", None)
    assert app.catalog.get(product.id).stock_quantity == 3

    cancelled = app.checkout.cancel(order.id)
    assert cancelled.status == OrderStatus.CANCELLED
    assert app.payments.get(payment.id).status == PaymentStatus.REFUNDED
    # 확정 차감분이 복원되어 물리 재고가 원복된다.
    assert app.catalog.get(product.id).stock_quantity == 5
    assert app.inventory.available(product.id) == 5


def test_e2e4_oversell_prevented(app):
    """E2E-4 oversell 방지: 가용분을 넘는 두 번째 체크아웃은 거부된다."""
    product = app.catalog.create("한정판", 500_000, 3, None)
    cart1 = app.carts.create()
    app.carts.add_item(cart1.id, product.id, 3)
    app.checkout.checkout(cart1.id, None)

    cart2 = app.carts.create()
    app.carts.add_item(cart2.id, product.id, 1)
    with pytest.raises(DomainException) as exc:
        app.checkout.checkout(cart2.id, None)
    assert exc.value.code == ErrorCode.INSUFFICIENT_STOCK


def test_e2e5_search_and_pagination(app):
    """E2E-5 검색·페이징: 이름 검색과 페이지 크기가 동작한다."""
    app.catalog.create("게이밍 노트북", 2_000_000, 5, None)
    app.catalog.create("사무용 노트북", 1_000_000, 5, None)
    app.catalog.create("기계식 키보드", 80_000, 10, None)

    page1 = app.catalog.search("노트북", None, 1, 1)
    assert page1.total == 2
    assert page1.pages == 2
    assert len(page1.items) == 1

    page_all = app.catalog.search("키보드", None, 1, 10)
    assert page_all.total == 1
    assert page_all.items[0].name == "기계식 키보드"


def test_e2e6_idempotent_checkout_and_payment(app):
    """E2E-6 멱등성: 같은 키의 체크아웃·결제는 한 번만 반영된다."""
    product = app.catalog.create("노트북", 1_000_000, 5, None)
    cart = app.carts.create()
    app.carts.add_item(cart.id, product.id, 2)

    order1 = app.checkout.checkout(cart.id, "checkout-key-1")
    order2 = app.checkout.checkout(cart.id, "checkout-key-1")
    assert order1.id == order2.id  # 같은 키는 새 주문을 만들지 않는다.

    pay1 = app.payments.pay(order1.id, "card", "pay-key-1")
    pay2 = app.payments.pay(order1.id, "card", "pay-key-1")
    assert pay1.id == pay2.id  # 이중 청구 없이 같은 결제를 돌려준다.
    # 멱등 결제이므로 재고는 1회만 차감된다.
    assert app.catalog.get(product.id).stock_quantity == 3


def test_e2e7_archived_product_blocked(app):
    """E2E-7 아카이브 차단: ARCHIVED 상품은 장바구니에 담을 수 없다."""
    product = app.catalog.create("단종품", 100_000, 5, None)
    app.catalog.archive(product.id)
    assert app.catalog.get(product.id).status == ProductStatus.ARCHIVED

    cart = app.carts.create()
    with pytest.raises(DomainException) as exc:
        app.carts.add_item(cart.id, product.id, 1)
    assert exc.value.code == ErrorCode.PRODUCT_ARCHIVED


def test_e2e8_fulfill_requires_paid(app):
    """E2E-8 이행 가드: 결제 전 주문은 이행이 거부된다."""
    product = app.catalog.create("노트북", 1_000_000, 5, None)
    cart = app.carts.create()
    app.carts.add_item(cart.id, product.id, 1)
    order = app.checkout.checkout(cart.id, None)

    with pytest.raises(DomainException) as exc:
        app.orders.fulfill(order.id)
    assert exc.value.code == ErrorCode.PAYMENT_REQUIRED


def test_e2e9_payment_declined(app):
    """E2E-9 결제 거절: declined 수단은 거부되고 주문은 CREATED 로 남는다."""
    product = app.catalog.create("노트북", 1_000_000, 5, None)
    cart = app.carts.create()
    app.carts.add_item(cart.id, product.id, 1)
    order = app.checkout.checkout(cart.id, None)

    with pytest.raises(DomainException) as exc:
        app.payments.pay(order.id, "declined", None)
    assert exc.value.code == ErrorCode.PAYMENT_DECLINED
    # 거절 후 주문은 CREATED 로 남아 재시도·취소가 가능하다.
    assert app.orders.get(order.id).status == OrderStatus.CREATED
    # 예약은 아직 살아 있어 가용 재고가 묶여 있다.
    assert app.inventory.available(product.id) == 4
