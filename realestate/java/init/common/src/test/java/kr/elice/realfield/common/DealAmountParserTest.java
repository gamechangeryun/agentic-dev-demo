package kr.elice.realfield.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** AC-3(데이터 정합): 거래금액 콤마 문자열을 원 단위 정수로 정확히 변환합니다. */
class DealAmountParserTest {

    @Test
    @DisplayName("AC-3: 콤마·공백 포함 만원 문자열을 원 단위로 변환한다")
    void parsesCommaSeparatedAmount() {
        assertEquals(825_000_000L, DealAmountParser.toWon(" 82,500"));
        assertEquals(1_200_000_000L, DealAmountParser.toWon("120,000"));
        assertEquals(95_000_000L, DealAmountParser.toWon("9,500"));
    }

    @Test
    @DisplayName("AC-3: 빈 값·잘못된 형식은 예외로 거부한다")
    void rejectsInvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> DealAmountParser.toWon(""));
        assertThrows(IllegalArgumentException.class, () -> DealAmountParser.toWon("팔만이천"));
    }
}
