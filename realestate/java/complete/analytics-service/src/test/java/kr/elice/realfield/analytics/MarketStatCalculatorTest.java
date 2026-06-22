package kr.elice.realfield.analytics;

import kr.elice.realfield.analytics.domain.MarketStat;
import kr.elice.realfield.analytics.domain.MarketStatCalculator;
import kr.elice.realfield.common.AptTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** AC-5(CQRS 집계) · AC-3(해제 제외): read model이 중위가격을 정확히, 해제거래는 빼고 계산합니다. */
class MarketStatCalculatorTest {

    private final MarketStatCalculator calculator = new MarketStatCalculator();

    private AptTransaction tx(long won, boolean canceled) {
        return new AptTransaction("11110", "청운동", "경복궁아파트", 84.97, 10, 2003,
                2024, 5, 12, won, canceled);
    }

    @Test
    @DisplayName("AC-5: 홀수 건의 중위 거래금액을 반환한다")
    void medianOfOddCount() {
        MarketStat stat = calculator.calculate("11110", 2024, 5, List.of(
                tx(700_000_000L, false),
                tx(900_000_000L, false),
                tx(800_000_000L, false)));

        assertEquals(3, stat.tradeCount());
        assertEquals(800_000_000L, stat.medianPriceWon());
    }

    @Test
    @DisplayName("AC-3: 해제거래(canceled=true)는 집계에서 제외한다")
    void excludesCanceledDeals() {
        MarketStat stat = calculator.calculate("11110", 2024, 5, List.of(
                tx(700_000_000L, false),
                tx(800_000_000L, false),
                tx(5_000_000_000L, true)));   // 해제 이상치: 제외돼야 한다

        assertEquals(2, stat.tradeCount());
        assertEquals(750_000_000L, stat.medianPriceWon());  // (700+800)/2
    }

    @Test
    @DisplayName("거래가 없으면 빈 통계를 반환한다")
    void emptyWhenNoTrades() {
        MarketStat stat = calculator.calculate("11110", 2024, 5, List.of());
        assertEquals(0, stat.tradeCount());
        assertEquals(0L, stat.medianPriceWon());
    }
}
