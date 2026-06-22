"""재고 예약 서비스 단위 테스트입니다. 자바 InventoryServiceTest 를 옮긴 것입니다."""

import threading

import pytest

from shop.catalog.repository import InMemoryProductRepository
from shop.catalog.service import CatalogService
from shop.inventory.repository import InMemoryReservationRepository
from shop.inventory.service import InventoryService
from shop.shared import DomainException, ErrorCode


@pytest.fixture
def fixture():
    catalog = CatalogService(InMemoryProductRepository())
    inventory = InventoryService(catalog, InMemoryReservationRepository())
    product = catalog.create("노트북", 1_000_000, 5, None)
    return catalog, inventory, product.id


def test_reserve_holds_availability(fixture):
    """예약은 가용 재고를 줄이지만 물리 재고는 그대로다."""
    catalog, inventory, product_id = fixture
    inventory.reserve(product_id, 3)
    assert inventory.available(product_id) == 2
    assert catalog.get(product_id).stock_quantity == 5


def test_confirm_reduces_physical_stock(fixture):
    """확정은 물리 재고를 실제로 차감한다."""
    catalog, inventory, product_id = fixture
    r = inventory.reserve(product_id, 3)
    inventory.confirm(r.id)
    assert catalog.get(product_id).stock_quantity == 2
    assert inventory.available(product_id) == 2


def test_release_restores_availability(fixture):
    """해제는 예약을 풀어 가용 재고를 되돌린다."""
    catalog, inventory, product_id = fixture
    r = inventory.reserve(product_id, 3)
    inventory.release(r.id)
    assert inventory.available(product_id) == 5


def test_prevents_oversell(fixture):
    """oversell 방지: 가용분을 초과하는 예약은 거부한다."""
    catalog, inventory, product_id = fixture
    inventory.reserve(product_id, 3)
    with pytest.raises(DomainException) as exc:
        inventory.reserve(product_id, 3)
    assert exc.value.code == ErrorCode.INSUFFICIENT_STOCK


def test_concurrent_reservations_never_oversell():
    """동시 예약 100건도 물리 재고를 초과하지 않는다.

    자바의 synchronized 와 동일하게, 파이썬은 RLock 으로 reserve 를 보호한다.
    재고 50개에 100건 요청: 정확히 50건만 성공하고 가용분은 0이어야 한다.
    """
    catalog = CatalogService(InMemoryProductRepository())
    inventory = InventoryService(catalog, InMemoryReservationRepository())
    big = catalog.create("키보드", 50_000, 50, None)

    success = {"count": 0}
    success_lock = threading.Lock()

    def worker():
        try:
            inventory.reserve(big.id, 1)
            with success_lock:
                success["count"] += 1
        except DomainException:
            # 가용분 소진 후 거부는 정상 경로입니다.
            pass

    workers = [threading.Thread(target=worker) for _ in range(100)]
    for w in workers:
        w.start()
    for w in workers:
        w.join()

    assert success["count"] == 50
    assert inventory.available(big.id) == 0
