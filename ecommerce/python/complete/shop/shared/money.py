"""금액 값 객체입니다.

원 단위 정수로만 표현하며 음수를 허용하지 않습니다. 불변 값 객체이므로
모든 연산은 새 인스턴스를 돌려줍니다. 자바 ``Money`` record 를 옮긴 것입니다.
"""

from __future__ import annotations

from dataclasses import dataclass

from .errors import DomainException, ErrorCode


@dataclass(frozen=True)
class Money:
    """원 단위 정수 금액입니다. 음수는 생성 시점에 거부합니다."""

    amount: int

    def __post_init__(self) -> None:
        if self.amount < 0:
            raise DomainException(
                ErrorCode.INVALID_PRICE, f"금액은 음수일 수 없습니다: {self.amount}"
            )

    @staticmethod
    def won(amount: int) -> "Money":
        """원 단위 금액으로 ``Money`` 를 만듭니다."""
        return Money(amount)

    def plus(self, other: "Money") -> "Money":
        """두 금액을 더한 새 ``Money`` 를 돌려줍니다."""
        return Money(self.amount + other.amount)

    def times(self, quantity: int) -> "Money":
        """수량을 곱한 새 ``Money`` 를 돌려줍니다. 음수 수량은 거부합니다."""
        if quantity < 0:
            raise DomainException(
                ErrorCode.INVALID_QTY, f"수량은 음수일 수 없습니다: {quantity}"
            )
        return Money(self.amount * quantity)

    def is_positive(self) -> bool:
        """금액이 0보다 크면 참을 돌려줍니다."""
        return self.amount > 0


# 자바의 Money.ZERO 상수와 동일한 역할입니다.
Money.ZERO = Money(0)  # type: ignore[attr-defined]
