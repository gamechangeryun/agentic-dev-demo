package kr.elice.realfield.common;

/**
 * 내부 표준 거래 스키마(04_data 정본).
 *
 * <p>원천 item(data.go.kr)을 정규화한 결과이며, 수집·적재·집계 세 도메인이 공유하는 단일 계약이다.
 * 금액은 원 단위 정수({@code dealAmountWon}), 해제는 boolean({@code canceled})으로 정규화되어 있다.
 */
public record AptTransaction(
        String sggCd,
        String umdNm,
        String aptNm,
        double exclusiveArea,
        int floor,
        int buildYear,
        int dealYear,
        int dealMonth,
        int dealDay,
        long dealAmountWon,
        boolean canceled
) {
    /**
     * 거래 동일성 자연키(AC-4 멱등 기준).
     *
     * <p>같은 시군구·법정동·단지·전용면적·층·계약일(연·월·일)·거래금액이면 동일 신고 거래로 본다.
     * 적재 측(transaction-service)은 이 키로 재수집 중복을 차단한다. 해제 여부·건축년도는 거래 동일성이
     * 아니므로 키에서 제외한다(원천에 aptSeq가 없어 단지 식별은 umdNm·aptNm으로 대체).
     */
    public String naturalKey() {
        return String.join("|",
                sggCd, umdNm, aptNm,
                String.valueOf(exclusiveArea),
                String.valueOf(floor),
                String.valueOf(dealYear),
                String.valueOf(dealMonth),
                String.valueOf(dealDay),
                String.valueOf(dealAmountWon));
    }
}
