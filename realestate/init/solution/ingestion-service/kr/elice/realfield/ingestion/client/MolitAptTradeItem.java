package kr.elice.realfield.ingestion.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * data.go.kr 아파트 매매 실거래가 API 응답의 {@code <item>} 한 건을 그대로 매핑한 원천 DTO입니다.
 * 태그명은 실제 OpenAPI 응답 요소명과 일치시킵니다(가공 전 raw). 정규화는 Normalizer가 담당합니다.
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
