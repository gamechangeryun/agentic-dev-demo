"""수집·정규화 (자바 ingestion-service 포팅).

자바에서는 data.go.kr(MOLIT) OpenAPI 를 WebClient 로 호출했지만, 외부 의존이므로
파이썬 포팅에서는 동일 스키마의 샘플 데이터 적재로 대체합니다. 정규화 규칙(AC-1·AC-3)은
자바와 동등하게 보존합니다.

  - AC-1: 원천 raw item 을 표준 AptTransaction 으로 정규화합니다.
  - AC-3: 거래금액 콤마 문자열을 원 단위로 변환합니다.
  - AC-3: cdealType == "O"(해제)이면 canceled=True 로 표시해 집계에서 제외할 수 있게 합니다.
"""

from __future__ import annotations

from dataclasses import dataclass

from .common import AptTransaction, DealAmountParser
from .transaction import TransactionCommandService


@dataclass(frozen=True)
class MolitAptTradeItem:
    """data.go.kr 아파트 매매 실거래가 API 응답의 <item> 한 건 (자바 MolitAptTradeItem 포팅).

    필드명은 실제 OpenAPI 응답 요소명과 맞춥니다(가공 전 raw). 정규화는 Normalizer 가 담당합니다.
    """

    sgg_cd: str
    umd_nm: str
    jibun: str
    apt_nm: str
    exclu_use_ar: str
    deal_year: str
    deal_month: str
    deal_day: str
    deal_amount: str
    floor: str
    build_year: str
    dealing_gbn: str
    cdeal_type: str | None = None
    cdeal_day: str | None = None


class AptTransactionNormalizer:
    """원천 raw item 을 표준 AptTransaction 으로 정규화합니다 (자바 AptTransactionNormalizer 포팅)."""

    def normalize(self, raw: MolitAptTradeItem) -> AptTransaction:
        canceled = self._safe_trim(raw.cdeal_type).upper() == "O"
        return AptTransaction(
            sgg_cd=self._safe_trim(raw.sgg_cd),
            umd_nm=self._safe_trim(raw.umd_nm),
            apt_nm=self._safe_trim(raw.apt_nm),
            exclusive_area=self._parse_float(raw.exclu_use_ar),
            floor=self._parse_int(raw.floor),
            build_year=self._parse_int(raw.build_year),
            deal_year=self._parse_int(raw.deal_year),
            deal_month=self._parse_int(raw.deal_month),
            deal_day=self._parse_int(raw.deal_day),
            deal_amount_won=DealAmountParser.to_won(raw.deal_amount),
            canceled=canceled,
        )

    @staticmethod
    def _safe_trim(value: str | None) -> str:
        return "" if value is None else value.strip()

    @staticmethod
    def _parse_int(value: str | None) -> int:
        return 0 if value is None or value.strip() == "" else int(value.strip())

    @staticmethod
    def _parse_float(value: str | None) -> float:
        return 0.0 if value is None or value.strip() == "" else float(value.strip())


class SampleMolitSource:
    """MOLIT OpenAPI 대체 샘플 소스입니다.

    자바 MolitApiClient(외부 HTTP 호출)를 대신해, 동일 스키마의 raw item 목록을 돌려줍니다.
    데모는 외부 인증키·네트워크 없이도 재현 가능해야 하므로 인메모리 샘플로 고정합니다.
    """

    def fetch_apt_trades(self, lawd_cd: str, deal_ymd: str) -> list[MolitAptTradeItem]:
        # deal_ymd 예: "202405" (계약년월). 샘플은 시군구 11110(서울 종로구) 2024-05.
        return [
            MolitAptTradeItem("11110", "청운동", "123", "경복궁아파트", "84.97",
                              "2024", "5", "12", " 70,000", "10", "2003", "중개거래"),
            MolitAptTradeItem("11110", "청운동", "200", "청운현대", "59.92",
                              "2024", "5", "20", " 55,000", "7", "2010", "중개거래"),
            MolitAptTradeItem("11110", "사직동", "55", "사직파크", "114.50",
                              "2024", "5", "8", " 92,000", "15", "2018", "중개거래"),
            # 해제거래(이상치): canceled 로 표시돼 집계에서 빠져야 합니다(AC-3).
            MolitAptTradeItem("11110", "청운동", "123", "경복궁아파트", "84.97",
                              "2024", "5", "28", " 500,000", "10", "2003", "중개거래",
                              cdeal_type="O", cdeal_day="24.06.01"),
        ]


class IngestionService:
    """수집 오케스트레이션입니다 (자바 IngestionService 포팅).

    원천을 받아 표준 스키마로 정규화한 뒤, 거래원장에 멱등 적재를 요청합니다(AC-4).
    """

    def __init__(self, source: SampleMolitSource,
                 normalizer: AptTransactionNormalizer,
                 transactions: TransactionCommandService) -> None:
        self._source = source
        self._normalizer = normalizer
        self._transactions = transactions

    def ingest(self, lawd_cd: str, deal_ymd: str) -> int:
        """한 시군구·계약월을 수집해 적재한 건수를 돌려줍니다."""
        normalized = [self._normalizer.normalize(raw)
                      for raw in self._source.fetch_apt_trades(lawd_cd, deal_ymd)]
        if not normalized:
            return 0
        # 거래원장이 자연키로 멱등 upsert 합니다(AC-4). 재수집해도 중복이 생기지 않습니다.
        return self._transactions.upsert_all(normalized)
