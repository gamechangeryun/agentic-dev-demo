"""인메모리 주문 저장소 어댑터입니다. 자바 ``InMemoryOrderRepository`` 를 옮긴 것입니다."""

from __future__ import annotations

from typing import Dict, List, Optional

from .domain import Order


class InMemoryOrderRepository:
    """dict 기반 주문 저장소입니다."""

    def __init__(self) -> None:
        self._store: Dict[str, Order] = {}
        self._seq = 0

    def save(self, order: Order) -> Order:
        self._store[order.id] = order
        return order

    def find_by_id(self, order_id: str) -> Optional[Order]:
        return self._store.get(order_id)

    def find_all(self) -> List[Order]:
        """id 오름차순 전체. 상태 필터·페이징은 서비스 계층에서 적용합니다."""
        return sorted(self._store.values(), key=lambda o: o.id)

    def next_id(self) -> str:
        self._seq += 1
        return f"ord_{self._seq:04d}"
