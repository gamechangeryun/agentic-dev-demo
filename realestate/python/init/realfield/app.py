"""단일 프로세스 조립 (자바 MSA 7모듈을 개념적으로 합친 진입점).

자바 데모는 Eureka·Config·Gateway 위에서 ingestion → transaction → analytics 가
원격 호출로 연결됐습니다. 파이썬 축소판은 같은 도메인 경계를 모듈로 유지하되,
한 프로세스 안에서 직접 주입(wire)으로 연결합니다.

흐름: 수집(ingestion) → 거래원장 적재(transaction) → 시세 집계 조회(analytics).
"""

from __future__ import annotations

from .analytics import MarketStat, MarketStatCalculator, MarketStatService
from .ingestion import (
    AptTransactionNormalizer,
    IngestionService,
    SampleMolitSource,
)
from .transaction import InMemoryAptTradeStore, TransactionCommandService


class RealFieldApp:
    """세 도메인 서비스를 인메모리로 조립한 데모 애플리케이션입니다."""

    def __init__(self) -> None:
        store = InMemoryAptTradeStore()
        self.transactions = TransactionCommandService(store)
        self.ingestion = IngestionService(
            SampleMolitSource(), AptTransactionNormalizer(), self.transactions
        )
        self.analytics = MarketStatService(self.transactions, MarketStatCalculator())

    def ingest(self, lawd_cd: str, deal_ymd: str) -> int:
        """샘플 원천을 수집·정규화해 거래원장에 멱등 적재합니다."""
        return self.ingestion.ingest(lawd_cd, deal_ymd)

    def market_stat(self, sgg_cd: str, deal_year: int, deal_month: int) -> MarketStat:
        """거래원장에서 해당 시군구·계약월의 시세 통계(read model)를 돌려줍니다."""
        return self.analytics.market_stat(sgg_cd, deal_year, deal_month)


def main() -> None:
    app = RealFieldApp()
    inserted = app.ingest(lawd_cd="11110", deal_ymd="202405")
    stat = app.market_stat("11110", 2024, 5)
    print(f"수집·적재 건수: {inserted}")
    print("시세 통계(read model, 해제거래 제외):")
    print(f"  대상 거래 수      : {stat.trade_count}")
    print(f"  중위 거래금액(원) : {stat.median_price_won:,}")
    print(f"  중위 ㎡당 단가(원): {stat.median_price_per_m2_won:,}")


if __name__ == "__main__":
    main()
