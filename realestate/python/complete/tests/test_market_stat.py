"""시세 통계 도메인 규칙 검증 (자바 MarketStatCalculatorTest 포팅).

AC-5(CQRS 집계) · AC-3(해제 제외): read model 이 중위가격을 정확히, 해제거래는 빼고 계산합니다.
"""

from realfield.analytics import MarketStatCalculator
from realfield.common import AptTransaction


def tx(won: int, canceled: bool, area: float = 84.97) -> AptTransaction:
    return AptTransaction("11110", "청운동", "경복궁아파트", area, 10, 2003,
                          2024, 5, 12, won, canceled)


def test_median_of_odd_count():
    """AC-5: 홀수 건의 중위 거래금액을 반환합니다."""
    calc = MarketStatCalculator()
    stat = calc.calculate("11110", 2024, 5, [
        tx(700_000_000, False),
        tx(900_000_000, False),
        tx(800_000_000, False),
    ])
    assert stat.trade_count == 3
    assert stat.median_price_won == 800_000_000


def test_excludes_canceled_deals():
    """AC-3: 해제거래(canceled=True)는 집계에서 제외합니다."""
    calc = MarketStatCalculator()
    stat = calc.calculate("11110", 2024, 5, [
        tx(700_000_000, False),
        tx(800_000_000, False),
        tx(5_000_000_000, True),  # 해제 이상치: 제외돼야 합니다.
    ])
    assert stat.trade_count == 2
    assert stat.median_price_won == 750_000_000  # (700+800)/2


def test_empty_when_no_trades():
    """거래가 없으면 빈 통계를 반환합니다."""
    calc = MarketStatCalculator()
    stat = calc.calculate("11110", 2024, 5, [])
    assert stat.trade_count == 0
    assert stat.median_price_won == 0


def test_median_price_per_square_meter():
    """㎡당 단가: 각 거래 ㎡당 단가의 중위값을 돌려줍니다.

    면적 100㎡, 금액 7억/8억/9억 → ㎡당 700만/800만/900만 → 중위 800만(원).
    """
    calc = MarketStatCalculator()
    stat = calc.calculate("11110", 2024, 5, [
        tx(700_000_000, False, area=100.0),
        tx(800_000_000, False, area=100.0),
        tx(900_000_000, False, area=100.0),
    ])
    assert stat.median_price_per_m2_won == 8_000_000
