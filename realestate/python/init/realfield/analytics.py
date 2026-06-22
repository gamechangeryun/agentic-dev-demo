"""시세 통계/추정 = '이스티메이트' (자바 analytics-service 포팅, read model / CQRS).

이 모듈이 파이썬 포팅의 중심입니다. 거래원장에서 거래를 읽어와 조회에 최적화된
집계(read model)로 돌려줍니다. 핵심 도메인 규칙 세 가지를 자바와 동등하게 보존합니다.

  - AC-3: 해제된 거래(canceled)는 집계에서 제외합니다.
  - 중위가격: 정렬 후 가운데 값(짝수면 두 가운데 값의 평균)으로 계산합니다.
  - ㎡당 단가: 각 거래의 ㎡당 단가를 구한 뒤 그 중위값을 돌려줍니다.
"""

from __future__ import annotations

from dataclasses import dataclass

from .common import AptTransaction
from .transaction import TransactionCommandService


@dataclass(frozen=True)
class MarketStat:
    """시세 통계 read model입니다 (자바 MarketStat 레코드 포팅).

    거래원장을 그대로 노출하지 않고, 조회에 최적화된 집계만 돌려줍니다.
    """

    sgg_cd: str
    deal_year: int
    deal_month: int
    trade_count: int            # 집계 대상 거래 수 (해제거래 제외)
    median_price_won: int       # 중위 거래금액(원)
    median_price_per_m2_won: int  # 중위 ㎡당 단가(원)

    @staticmethod
    def empty(sgg_cd: str, deal_year: int, deal_month: int) -> "MarketStat":
        return MarketStat(sgg_cd, deal_year, deal_month, 0, 0, 0)


class MarketStatCalculator:
    """거래 목록에서 시세 통계를 계산하는 순수 도메인 로직입니다 (자바 MarketStatCalculator 포팅)."""

    def calculate(self, sgg_cd: str, deal_year: int, deal_month: int,
                  transactions: list[AptTransaction]) -> MarketStat:
        valid = [t for t in transactions if not t.canceled]  # AC-3: 해제거래 제외

        if not valid:
            return MarketStat.empty(sgg_cd, deal_year, deal_month)

        median_price = self._median(sorted(t.deal_amount_won for t in valid))
        median_per_m2 = self._median(sorted(t.price_per_square_meter() for t in valid))

        return MarketStat(sgg_cd, deal_year, deal_month, len(valid), median_price, median_per_m2)

    @staticmethod
    def _median(values: list[int]) -> int:
        """정렬된 리스트의 중위값을 반환합니다(짝수면 두 가운데 값의 정수 평균).

        자바 ``long`` 나눗셈과 동일하게 정수 나눗셈(버림)으로 평균을 냅니다.
        """
        n = len(values)
        if n == 0:
            return 0
        if n % 2 == 1:
            return values[n // 2]
        return (values[n // 2 - 1] + values[n // 2]) // 2


class MarketStatService:
    """시세 통계 조회 서비스입니다 (자바 MarketStatService 포팅).

    자바에서는 WebClient 로 transaction-service 를 원격 호출했지만, 단일 프로세스 축소판에서는
    거래원장 서비스를 직접 주입받아 집계합니다. 조회 시 집계하는 단순화는 자바 데모와 동일합니다.
    """

    def __init__(self, transactions: TransactionCommandService,
                 calculator: MarketStatCalculator | None = None) -> None:
        self._transactions = transactions
        self._calculator = calculator or MarketStatCalculator()

    def market_stat(self, sgg_cd: str, deal_year: int, deal_month: int) -> MarketStat:
        rows = self._transactions.query(sgg_cd, deal_year, deal_month)
        return self._calculator.calculate(sgg_cd, deal_year, deal_month, rows)
