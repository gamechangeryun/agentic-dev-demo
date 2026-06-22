"""엔드투엔드 수용기준: 수집 → 적재 → 시세 조회 전 흐름을 한 번에 검증합니다.

MSA 인프라 없이 단일 프로세스로 합쳤지만, 도메인 경계(수집·원장·집계)를 거치는
핵심 시나리오는 자바 데모와 동등하게 동작해야 합니다.
"""

from realfield.app import RealFieldApp


def test_ingest_then_market_stat():
    """수집한 샘플로 시세 통계를 조회하면 해제거래를 제외한 중위값이 나옵니다.

    샘플 4건 중 1건은 해제거래(50억 이상치)이므로 집계는 유효 3건 기준입니다.
    유효 금액: 7억, 5.5억, 9.2억 → 중위 7억(원).
    """
    app = RealFieldApp()
    inserted = app.ingest(lawd_cd="11110", deal_ymd="202405")
    assert inserted == 4, "샘플 4건이 멱등 적재된다"

    stat = app.market_stat("11110", 2024, 5)
    assert stat.trade_count == 3, "해제거래 1건은 집계에서 제외된다"
    assert stat.median_price_won == 700_000_000, "유효 3건의 중위 거래금액"
    assert stat.median_price_per_m2_won > 0, "㎡당 단가 중위값이 계산된다"


def test_reingestion_is_idempotent():
    """같은 계약월을 두 번 수집해도 원장은 중복되지 않습니다(AC-4)."""
    app = RealFieldApp()
    first = app.ingest("11110", "202405")
    second = app.ingest("11110", "202405")
    assert first == 4
    assert second == 0, "재수집은 0건만 추가"

    stat = app.market_stat("11110", 2024, 5)
    assert stat.trade_count == 3, "재수집해도 집계 결과는 동일"
