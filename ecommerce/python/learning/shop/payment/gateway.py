"""결제 게이트웨이 포트와 데모 어댑터입니다.

도메인은 승인 여부만 알면 되므로 포트는 bool 하나로 단순합니다. 데모 어댑터는
결정적으로 동작하여 테스트에서 거절 경로를 정확히 재현합니다. 운영에서는 실제
PG 연동 어댑터로 교체합니다. 자바 ``PaymentGateway`` / ``DemoPaymentGateway`` 를
옮긴 것입니다.
"""

from __future__ import annotations

from typing import Optional, Protocol

from ..shared import Money


class PaymentGateway(Protocol):
    """결제 게이트웨이 포트입니다. 승인 여부만 돌려줍니다."""

    def authorize(self, amount: Money, method: Optional[str]) -> bool: ...


class DemoPaymentGateway:
    """데모용 결정적 결제 게이트웨이 어댑터입니다.

    결제 수단이 "declined" 이면 거절하고, 그 밖에는 금액이 양수일 때 승인합니다.
    실시간·난수에 의존하지 않으므로 거절 경로를 정확히 재현할 수 있습니다.
    """

    def authorize(self, amount: Money, method: Optional[str]) -> bool:
        if method is not None and method.lower() == "declined":
            return False
        return amount.is_positive()
