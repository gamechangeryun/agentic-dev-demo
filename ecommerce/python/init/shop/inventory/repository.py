"""인메모리 예약 저장소 어댑터입니다. 상품별 활성 예약 질의를 포함합니다.

자바 ``InMemoryReservationRepository`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from typing import Dict, List, Optional

from .domain import Reservation, ReservationStatus


class InMemoryReservationRepository:
    """dict 기반 예약 저장소입니다."""

    def __init__(self) -> None:
        self._store: Dict[str, Reservation] = {}
        self._seq = 0

    def save(self, reservation: Reservation) -> Reservation:
        self._store[reservation.id] = reservation
        return reservation

    def find_by_id(self, reservation_id: str) -> Optional[Reservation]:
        return self._store.get(reservation_id)

    def find_active_by_product(self, product_id: str) -> List[Reservation]:
        """해당 상품의 RESERVED(활성) 예약만 돌려줍니다."""
        return [
            r
            for r in self._store.values()
            if r.product_id == product_id and r.status == ReservationStatus.RESERVED
        ]

    def next_id(self) -> str:
        self._seq += 1
        return f"resv_{self._seq:04d}"
