"""거래원장 멱등 적재 검증 (자바 IdempotentUpsertTest 포팅).

AC-4(멱등): 같은 배치를 두 번 적재해도 중복이 생기지 않습니다.
"""

from realfield.common import AptTransaction
from realfield.transaction import InMemoryAptTradeStore, TransactionCommandService


def sample() -> AptTransaction:
    return AptTransaction("11110", "청운동", "경복궁아파트", 84.97, 10, 2003,
                          2024, 5, 12, 825_000_000, False)


def test_reingestion_does_not_duplicate():
    """AC-4: 동일 배치 재적재 시 두 번째는 0건만 삽입합니다."""
    store = InMemoryAptTradeStore()
    service = TransactionCommandService(store)
    batch = [sample()]

    first = service.upsert_all(batch)
    second = service.upsert_all(batch)

    assert first == 1, "최초 적재는 1건"
    assert second == 0, "재수집은 중복 0건"
    assert store.size() == 1, "원장에는 1건만 남는다"


def test_price_per_square_meter():
    """㎡당 단가: 자바 Math.round(floor(x+0.5)) 와 동일한 반올림으로 계산합니다."""
    import math
    tx = sample()  # 8.25억 / 84.97㎡
    expected = math.floor(825_000_000 / 84.97 + 0.5)
    assert tx.price_per_square_meter() == expected


def test_price_per_square_meter_zero_area():
    """전용면적이 0이면 ㎡당 단가는 0입니다."""
    tx = AptTransaction("11110", "청운동", "X", 0.0, 1, 2000,
                        2024, 5, 1, 500_000_000, False)
    assert tx.price_per_square_meter() == 0
