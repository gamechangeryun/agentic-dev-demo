package kr.elice.realfield.analytics.domain;

import java.util.List;

import kr.elice.realfield.common.AptTransaction;

/**
 * 시세 통계 집계기 (AC-5 CQRS read model · AC-3 해제 제외).
 *
 * <p>조회한 거래 목록에서 해제거래(`canceled=true`)를 제외하고 중위 거래금액(원)과
 * 중위 ㎡당 단가(원)를 산출한다. 순수 도메인 계산이라 DB·네트워크 없이 결정적으로 검증된다.
 */
public class MarketStatCalculator {

    /** 시군구·계약 연월의 거래 목록에서 해제 제외 후 거래 수·중위가격·중위 ㎡당 단가를 집계한다. */
    public MarketStat calculate(String sggCd, int dealYear, int dealMonth, List<AptTransaction> transactions) {
        List<AptTransaction> valid = transactions.stream()
                .filter(t -> !t.canceled())             // AC-3: 해제거래는 집계에서 제외
                .toList();

        List<Long> prices = valid.stream()
                .map(AptTransaction::dealAmountWon)
                .sorted()
                .toList();

        List<Long> perM2 = valid.stream()
                .filter(t -> t.exclusiveArea() > 0)
                .map(t -> Math.round(t.dealAmountWon() / t.exclusiveArea()))
                .sorted()
                .toList();

        return new MarketStat(sggCd, dealYear, dealMonth, prices.size(), median(prices), median(perM2));
    }

    /** 정렬된 값의 중위값. 짝수 개면 가운데 두 값의 평균(정수), 비어 있으면 0. */
    private static long median(List<Long> sorted) {
        int n = sorted.size();
        if (n == 0) {
            return 0L;
        }
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2;
    }
}
