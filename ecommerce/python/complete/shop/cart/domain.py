"""장바구니 애그리거트입니다. 상품 id 별 수량을 담습니다.

같은 상품을 다시 담으면 수량이 합쳐지고, 수량을 0으로 바꾸면 항목이 제거됩니다.
가격은 담는 시점이 아니라 checkout 시점에 스냅샷되므로, 장바구니는 수량만
책임집니다. 빈 장바구니는 checkout 단계에서 거부됩니다. 자바 ``Cart`` 를 옮긴
것입니다. 삽입 순서 보존은 파이썬 dict 가 기본 보장합니다(자바의 LinkedHashMap).
"""

from __future__ import annotations

from typing import Dict

from ..shared import DomainException, ErrorCode


class Cart:
    """장바구니 애그리거트입니다. 수량 규칙을 스스로 지킵니다."""

    def __init__(self, cart_id: str) -> None:
        self._id = cart_id
        self._lines: Dict[str, int] = {}

    def add_item(self, product_id: str, qty: int) -> None:
        """상품을 담습니다. 이미 있으면 수량을 합칩니다. 수량은 1 이상이어야 합니다."""
        if qty <= 0:
            raise DomainException(ErrorCode.INVALID_QTY, "담는 수량은 1 이상이어야 합니다.")
        self._lines[product_id] = self._lines.get(product_id, 0) + qty

    def update_qty(self, product_id: str, qty: int) -> None:
        """수량을 변경합니다. 0이면 항목을 제거하고, 음수는 거부합니다."""
        self._require_line(product_id)
        if qty < 0:
            raise DomainException(ErrorCode.INVALID_QTY, "수량은 음수일 수 없습니다.")
        if qty == 0:
            del self._lines[product_id]
        else:
            self._lines[product_id] = qty

    def remove_item(self, product_id: str) -> None:
        """담긴 항목을 제거합니다. 없는 항목이면 NOT_FOUND 로 거부합니다."""
        self._require_line(product_id)
        del self._lines[product_id]

    def clear(self) -> None:
        """장바구니를 비웁니다."""
        self._lines.clear()

    def is_empty(self) -> bool:
        """담긴 항목이 없으면 참을 돌려줍니다."""
        return len(self._lines) == 0

    def _require_line(self, product_id: str) -> None:
        if product_id not in self._lines:
            raise DomainException(
                ErrorCode.NOT_FOUND, f"장바구니에 없는 상품입니다: {product_id}"
            )

    @property
    def id(self) -> str:
        return self._id

    def lines(self) -> Dict[str, int]:
        """읽기 전용 사본을 돌려줍니다. 외부에서 내부 dict 를 직접 바꾸지 못합니다."""
        return dict(self._lines)
