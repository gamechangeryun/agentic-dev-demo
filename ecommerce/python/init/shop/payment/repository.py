"""인메모리 결제 저장소 어댑터입니다. 자바 ``InMemoryPaymentRepository`` 를 옮긴 것입니다."""

from __future__ import annotations

from typing import Dict, Optional

from .domain import Payment


class InMemoryPaymentRepository:
    """dict 기반 결제 저장소입니다."""

    def __init__(self) -> None:
        self._store: Dict[str, Payment] = {}
        self._seq = 0

    def save(self, payment: Payment) -> Payment:
        self._store[payment.id] = payment
        return payment

    def find_by_id(self, payment_id: str) -> Optional[Payment]:
        return self._store.get(payment_id)

    def next_id(self) -> str:
        self._seq += 1
        return f"pay_{self._seq:04d}"
