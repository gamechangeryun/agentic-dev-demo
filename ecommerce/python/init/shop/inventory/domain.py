"""재고 예약 애그리거트입니다. 한 상품에 대한 특정 수량의 점유를 표현합니다.

예약은 가용 재고를 미리 묶어 두는 장치입니다. 결제가 끝나면 confirm 으로
물리 재고에 반영되고, 주문이 취소되면 release 로 점유가 풀립니다. 상태 전환은
이 애그리거트가 직접 검증하여 잘못된 이중 확정·이중 해제를 막습니다. 자바
``Reservation`` / ``ReservationStatus`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from enum import Enum

from ..shared import DomainException, ErrorCode


class ReservationStatus(Enum):
    """예약 상태입니다. RESERVED 로 시작해 결제 확정 시 CONFIRMED, 취소 시 RELEASED 입니다.

    CONFIRMED 와 RELEASED 는 terminal 입니다.
    """

    RESERVED = "RESERVED"
    CONFIRMED = "CONFIRMED"
    RELEASED = "RELEASED"


class Reservation:
    """재고 예약 애그리거트입니다. 상태 전환을 스스로 검증합니다."""

    def __init__(self, reservation_id: str, product_id: str, qty: int) -> None:
        if qty <= 0:
            raise DomainException(ErrorCode.INVALID_QTY, "예약 수량은 1 이상이어야 합니다.")
        self._id = reservation_id
        self._product_id = product_id
        self._qty = qty
        self._status = ReservationStatus.RESERVED

    def confirm(self) -> None:
        """예약을 확정합니다. RESERVED 상태에서만 허용합니다."""
        if self._status != ReservationStatus.RESERVED:
            raise DomainException(
                ErrorCode.INVALID_STATE_TRANSITION,
                f"RESERVED 상태에서만 확정할 수 있습니다. 현재: {self._status.value}",
            )
        self._status = ReservationStatus.CONFIRMED

    def release(self) -> bool:
        """점유를 해제합니다.

        이미 해제된 예약을 다시 해제하면 멱등하게 통과(False)합니다. 확정된
        예약을 해제하면 물리 재고 복원이 필요하므로 True 를 돌려줍니다.
        """
        if self._status == ReservationStatus.RELEASED:
            return False
        was_confirmed = self._status == ReservationStatus.CONFIRMED
        self._status = ReservationStatus.RELEASED
        return was_confirmed

    @property
    def id(self) -> str:
        return self._id

    @property
    def product_id(self) -> str:
        return self._product_id

    @property
    def qty(self) -> int:
        return self._qty

    @property
    def status(self) -> ReservationStatus:
        return self._status
