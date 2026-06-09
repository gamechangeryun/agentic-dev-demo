package kr.elice.realfield.ingestion;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.ingestion.client.MolitAptTradeItem;
import kr.elice.realfield.ingestion.domain.AptTransactionNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** AC-1(정규화) · AC-3(정합): 원천 item을 표준 스키마로 바르게 변환하고 해제거래를 표시합니다. */
class AptTransactionNormalizerTest {

    private final AptTransactionNormalizer normalizer = new AptTransactionNormalizer();

    @Test
    @DisplayName("AC-1: 원천 item을 표준 AptTransaction으로 정규화한다")
    void normalizesRawItem() {
        MolitAptTradeItem raw = new MolitAptTradeItem(
                "11110", "청운동", "123", "경복궁아파트", "84.97",
                "2024", "5", "12", " 82,500", "10", "2003",
                "중개거래", null, null);

        AptTransaction tx = normalizer.normalize(raw);

        assertEquals("11110", tx.sggCd());
        assertEquals("경복궁아파트", tx.aptNm());
        assertEquals(84.97, tx.exclusiveArea(), 0.001);
        assertEquals(825_000_000L, tx.dealAmountWon());
        assertFalse(tx.canceled());
    }

    @Test
    @DisplayName("AC-3: cdealType=O(해제) 거래는 canceled=true 로 표시한다")
    void marksCanceledDeal() {
        MolitAptTradeItem raw = new MolitAptTradeItem(
                "11110", "청운동", "123", "경복궁아파트", "84.97",
                "2024", "5", "12", " 82,500", "10", "2003",
                "중개거래", "O", "24.06.01");

        AptTransaction tx = normalizer.normalize(raw);

        assertTrue(tx.canceled());
    }
}
