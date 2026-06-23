package kr.elice.realfield.ingestion.domain;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.common.DealAmountParser;
import kr.elice.realfield.ingestion.client.MolitAptTradeItem;
import org.springframework.stereotype.Component;

/**
 * 원천 item → 표준 {@link AptTransaction} 정규화 (AC-1) + 정합 변환 (AC-3).
 *
 * <p>금액 변환·해제 판정은 {@link DealAmountParser} 한곳에 위임한다(정합 규칙 단일 강제).
 * 정규화기는 문자열 → 타입 변환과 표준 필드 사상만 책임진다.
 */
@Component
public class AptTransactionNormalizer {

    /** 원천 item 한 건을 표준 거래 스키마로 변환한다. */
    public AptTransaction normalize(MolitAptTradeItem raw) {
        long dealAmountWon = DealAmountParser.toWon(raw.dealAmount());
        boolean canceled = DealAmountParser.isCanceled(raw.cdealType());
        return new AptTransaction(
                trim(raw.sggCd()),
                trim(raw.umdNm()),
                trim(raw.aptNm()),
                parseDouble(raw.excluUseAr()),
                parseInt(raw.floor()),
                parseIntOrZero(raw.buildYear()),
                parseInt(raw.dealYear()),
                parseInt(raw.dealMonth()),
                parseInt(raw.dealDay()),
                dealAmountWon,
                canceled
        );
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static double parseDouble(String s) {
        return Double.parseDouble(s.trim());
    }

    private static int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    /** 건축년도 등 결측 허용 정수: 공백·null 이면 0(미상)으로 둔다. */
    private static int parseIntOrZero(String s) {
        if (s == null || s.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(s.trim());
    }
}
