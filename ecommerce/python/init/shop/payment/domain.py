"""결제 애그리거트입니다. 한 주문에 대한 한 번의 결제 시도를 표현합니다.

승인 여부는 외부 게이트웨이가 판정하고, 이 애그리거트는 그 결과를 상태로
고정합니다. 환불은 캡처된 결제에 대해서만 허용됩니다. 이중 캡처·이중 환불은
상태 가드로 막습니다. 자바 ``Payment`` / ``PaymentStatus`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from enum import Enum
from typing import Optional

from ..shared import DomainException, ErrorCode, Money


class PaymentStatus(Enum):
    """결제 상태입니다. PENDING 으로 시작해 승인되면 CAPTURED, 거절되면 DECLINED 입니다.

    CAPTURED 결제만 REFUNDED 로 환불할 수 있습니다.
    """

    PENDING = "PENDING"
    CAPTURED = "CAPTURED"
    DECLINED = "DECLINED"
    REFUNDED = "REFUNDED"


class Payment:
    """결제 애그리거트입니다. 캡처·환불 상태 전환을 스스로 검증합니다."""

    def __init__(
        self, payment_id: str, order_id: str, amount: Money, method: Optional[str]
    ) -> None:
        self._id = payment_id
        self._order_id = order_id
        self._amount = amount
        self._method = method if method is not None else "card"
        self._status = PaymentStatus.PENDING

    def capture(self, approved: bool) -> None:
        """게이트웨이 판정을 상태로 고정합니다. PENDING 결제만 캡처할 수 있습니다."""
        if self._status != PaymentStatus.PENDING:
            raise DomainException(
                ErrorCode.INVALID_STATE_TRANSITION,
                f"PENDING 결제만 캡처할 수 있습니다. 현재: {self._status.value}",
            )
        self._status = PaymentStatus.CAPTURED if approved else PaymentStatus.DECLINED

    def refund(self) -> None:
        """환불합니다. 캡처된 결제에 대해서만 허용합니다."""
        if self._status != PaymentStatus.CAPTURED:
            raise DomainException(
                ErrorCode.REFUND_NOT_ALLOWED,
                f"캡처된 결제만 환불할 수 있습니다. 현재: {self._status.value}",
            )
        self._status = PaymentStatus.REFUNDED

    def is_captured(self) -> bool:
        return self._status == PaymentStatus.CAPTURED

    @property
    def id(self) -> str:
        return self._id

    @property
    def order_id(self) -> str:
        return self._order_id

    @property
    def amount(self) -> Money:
        return self._amount

    @property
    def method(self) -> str:
        return self._method

    @property
    def status(self) -> PaymentStatus:
        return self._status
