package kr.elice.realfield.ingestion.domain;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.common.DealAmountParser;
import kr.elice.realfield.ingestion.client.MolitAptTradeItem;
import org.springframework.stereotype.Component;

/**
 * 원천 {@link MolitAptTradeItem}(data.go.kr raw)을 표준 {@link AptTransaction}으로 정규화합니다.
 *
 * <p>요구사항 정합 규칙을 한곳에 모읍니다.
 * <ul>
 *   <li>AC-3: 거래금액 콤마 문자열을 원 단위로 변환합니다.</li>
 *   <li>AC-3: {@code cdealType == "O"}(해제)이면 canceled=true 로 표시해 집계에서 제외할 수 있게 합니다.</li>
 * </ul>
 */
@Component
public class AptTransactionNormalizer {

    public AptTransaction normalize(MolitAptTradeItem raw) {
        boolean canceled = "O".equalsIgnoreCase(safeTrim(raw.cdealType()));
        return new AptTransaction(
                safeTrim(raw.sggCd()),
                safeTrim(raw.umdNm()),
                safeTrim(raw.aptNm()),
                parseDouble(raw.excluUseAr()),
                parseInt(raw.floor()),
                parseInt(raw.buildYear()),
                parseInt(raw.dealYear()),
                parseInt(raw.dealMonth()),
                parseInt(raw.dealDay()),
                DealAmountParser.toWon(raw.dealAmount()),
                canceled
        );
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static int parseInt(String s) {
        return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim());
    }

    private static double parseDouble(String s) {
        return (s == null || s.isBlank()) ? 0d : Double.parseDouble(s.trim());
    }
}
