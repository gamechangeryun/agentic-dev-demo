"""결제 유스케이스입니다. 주문 결제와 환불을 오케스트레이션합니다.

결제 승인이 떨어지면 주문을 PAID 로 전환하고, 주문에 묶인 재고 예약을 확정해
물리 재고를 실제로 차감합니다. 같은 idempotency_key 재요청은 기존 결제를 그대로
돌려주어 이중 청구를 막습니다. 결제가 거절되면 주문은 CREATED 로 남아 재시도하거나
취소할 수 있습니다. 자바 ``PaymentService`` 를 옮긴 것입니다.
"""

from __future__ import annotations

from typing import Dict, Optional

from ..inventory.service import InventoryService
from ..ordering.domain import OrderStatus
from ..ordering.service import OrderService
from ..shared import DomainException, ErrorCode
from .domain import Payment
from .gateway import PaymentGateway
from .repository import InMemoryPaymentRepository


class PaymentService:
    """멱등 결제와 환불 유스케이스를 조율합니다."""

    def __init__(
        self,
        payments: Optional[InMemoryPaymentRepository],
        gateway: PaymentGateway,
        orders: OrderService,
        inventory: InventoryService,
    ) -> None:
        self._payments = payments if payments is not None else InMemoryPaymentRepository()
        self._gateway = gateway
        self._orders = orders
        self._inventory = inventory
        self._idempotency: Dict[str, str] = {}

    def pay(
        self, order_id: str, method: Optional[str], idem_key: Optional[str] = None
    ) -> Payment:
        """주문을 결제합니다.

        같은 idem_key 재요청은 기존 결제를 그대로 돌려줍니다(이중 청구 차단).
        승인 시 주문을 PAID 로 전환하고 예약 재고를 확정합니다. 거절 시
        PAYMENT_DECLINED 를 던지고 주문은 CREATED 로 남습니다.
        """
        if idem_key is not None and idem_key in self._idempotency:
            return self.get(self._idempotency[idem_key])

        order = self._orders.get(order_id)
        if order.status == OrderStatus.PAID:
            raise DomainException(ErrorCode.ALREADY_PAID, f"이미 결제된 주문입니다: {order_id}")
        if order.status != OrderStatus.CREATED:
            raise DomainException(
                ErrorCode.INVALID_STATE_TRANSITION,
                f"CREATED 주문만 결제할 수 있습니다. 현재: {order.status.value}",
            )

        payment = Payment(self._payments.next_id(), order_id, order.total_amount, method)
        approved = self._gateway.authorize(payment.amount, method)
        payment.capture(approved)
        self._payments.save(payment)

        if not approved:
            raise DomainException(
                ErrorCode.PAYMENT_DECLINED, f"결제가 거절되었습니다. 결제 id: {payment.id}"
            )

        self._orders.mark_paid(order_id, payment.id)
        # 결제 확정과 동시에 예약 재고를 실제 차감합니다.
        for reservation_id in order.reservation_ids:
            self._inventory.confirm(reservation_id)
        if idem_key is not None:
            self._idempotency[idem_key] = payment.id
        return payment

    def get(self, payment_id: str) -> Payment:
        payment = self._payments.find_by_id(payment_id)
        if payment is None:
            raise DomainException(ErrorCode.NOT_FOUND, f"결제를 찾을 수 없습니다: {payment_id}")
        return payment

    def refund(self, payment_id: str) -> Payment:
        payment = self.get(payment_id)
        payment.refund()
        return self._payments.save(payment)
