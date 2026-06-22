"""수집·정규화 검증 (자바 AptTransactionNormalizerTest + DealAmountParserTest 포팅).

AC-1(정규화) · AC-3(정합): 원천 item 을 표준 스키마로 바르게 변환하고 해제거래를 표시합니다.
"""

import pytest

from realfield.common import DealAmountParser
from realfield.ingestion import AptTransactionNormalizer, MolitAptTradeItem


def test_normalizes_raw_item():
    """AC-1: 원천 item 을 표준 AptTransaction 으로 정규화합니다."""
    raw = MolitAptTradeItem("11110", "청운동", "123", "경복궁아파트", "84.97",
                            "2024", "5", "12", " 82,500", "10", "2003", "중개거래")
    tx = AptTransactionNormalizer().normalize(raw)

    assert tx.sgg_cd == "11110"
    assert tx.apt_nm == "경복궁아파트"
    assert tx.exclusive_area == pytest.approx(84.97, abs=0.001)
    assert tx.deal_amount_won == 825_000_000
    assert tx.canceled is False


def test_marks_canceled_deal():
    """AC-3: cdealType=O(해제) 거래는 canceled=True 로 표시합니다."""
    raw = MolitAptTradeItem("11110", "청운동", "123", "경복궁아파트", "84.97",
                            "2024", "5", "12", " 82,500", "10", "2003", "중개거래",
                            cdeal_type="O", cdeal_day="24.06.01")
    tx = AptTransactionNormalizer().normalize(raw)
    assert tx.canceled is True


def test_deal_amount_parser_to_won():
    """AC-3: 만원 단위 콤마 문자열을 원 단위로 변환합니다."""
    assert DealAmountParser.to_won(" 82,500") == 825_000_000
    assert DealAmountParser.to_man_won(" 82,500") == 82_500


def test_deal_amount_parser_rejects_blank():
    """빈 금액은 예외를 던집니다."""
    with pytest.raises(ValueError):
        DealAmountParser.to_won("   ")
