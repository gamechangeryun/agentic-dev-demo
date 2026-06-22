"""공유 도메인 (자바 common 모듈 포팅).

수집·거래원장·분석 모듈이 함께 쓰는 계약(contract)입니다. 표준 실거래 모델
``AptTransaction`` 과 원천 금액 문자열을 원 단위로 바꾸는 ``DealAmountParser`` 를 담습니다.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class AptTransaction:
    """정규화된 아파트 실거래 한 건입니다.

    금액은 원 단위 정수로 보관합니다. 원천의 거래금액은 만원 단위 콤마 문자열이므로
    ``DealAmountParser`` 로 변환해 채웁니다. ``canceled`` 가 True 이면 해제된 거래이며
    시세 집계에서 제외합니다(요구사항 AC-3).
    """

    sgg_cd: str            # 법정동 시군구코드 (5자리)
    umd_nm: str            # 법정동(읍면동)명
    apt_nm: str            # 단지명
    exclusive_area: float  # 전용면적(㎡)
    floor: int             # 층
    build_year: int        # 건축년도
    deal_year: int
    deal_month: int
    deal_day: int
    deal_amount_won: int   # 거래금액 (원 단위)
    canceled: bool         # 해제여부 (cdealType == "O")

    def natural_key(self) -> str:
        """자연키: 동일 거래를 멱등 적재하기 위한 식별자입니다(요구사항 AC-4)."""
        return "|".join(str(part) for part in (
            self.sgg_cd, self.umd_nm, self.apt_nm,
            self.exclusive_area, self.floor,
            self.deal_year, self.deal_month, self.deal_day,
            self.deal_amount_won,
        ))

    def price_per_square_meter(self) -> int:
        """㎡당 단가(원)를 계산합니다. 전용면적이 0 이하이면 0을 반환합니다.

        자바 ``Math.round`` 와 동일하게 0.5 는 위(양의 무한대)로 올림합니다.
        파이썬 ``round`` 는 은행가 반올림이므로 명시적으로 ``floor(x + 0.5)`` 를 씁니다.
        """
        if self.exclusive_area <= 0:
            return 0
        import math
        return math.floor(self.deal_amount_won / self.exclusive_area + 0.5)


class DealAmountParser:
    """data.go.kr(MOLIT) 실거래가 API의 거래금액 파서입니다 (자바 DealAmountParser 포팅).

    원천 ``dealAmount`` 는 만원 단위의 콤마 포함 문자열입니다(예: " 82,500").
    공백·콤마 제거 후 만원 정수로 만들고, 다시 원 단위(×10000)로 변환합니다.
    이 변환은 요구사항 AC-3의 핵심 정합 규칙이며, 수집 모듈이 적재 전에 반드시 거칩니다.
    """

    @staticmethod
    def to_won(raw_deal_amount: str) -> int:
        """만원 단위 콤마 문자열을 원 단위 정수로 변환합니다."""
        return DealAmountParser.to_man_won(raw_deal_amount) * 10_000

    @staticmethod
    def to_man_won(raw_deal_amount: str) -> int:
        """만원 단위 콤마 문자열을 만원 정수로 변환합니다."""
        if raw_deal_amount is None or raw_deal_amount.strip() == "":
            raise ValueError("거래금액이 비어 있습니다.")
        digits = raw_deal_amount.replace(",", "").strip()
        try:
            return int(digits)
        except ValueError as exc:
            raise ValueError(f"거래금액 파싱 실패: '{raw_deal_amount}'") from exc
