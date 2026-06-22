"""인메모리 상품 저장소 어댑터입니다. 외부 DB 없이 동작합니다.

dict 로 저장하고, id 는 단조 증가 시퀀스로 결정적으로 발급합니다. 운영에서는
이 어댑터를 실제 DB 구현으로 교체합니다. 자바 ``InMemoryProductRepository`` 를
옮긴 것입니다.
"""

from __future__ import annotations

from typing import Dict, List, Optional

from .domain import Product


class InMemoryProductRepository:
    """dict 기반 상품 저장소입니다."""

    def __init__(self) -> None:
        self._store: Dict[str, Product] = {}
        self._seq = 0

    def save(self, product: Product) -> Product:
        self._store[product.id] = product
        return product

    def find_by_id(self, product_id: str) -> Optional[Product]:
        return self._store.get(product_id)

    def find_all(self) -> List[Product]:
        """id 오름차순으로 전체를 돌려줍니다. 검색·페이징은 서비스 계층에서 적용합니다."""
        return sorted(self._store.values(), key=lambda p: p.id)

    def next_id(self) -> str:
        self._seq += 1
        return f"prod_{self._seq:04d}"
