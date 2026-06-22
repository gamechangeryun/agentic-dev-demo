"""인메모리 장바구니 저장소 어댑터입니다. 자바 ``InMemoryCartRepository`` 를 옮긴 것입니다."""

from __future__ import annotations

from typing import Dict, Optional

from .domain import Cart


class InMemoryCartRepository:
    """dict 기반 장바구니 저장소입니다."""

    def __init__(self) -> None:
        self._store: Dict[str, Cart] = {}
        self._seq = 0

    def save(self, cart: Cart) -> Cart:
        self._store[cart.id] = cart
        return cart

    def find_by_id(self, cart_id: str) -> Optional[Cart]:
        return self._store.get(cart_id)

    def next_id(self) -> str:
        self._seq += 1
        return f"cart_{self._seq:04d}"
