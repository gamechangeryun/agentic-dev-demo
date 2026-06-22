"""체크아웃 오케스트레이션입니다. 여러 bounded context 를 한 흐름으로 묶습니다.

체크아웃은 장바구니를 주문으로 바꾸는 과정입니다. 각 줄 항목의 재고를 먼저
예약하고, 모두 성공하면 가격 스냅샷으로 주문을 생성합니다. 중간에 한 줄이라도
재고가 부족하면, 이미 잡아 둔 예약을 모두 풀어 원자성을 흉내 내고 거부합니다.
취소는 반대 방향 보상입니다. 예약을 풀고, 이미 결제된 주문이면 환불까지
수행합니다. 자바 ``CheckoutService`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from typing import Dict, List, Optional

from ..cart.service import CartService
from ..catalog.service import CatalogService
from ..inventory.service import InventoryService
from ..ordering.domain import Order, OrderLine
from ..ordering.service import OrderService
from ..payment.service import PaymentService
from ..shared import DomainException, ErrorCode


class CheckoutService:
    """장바구니→주문 체크아웃과 주문 취소 보상을 오케스트레이션합니다."""

    def __init__(
        self,
        carts: CartService,
        catalog: CatalogService,
        inventory: InventoryService,
        orders: OrderService,
        payments: PaymentService,
    ) -> None:
        self._carts = carts
        self._catalog = catalog
        self._inventory = inventory
        self._orders = orders
        self._payments = payments
        self._idempotency: Dict[str, str] = {}

    def checkout(self, cart_id: str, idem_key: Optional[str] = None) -> Order:
        """장바구니를 주문으로 확정합니다.

        같은 idem_key 재요청은 기존 주문을 그대로 돌려줍니다. 재고를 줄별로
        예약하다 한 줄이라도 부족하면 잡아 둔 예약을 모두 풀고 거부합니다.
        """
        if idem_key is not None and idem_key in self._idempotency:
            return self._orders.get(self._idempotency[idem_key])

        cart = self._carts.get(cart_id)
        if cart.is_empty():
            raise DomainException(ErrorCode.EMPTY_CART, "빈 장바구니는 주문할 수 없습니다.")

        lines: List[OrderLine] = []
        reservation_ids: List[str] = []
        try:
            for product_id, qty in cart.lines().items():
                product = self._catalog.get(product_id)
                reservation = self._inventory.reserve(product.id, qty)
                reservation_ids.append(reservation.id)
                lines.append(
                    OrderLine(product.id, product.name, product.price, qty)
                )
        except DomainException:
            # 보상: 이미 잡은 예약을 모두 풀고 거부합니다(부분 예약을 남기지 않습니다).
            for reservation_id in reservation_ids:
                self._inventory.release(reservation_id)
            raise

        order = self._orders.create(lines, reservation_ids)
        self._carts.clear(cart_id)
        if idem_key is not None:
            self._idempotency[idem_key] = order.id
        return order

    def cancel(self, order_id: str) -> Order:
        """주문을 취소합니다. 예약을 풀고, 이미 결제된 주문이면 환불까지 보상합니다."""
        order = self._orders.get(order_id)
        reservation_ids = order.reservation_ids
        payment_id = order.payment_id

        was_paid = self._orders.cancel(order_id)
        for reservation_id in reservation_ids:
            self._inventory.release(reservation_id)
        if was_paid and payment_id is not None:
            self._payments.refund(payment_id)
        return self._orders.get(order_id)
