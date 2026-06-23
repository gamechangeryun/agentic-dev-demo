package kr.elice.realfield.common;

/**
 * AC-3(데이터 정합): 거래금액 정합 변환의 단일 강제점.
 *
 * <p>data.go.kr {@code dealAmount}(만원 단위·천 단위 콤마·선행 공백 포함 문자열)을
 * 원 단위 정수(long)로 변환한다. 수집(ingestion)과 집계(analytics)가 같은 규칙을 쓰도록
 * 정합 변환은 이 한곳(common)에서만 강제한다. (04_data §3·§4)
 */
public final class DealAmountParser {

    private DealAmountParser() {
    }

    /**
     * 만원 단위 거래금액 문자열을 원 단위 정수로 변환한다.
     * <pre>"  82,500" → "82500" → 82,500 → ×10,000 → 825,000,000(원)</pre>
     *
     * @throws IllegalArgumentException 값이 비었거나 숫자가 아니거나 0 이하인 경우(품질 게이트)
     */
    public static long toWon(String dealAmount) {
        if (dealAmount == null) {
            throw new IllegalArgumentException("거래금액이 null 입니다");
        }
        String digits = dealAmount.replace(",", "").replace(" ", "").trim();
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("거래금액이 비어 있습니다: '" + dealAmount + "'");
        }
        long manWon;
        try {
            manWon = Long.parseLong(digits);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("거래금액 형식 오류: '" + dealAmount + "'", e);
        }
        if (manWon <= 0) {
            throw new IllegalArgumentException("거래금액은 0보다 커야 합니다: '" + dealAmount + "'");
        }
        return manWon * 10_000L;
    }

    /**
     * 해제 여부 코드({@code cdealType})를 boolean 으로 변환한다.
     * {@code "O"}(해제)만 true, 공백·null 은 false. (04_data §4)
     */
    public static boolean isCanceled(String cdealType) {
        return cdealType != null && cdealType.trim().equalsIgnoreCase("O");
    }
}
