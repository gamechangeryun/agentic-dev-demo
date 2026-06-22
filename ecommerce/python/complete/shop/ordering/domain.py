"""주문 애그리거트입니다. 상태 전환 규칙의 단일 소유자입니다.

주문은 줄 항목 가격 스냅샷의 합을 총액으로 가지며, 총액이 0원 이하이면 생성
자체가 거부됩니다. 결제·이행·취소는 모두 이 애그리거트의 상태 가드를 통과해야
합니다. 외부 오케스트레이션은 재고 예약·결제 같은 부수효과를 맡고, 상태 전환의
정합성은 여기서 보장됩니다. 자바 ``Order`` / ``OrderLine`` / ``OrderStatus`` 를
옮긴 것입니다.
"""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from functools import reduce
from typing import List, Optional

from ..shared import DomainException, ErrorCode, Money


class OrderStatus(Enum):
    """주문 상태입니다.

    정방향은 CREATED → PAID → FULFILLED 한 방향입니다. CREATED·PAID 에서만
    취소가 가능하고, 취소되면 CANCELLED 로 갑니다. FULFILLED 와 CANCELLED 는
    terminal 이며 더 이상 어떤 전환도 허용되지 않습니다.
    """

    CREATED = "CREATED"
    PAID = "PAID"
    FULFILLED = "FULFILLED"
    CANCELLED = "CANCELLED"


@dataclass(frozen=True)
class OrderLine:
    """주문 줄 항목 값 객체입니다. 주문 시점의 상품 가격을 스냅샷으로 고정합니다.

    주문 이후 상품 가격이 바뀌어도 주문 총액은 변하지 않아야 하므로, 가격을
    상품을 다시 참조하지 않고 이 값 객체에 박아 둡니다.
    """

    product_id: str
    name: str
    unit_price: Money
    qty: int

    def line_total(self) -> Money:
        """이 줄의 합계(단가 × 수량)를 돌려줍니다."""
        return self.unit_price.times(self.qty)


class Order:
    """주문 애그리거트입니다. 상태 전환의 정합성을 스스로 보장합니다."""

    def __init__(
        self,
        order_id: str,
        lines: List[OrderLine],
        total_amount: Money,
        reservation_ids: List[str],
    ) -> None:
        self._id = order_id
        self._lines = list(lines)
        self._total_amount = total_amount
        self._reservation_ids = list(reservation_ids)
        self._status = OrderStatus.CREATED
        self._payment_id: Optional[str] = None

    @staticmethod
    def create(
        order_id: str, lines: List[OrderLine], reservation_ids: List[str]
    ) -> "Order":
        """주문을 생성합니다. 빈 주문이거나 총액이 0원 이하이면 거부합니다."""
        if not lines:
            raise DomainException(ErrorCode.EMPTY_CART, "주문 항목이 비어 있습니다.")
        total = reduce(
            lambda acc, ln: acc.plus(ln.line_total()), lines, Money.ZERO
        )
        if not total.is_positive():
            raise DomainException(ErrorCode.INVALID_AMOUNT, "주문 총액은 0원보다 커야 합니다.")
        return Order(order_id, lines, total, reservation_ids)

    def mark_paid(self, payment_id: str) -> None:
        """CREATED 주문을 PAID 로 전환합니다."""
        if self._status != OrderStatus.CREATED:
            raise DomainException(
                ErrorCode.INVALID_STATE_TRANSITION,
                f"CREATED 주문만 결제 완료로 전환할 수 있습니다. 현재: {self._status.value}",
            )
        self._status = OrderStatus.PAID
        self._payment_id = payment_id

    def fulfill(self) -> None:
        """PAID 주문을 FULFILLED 로 전환합니다. 결제 전이면 거부합니다."""
        if self._status != OrderStatus.PAID:
            raise DomainException(
                ErrorCode.PAYMENT_REQUIRED,
                f"PAID 주문만 이행할 수 있습니다. 현재: {self._status.value}",
            )
        self._status = OrderStatus.FULFILLED

    def cancel(self) -> bool:
        """취소합니다. CREATED·PAID 에서만 허용되며, 취소 직전 결제 완료였는지 돌려줍니다."""
        if self._status == OrderStatus.FULFILLED:
            raise DomainException(
                ErrorCode.INVALID_STATE_TRANSITION, "이행 완료된 주문은 취소할 수 없습니다."
            )
        if self._status == OrderStatus.CANCELLED:
            raise DomainException(
                ErrorCode.INVALID_STATE_TRANSITION, "이미 취소된 주문입니다."
            )
        was_paid = self._status == OrderStatus.PAID
        self._status = OrderStatus.CANCELLED
        return was_paid

    @property
    def id(self) -> str:
        return self._id

    @property
    def lines(self) -> List[OrderLine]:
        return list(self._lines)

    @property
    def total_amount(self) -> Money:
        return self._total_amount

    @property
    def reservation_ids(self) -> List[str]:
        return list(self._reservation_ids)

    @property
    def status(self) -> OrderStatus:
        return self._status

    @property
    def payment_id(self) -> Optional[str]:
        return self._payment_id
