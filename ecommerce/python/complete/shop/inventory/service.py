"""재고 예약 유스케이스입니다. oversell 을 막는 핵심 게이트입니다.

가용 재고는 물리 재고에서 활성 예약 합계를 뺀 값입니다. reserve 는 가용 재고를
확인하고 점유하는 두 동작이 원자적으로 일어나야 하므로 락으로 보호합니다. 이
덕분에 동시에 들어온 두 주문이 같은 재고를 중복 점유하지 못하고, 가용분을
초과하는 뒤 주문은 INSUFFICIENT_STOCK 으로 거부됩니다. 자바 ``InventoryService``
의 ``synchronized`` 를 ``threading.RLock`` 으로 옮긴 것입니다.
"""

from __future__ import annotations

import threading
from typing import Optional

from ..catalog.service import CatalogService
from ..shared import DomainException, ErrorCode
from .domain import Reservation
from .repository import InMemoryReservationRepository


class InventoryService:
    """재고 예약·확정·해제 유스케이스를 조율합니다."""

    def __init__(
        self,
        catalog: CatalogService,
        reservations: Optional[InMemoryReservationRepository] = None,
    ) -> None:
        self._catalog = catalog
        self._reservations = (
            reservations if reservations is not None else InMemoryReservationRepository()
        )
        self._lock = threading.RLock()

    def available(self, product_id: str) -> int:
        """가용 재고(물리 재고 - 활성 예약 합계)를 돌려줍니다."""
        product = self._catalog.get(product_id)
        reserved = sum(
            r.qty for r in self._reservations.find_active_by_product(product_id)
        )
        return product.stock_quantity - reserved

    def reserve(self, product_id: str, qty: int) -> Reservation:
        """가용 재고를 확인하고 점유합니다. 가용분 초과·아카이브 상품은 거부합니다."""
        with self._lock:
            product = self._catalog.get(product_id)
            if not product.is_active():
                raise DomainException(
                    ErrorCode.PRODUCT_ARCHIVED, f"ARCHIVED 상품은 예약할 수 없습니다: {product_id}"
                )
            if qty <= 0:
                raise DomainException(ErrorCode.INVALID_QTY, "예약 수량은 1 이상이어야 합니다.")
            available = self.available(product_id)
            if qty > available:
                raise DomainException(
                    ErrorCode.INSUFFICIENT_STOCK,
                    f"가용 재고가 부족합니다. 가용 {available}, 요청 {qty}",
                )
            reservation = Reservation(self._reservations.next_id(), product_id, qty)
            return self._reservations.save(reservation)

    def confirm(self, reservation_id: str) -> Reservation:
        """예약을 확정하고, 물리 재고를 실제로 차감합니다."""
        with self._lock:
            reservation = self._find(reservation_id)
            reservation.confirm()
            self._catalog.reduce_stock(reservation.product_id, reservation.qty)
            return self._reservations.save(reservation)

    def release(self, reservation_id: str) -> Reservation:
        """예약을 해제합니다. 이미 확정된 예약이면 물리 재고를 복원합니다."""
        with self._lock:
            reservation = self._find(reservation_id)
            was_confirmed = reservation.release()
            if was_confirmed:
                # 이미 물리 재고에서 빠진 예약을 되돌리므로 재고를 복원합니다.
                self._catalog.add_stock(reservation.product_id, reservation.qty)
            return self._reservations.save(reservation)

    def _find(self, reservation_id: str) -> Reservation:
        reservation = self._reservations.find_by_id(reservation_id)
        if reservation is None:
            raise DomainException(
                ErrorCode.NOT_FOUND, f"예약을 찾을 수 없습니다: {reservation_id}"
            )
        return reservation
