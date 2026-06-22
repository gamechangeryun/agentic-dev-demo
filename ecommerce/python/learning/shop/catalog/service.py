"""상품 카탈로그 유스케이스입니다.

생성은 idempotency_key 로 멱등을 보장하고, 목록 조회는 이름 검색·상태 필터·
페이징을 한 번에 처리합니다. 도메인 규칙은 모두 ``Product`` 애그리거트가
지키고, 이 서비스는 흐름을 조율하고 저장소와 연결합니다. 자바 ``CatalogService``
를 옮긴 것입니다.
"""

from __future__ import annotations

from typing import Dict, Optional

from ..shared import DomainException, ErrorCode, Money, Page
from .domain import Product, ProductStatus
from .repository import InMemoryProductRepository


class CatalogService:
    """상품 생성·조회·재고 변경·아카이브 유스케이스를 조율합니다."""

    def __init__(self, products: Optional[InMemoryProductRepository] = None) -> None:
        self._products = products if products is not None else InMemoryProductRepository()
        self._idempotency: Dict[str, str] = {}

    def create(
        self, name: str, price: int, initial_stock: int, idem_key: Optional[str] = None
    ) -> Product:
        """상품을 생성합니다. 같은 idem_key 재요청은 기존 상품을 그대로 돌려줍니다."""
        if idem_key is not None and idem_key in self._idempotency:
            return self.get(self._idempotency[idem_key])
        product = Product.create(
            self._products.next_id(), name, Money.won(price), initial_stock
        )
        self._products.save(product)
        if idem_key is not None:
            self._idempotency[idem_key] = product.id
        return product

    def get(self, product_id: str) -> Product:
        product = self._products.find_by_id(product_id)
        if product is None:
            raise DomainException(ErrorCode.NOT_FOUND, f"상품을 찾을 수 없습니다: {product_id}")
        return product

    def search(
        self,
        q: Optional[str],
        status: Optional[ProductStatus],
        page: int,
        size: int,
    ) -> Page[Product]:
        """이름 검색·상태 필터·페이징을 한 번에 적용해 목록을 돌려줍니다."""
        rows = self._products.find_all()
        if q is not None and q.strip() != "":
            needle = q.strip().lower()
            rows = [p for p in rows if needle in p.name.lower()]
        if status is not None:
            rows = [p for p in rows if p.status == status]
        return Page.of(rows, page, size)

    def add_stock(self, product_id: str, qty: int) -> Product:
        product = self.get(product_id)
        product.add_stock(qty)
        return self._products.save(product)

    def reduce_stock(self, product_id: str, qty: int) -> Product:
        product = self.get(product_id)
        product.reduce_stock(qty)
        return self._products.save(product)

    def archive(self, product_id: str) -> Product:
        product = self.get(product_id)
        product.archive()
        return self._products.save(product)
