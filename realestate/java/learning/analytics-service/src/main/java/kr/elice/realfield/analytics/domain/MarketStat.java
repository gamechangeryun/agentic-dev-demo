package kr.elice.realfield.analytics.domain;

/**
 * 시세 통계 read model (AC-5 CQRS).
 *
 * <p>시군구·계약 연월 단위 집계 결과다. 거래원장(write model)을 복제하지 않고
 * 조회된 거래에서 해제거래를 제외한 뒤 산출한 중위 거래금액(원)과 중위 ㎡당 단가(원)를 담는다.
 * 프론트 계약(lib/types.ts MarketStat)과 필드명이 1:1로 대응한다.
 */
public record MarketStat(
        String sggCd,
        int dealYear,
        int dealMonth,
        int tradeCount,
        long medianPriceWon,
        long medianPricePerM2Won
) {
}
