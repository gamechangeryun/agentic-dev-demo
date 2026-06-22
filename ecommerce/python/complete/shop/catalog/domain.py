"""상품 애그리거트입니다. 가격·재고·상태의 불변식을 스스로 지킵니다.

가격은 0원 이하로 생성될 수 없고, ARCHIVED 상태에서는 어떤 재고 변경도
허용되지 않습니다. 재고는 음수가 될 수 없습니다. 이 규칙들은 모두 이 애그리거트
안에 모여 있어, 외부에서 상태를 직접 바꾸지 못합니다. 자바 ``Product`` /
``ProductStatus`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from enum import Enum

from ..shared import DomainException, ErrorCode, Money


class ProductStatus(Enum):
    """상품 상태입니다. ACTIVE 에서 ARCHIVED 로 한 방향 전환만 허용됩니다.

    ARCHIVED 는 terminal 상태이며 이후 재고 변경이 모두 거부됩니다.
    """

    ACTIVE = "ACTIVE"
    ARCHIVED = "ARCHIVED"


class Product:
    """상품 애그리거트입니다. 불변식은 모두 이 안에서 검증합니다."""

    def __init__(self, product_id: str, name: str, price: Money, stock_quantity: int) -> None:
        self._id = product_id
        self._name = name
        self._price = price
        self._stock_quantity = stock_quantity
        self._status = ProductStatus.ACTIVE

    @staticmethod
    def create(product_id: str, name: str, price: Money, initial_stock: int) -> "Product":
        """새 상품을 생성합니다. 이름이 비었거나 가격이 0원 이하이면 거부합니다."""
        if name is None or name.strip() == "":
            raise DomainException(ErrorCode.EMPTY_NAME, "상품명은 비어 있을 수 없습니다.")
        if not price.is_positive():
            raise DomainException(ErrorCode.INVALID_PRICE, "가격은 0원보다 커야 합니다.")
        if initial_stock < 0:
            raise DomainException(ErrorCode.INVALID_QTY, "초기 재고는 음수일 수 없습니다.")
        return Product(product_id, name.strip(), price, initial_stock)

    def add_stock(self, qty: int) -> None:
        """재고를 더합니다. ARCHIVED 이거나 수량이 1 미만이면 거부합니다."""
        self._ensure_active()
        if qty <= 0:
            raise DomainException(ErrorCode.INVALID_QTY, "추가 수량은 1 이상이어야 합니다.")
        self._stock_quantity += qty

    def reduce_stock(self, qty: int) -> None:
        """재고를 뺍니다. 보유분을 넘으면 INSUFFICIENT_STOCK 으로 거부합니다."""
        self._ensure_active()
        if qty <= 0:
            raise DomainException(ErrorCode.INVALID_QTY, "차감 수량은 1 이상이어야 합니다.")
        if qty > self._stock_quantity:
            raise DomainException(
                ErrorCode.INSUFFICIENT_STOCK,
                f"재고가 부족합니다. 보유 {self._stock_quantity}, 요청 {qty}",
            )
        self._stock_quantity -= qty

    def archive(self) -> None:
        """상품을 ARCHIVED 로 전환합니다. 이후 재고 변경이 모두 막힙니다."""
        self._status = ProductStatus.ARCHIVED

    def _ensure_active(self) -> None:
        if self._status == ProductStatus.ARCHIVED:
            raise DomainException(
                ErrorCode.PRODUCT_ARCHIVED, "ARCHIVED 상품은 재고를 변경할 수 없습니다."
            )

    def is_active(self) -> bool:
        """상품이 ACTIVE 이면 참을 돌려줍니다."""
        return self._status == ProductStatus.ACTIVE

    @property
    def id(self) -> str:
        return self._id

    @property
    def name(self) -> str:
        return self._name

    @property
    def price(self) -> Money:
        return self._price

    @property
    def stock_quantity(self) -> int:
        return self._stock_quantity

    @property
    def status(self) -> ProductStatus:
        return self._status
