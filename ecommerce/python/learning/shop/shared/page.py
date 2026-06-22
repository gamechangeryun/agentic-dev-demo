"""페이지 조회 결과를 표현하는 공용 값 객체입니다.

목록 API 는 전체 건수와 페이지 수를 함께 돌려주어, 호출자가 다음 페이지
존재 여부를 계산할 수 있게 합니다. 자바 ``Page<T>`` record 를 옮긴 것입니다.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Generic, List, TypeVar

T = TypeVar("T")


@dataclass(frozen=True)
class Page(Generic[T]):
    """페이지 단위 조회 결과입니다. 항목·전체 건수·페이지·크기·총 페이지 수를 담습니다."""

    items: List[T]
    total: int
    page: int
    size: int
    pages: int

    @staticmethod
    def of(all_items: List[T], page: int, size: int) -> "Page[T]":
        """전체 목록을 페이지·크기로 잘라 ``Page`` 를 만듭니다.

        자바 구현과 동일하게 페이지·크기를 1 이상으로 보정하고, 범위를 넘는
        요청은 빈 슬라이스로 안전하게 처리합니다.
        """
        safe_page = max(1, page)
        safe_size = max(1, size)
        total = len(all_items)
        pages = (total + safe_size - 1) // safe_size
        start = min((safe_page - 1) * safe_size, total)
        end = min(start + safe_size, total)
        return Page(all_items[start:end], total, safe_page, safe_size, pages)
