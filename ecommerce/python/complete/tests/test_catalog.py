"""상품 애그리거트 불변식 단위 테스트입니다. 자바 ProductTest 를 옮긴 것입니다."""

import pytest

from shop.catalog.domain import Product
from shop.shared import DomainException, ErrorCode, Money


def test_ac1_rejects_non_positive_price():
    """AC-1 가격이 0원 이하이면 상품 생성을 거부한다."""
    with pytest.raises(DomainException) as exc:
        Product.create("p1", "노트북", Money.won(0), 5)
    assert exc.value.code == ErrorCode.INVALID_PRICE


def test_ac3_add_and_reduce_stock():
    """AC-3 ACTIVE 상품은 재고를 더하고 뺄 수 있다."""
    p = Product.create("p1", "노트북", Money.won(1000), 5)
    p.add_stock(3)
    assert p.stock_quantity == 8
    p.reduce_stock(2)
    assert p.stock_quantity == 6


def test_ac4_reduce_beyond_stock_rejected():
    """AC-4 보유 재고를 넘는 차감은 INSUFFICIENT_STOCK 으로 거부한다."""
    p = Product.create("p1", "노트북", Money.won(1000), 5)
    with pytest.raises(DomainException) as exc:
        p.reduce_stock(6)
    assert exc.value.code == ErrorCode.INSUFFICIENT_STOCK


def test_ac6_archived_blocks_stock_changes():
    """AC-6 ARCHIVED 상품은 재고 변경이 모두 거부된다."""
    p = Product.create("p1", "노트북", Money.won(1000), 5)
    p.archive()
    with pytest.raises(DomainException) as exc:
        p.add_stock(1)
    assert exc.value.code == ErrorCode.PRODUCT_ARCHIVED


def test_empty_name_rejected():
    """상품명이 비어 있으면 생성을 거부한다."""
    with pytest.raises(DomainException) as exc:
        Product.create("p1", "  ", Money.won(1000), 5)
    assert exc.value.code == ErrorCode.EMPTY_NAME
