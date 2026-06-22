"""실거래 원장 (자바 transaction-service 포팅, write model).

거래 적재 커맨드 서비스와 인메모리 저장소를 담습니다. 자바에서는 JPA 어댑터와 포트를
헥사고날로 분리했지만, 파이썬 축소판에서는 인메모리 저장소 하나로 동일한 멱등 규칙을 보존합니다.

요구사항 AC-4(멱등): 동일 (시군구·계약월) 수집이 재실행돼도 자연키로 중복을 차단합니다.
"""

from __future__ import annotations

from typing import Iterable

from .common import AptTransaction


class InMemoryAptTradeStore:
    """거래원장 저장소입니다 (자바 AptTradeStore 포트 + 인메모리 어댑터).

    자연키로 멱등성을 보장합니다. 같은 자연키가 다시 들어오면 덮어쓰지 않고 무시합니다.
    """

    def __init__(self) -> None:
        # 삽입 순서를 보존해 자바 LinkedHashMap 동작과 맞춥니다.
        self._data: dict[str, AptTransaction] = {}

    def exists_by_natural_key(self, natural_key: str) -> bool:
        return natural_key in self._data

    def save(self, transaction: AptTransaction) -> None:
        self._data[transaction.natural_key()] = transaction

    def find_by_region_month(self, sgg_cd: str, deal_year: int, deal_month: int) -> list[AptTransaction]:
        return [
            tx for tx in self._data.values()
            if tx.sgg_cd == sgg_cd and tx.deal_year == deal_year and tx.deal_month == deal_month
        ]

    def size(self) -> int:
        return len(self._data)


class TransactionCommandService:
    """거래 적재 커맨드 서비스입니다 (자바 TransactionCommandService 포팅).

    이미 존재하는 자연키는 건너뛰고, 새 거래만 저장한 뒤 새로 적재된 건수를 돌려줍니다.
    """

    def __init__(self, store: InMemoryAptTradeStore) -> None:
        self._store = store

    def upsert_all(self, transactions: Iterable[AptTransaction]) -> int:
        """정규화된 거래 목록을 멱등 적재하고, 새로 들어간 건수를 반환합니다(AC-4)."""
        inserted = 0
        for tx in transactions:
            if self._store.exists_by_natural_key(tx.natural_key()):
                continue  # 이미 적재된 거래입니다(멱등).
            self._store.save(tx)
            inserted += 1
        return inserted

    def query(self, sgg_cd: str, deal_year: int, deal_month: int) -> list[AptTransaction]:
        return self._store.find_by_region_month(sgg_cd, deal_year, deal_month)
