"""애플리케이션 조립기입니다. Spring 의 DI 컨테이너 역할을 대신합니다.

6개 bounded context 의 서비스와 인메모리 저장소를 한곳에서 배선합니다. 자바의
``ShopApplication`` + Spring 컴포넌트 스캔이 하던 의존성 주입을, 외부 프레임워크
없이 명시적 생성자 호출로 옮긴 것입니다. 테스트와 데모는 이 조립기 하나로 전체
흐름을 실행합니다.
"""

from __future__ import annotations

from typing import Optional

from .cart.repository import InMemoryCartRepository
from .cart.service import CartService
from .catalog.repository import InMemoryProductRepository
from .catalog.service import CatalogService
from .checkout.service import CheckoutService
from .inventory.repository import InMemoryReservationRepository
from .inventory.service import InventoryService
from .ordering.repository import InMemoryOrderRepository
from .ordering.service import OrderService
from .payment.gateway import DemoPaymentGateway, PaymentGateway
from .payment.repository import InMemoryPaymentRepository
from .payment.service import PaymentService


class ShopApp:
    """전체 도메인을 인메모리로 배선한 애플리케이션입니다."""

    def __init__(self, gateway: Optional[PaymentGateway] = None) -> None:
        self.catalog = CatalogService(InMemoryProductRepository())
        self.inventory = InventoryService(
            self.catalog, InMemoryReservationRepository()
        )
        self.carts = CartService(InMemoryCartRepository(), self.catalog)
        self.orders = OrderService(InMemoryOrderRepository())
        self.payments = PaymentService(
            InMemoryPaymentRepository(),
            gateway if gateway is not None else DemoPaymentGateway(),
            self.orders,
            self.inventory,
        )
        self.checkout = CheckoutService(
            self.carts, self.catalog, self.inventory, self.orders, self.payments
        )
