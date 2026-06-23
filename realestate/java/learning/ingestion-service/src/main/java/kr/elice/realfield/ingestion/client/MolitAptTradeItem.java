package kr.elice.realfield.ingestion.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * data.go.kr 아파트 매매 실거래가 원천 item(상세 엔드포인트).
 *
 * <p>모든 값은 XML 텍스트(String)로 내려오며(숫자 항목도 문자열), 표준 스키마로의 변환은
 * {@code AptTransactionNormalizer}가 담당한다. 외부 응답 형태 의존은 이 경계(client) 안에만 둔다.
 * 컴포넌트 순서는 정규화 테스트 픽스처와 일치시킨다. (00_sources §4.3)
 */
public record MolitAptTradeItem(
        @JacksonXmlProperty(localName = "sggCd") String sggCd,
        @JacksonXmlProperty(localName = "umdNm") String umdNm,
        @JacksonXmlProperty(localName = "jibun") String jibun,
        @JacksonXmlProperty(localName = "aptNm") String aptNm,
        @JacksonXmlProperty(localName = "excluUseAr") String excluUseAr,
        @JacksonXmlProperty(localName = "dealYear") String dealYear,
        @JacksonXmlProperty(localName = "dealMonth") String dealMonth,
        @JacksonXmlProperty(localName = "dealDay") String dealDay,
        @JacksonXmlProperty(localName = "dealAmount") String dealAmount,
        @JacksonXmlProperty(localName = "floor") String floor,
        @JacksonXmlProperty(localName = "buildYear") String buildYear,
        @JacksonXmlProperty(localName = "dealingGbn") String dealingGbn,
        @JacksonXmlProperty(localName = "cdealType") String cdealType,
        @JacksonXmlProperty(localName = "cdealDay") String cdealDay
) {
}
