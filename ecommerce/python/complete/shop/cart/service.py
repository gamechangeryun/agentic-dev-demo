"""장바구니 유스케이스입니다.

상품을 담을 때 해당 상품이 존재하고 ACTIVE 인지 검증합니다. ARCHIVED 상품은
담을 수 없습니다. 수량 규칙 자체는 ``Cart`` 애그리거트가 지킵니다. 자바
``CartService`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from typing import Optional

from ..catalog.service import CatalogService
from ..shared import DomainException, ErrorCode
from .domain import Cart
from .repository import InMemoryCartRepository


class CartService:
    """장바구니 생성·조회·항목 조작 유스케이스를 조율합니다."""

    def __init__(
        self, carts: Optional[InMemoryCartRepository], catalog: CatalogService
    ) -> None:
        self._carts = carts if carts is not None else InMemoryCartRepository()
        self._catalog = catalog

    def create(self) -> Cart:
        return self._carts.save(Cart(self._carts.next_id()))

    def get(self, cart_id: str) -> Cart:
        cart = self._carts.find_by_id(cart_id)
        if cart is None:
            raise DomainException(ErrorCode.NOT_FOUND, f"장바구니를 찾을 수 없습니다: {cart_id}")
        return cart

    def add_item(self, cart_id: str, product_id: str, qty: int) -> Cart:
        """상품을 담습니다. ARCHIVED 상품은 거부합니다."""
        cart = self.get(cart_id)
        product = self._catalog.get(product_id)
        if not product.is_active():
            raise DomainException(
                ErrorCode.PRODUCT_ARCHIVED,
                f"ARCHIVED 상품은 장바구니에 담을 수 없습니다: {product_id}",
            )
        cart.add_item(product_id, qty)
        return self._carts.save(cart)

    def update_qty(self, cart_id: str, product_id: str, qty: int) -> Cart:
        cart = self.get(cart_id)
        cart.update_qty(product_id, qty)
        return self._carts.save(cart)

    def remove_item(self, cart_id: str, product_id: str) -> Cart:
        cart = self.get(cart_id)
        cart.remove_item(product_id)
        return self._carts.save(cart)

    def clear(self, cart_id: str) -> Cart:
        cart = self.get(cart_id)
        cart.clear()
        return self._carts.save(cart)
