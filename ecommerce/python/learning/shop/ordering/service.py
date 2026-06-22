"""주문 유스케이스입니다. 주문 애그리거트의 상태 전환과 영속화만 책임집니다.

재고 예약·결제 같은 부수효과는 checkout·payment 오케스트레이션이 담당하고,
이 서비스는 그 결과로 도착하는 상태 전환 요청을 애그리거트에 위임합니다.
목록 조회는 상태 필터와 페이징을 제공합니다. 자바 ``OrderService`` 를 옮긴
것입니다.
"""

from __future__ import annotations

from typing import List, Optional

from ..shared import DomainException, ErrorCode, Page
from .domain import Order, OrderLine, OrderStatus
from .repository import InMemoryOrderRepository


class OrderService:
    """주문 생성·조회·상태 전환 유스케이스를 조율합니다."""

    def __init__(self, orders: Optional[InMemoryOrderRepository] = None) -> None:
        self._orders = orders if orders is not None else InMemoryOrderRepository()

    def create(self, lines: List[OrderLine], reservation_ids: List[str]) -> Order:
        order = Order.create(self._orders.next_id(), lines, reservation_ids)
        return self._orders.save(order)

    def get(self, order_id: str) -> Order:
        order = self._orders.find_by_id(order_id)
        if order is None:
            raise DomainException(ErrorCode.NOT_FOUND, f"주문을 찾을 수 없습니다: {order_id}")
        return order

    def search(self, status: Optional[OrderStatus], page: int, size: int) -> Page[Order]:
        rows = self._orders.find_all()
        if status is not None:
            rows = [o for o in rows if o.status == status]
        return Page.of(rows, page, size)

    def mark_paid(self, order_id: str, payment_id: str) -> Order:
        order = self.get(order_id)
        order.mark_paid(payment_id)
        return self._orders.save(order)

    def fulfill(self, order_id: str) -> Order:
        order = self.get(order_id)
        order.fulfill()
        return self._orders.save(order)

    def cancel(self, order_id: str) -> bool:
        """주문 상태만 취소로 전환하고, 취소 직전 결제 완료였는지 돌려줍니다."""
        order = self.get(order_id)
        was_paid = order.cancel()
        self._orders.save(order)
        return was_paid
