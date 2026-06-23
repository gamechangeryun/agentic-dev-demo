package kr.elice.realfield.ingestion.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * data.go.kr 아파트 매매 실거래가 수집 클라이언트 (AC-1 수집 · AC-2 회복력). 운영 프로필 전용(`!stub`).
 *
 * <ul>
 *   <li><b>회복력</b>: {@code @Retry}(최대 3회) + {@code @CircuitBreaker}(open 시 빈 결과 fallback).
 *       한 시군구·페이지 실패가 배치 전체를 멈추지 않는다(부분 수집, SFR-011).</li>
 *   <li><b>전량 페이징</b>: {@code pageNo}를 1부터 증가시키며 누적 건수가 {@code totalCount} 이상이 될 때까지.</li>
 *   <li><b>경계 격리</b>: 외부 응답(XML)·인증키·네트워크 의존을 이 클래스 안에만 둔다. 인증키는 환경변수로만 주입(SECR-001).</li>
 * </ul>
 */
@Component
@Profile("!stub")
public class MolitApiClient implements MolitTradeSource {

    private static final Logger log = LoggerFactory.getLogger(MolitApiClient.class);
    // config-server가 외부화한 resilience4j 인스턴스명(ingestion-service.yml: instances.molitApi)과 일치시킨다.
    private static final String RESILIENCE = "molitApi";

    private final WebClient webClient = WebClient.builder().build();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final String baseUrl;
    private final String aptTradePath;
    private final String serviceKey;
    private final int numOfRows;

    /**
     * AOP 프록시 경유 자기 호출용 self 참조. {@code fetchAll → fetchPage}는 프록시를 거쳐야
     * 페이지 단위로 {@code @Retry}/{@code @CircuitBreaker}가 적용된다(직접 {@code this} 호출은 미적용).
     */
    @Autowired
    @Lazy
    private MolitApiClient self;

    public MolitApiClient(
            @Value("${molit.base-url}") String baseUrl,
            @Value("${molit.apt-trade-path}") String aptTradePath,
            @Value("${molit.service-key:}") String serviceKey,
            @Value("${molit.num-of-rows:1000}") int numOfRows) {
        this.baseUrl = baseUrl;
        this.aptTradePath = aptTradePath;
        this.serviceKey = serviceKey;
        this.numOfRows = numOfRows;
    }

    /**
     * 한 시군구·계약월의 전체 거래를 전 페이지 수집한다.
     * 각 페이지 호출에 회복력 정책을 적용하고, 누적 건수가 {@code totalCount} 이상이면 종료한다.
     */
    @Override
    public List<MolitAptTradeItem> fetchAll(String lawdCd, String dealYmd) {
        List<MolitAptTradeItem> all = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            MolitApiResponse res = self.fetchPage(lawdCd, dealYmd, pageNo);
            if (res == null || res.body() == null) {
                break;
            }
            List<MolitAptTradeItem> items = res.body().items();
            if (items == null || items.isEmpty()) {
                break;
            }
            all.addAll(items);
            if (all.size() >= res.body().totalCount()) {
                break;
            }
            pageNo++;
        }
        return all;
    }

    /**
     * 단일 페이지 호출(회복력 적용 지점).
     * Retry 는 일시 오류를 최대 3회 재시도하고, CircuitBreaker open 동안에는
     * {@code fetchPageFallback}(빈 결과)으로 폴백해 부분 수집을 계속한다.
     */
    @Retry(name = RESILIENCE)
    @CircuitBreaker(name = RESILIENCE, fallbackMethod = "fetchPageFallback")
    public MolitApiResponse fetchPage(String lawdCd, String dealYmd, int pageNo) {
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + aptTradePath)
                .queryParam("serviceKey", serviceKey)
                .queryParam("LAWD_CD", lawdCd)
                .queryParam("DEAL_YMD", dealYmd)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .build()
                .toUriString();

        String xml = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parse(xml);
    }

    /** CircuitBreaker open / 호출 실패 시 폴백: 빈 응답으로 부분 수집을 이어간다(SFR-011). */
    @SuppressWarnings("unused") // resilience4j 가 리플렉션으로 호출
    private MolitApiResponse fetchPageFallback(String lawdCd, String dealYmd, int pageNo, Throwable t) {
        log.warn("MOLIT 수집 폴백 lawdCd={} dealYmd={} pageNo={} 사유={}", lawdCd, dealYmd, pageNo, t.toString());
        return MolitApiResponse.empty();
    }

    private MolitApiResponse parse(String xml) {
        if (xml == null || xml.isBlank()) {
            return MolitApiResponse.empty();
        }
        try {
            return xmlMapper.readValue(xml, MolitApiResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("MOLIT 응답 XML 파싱 실패", e);
        }
    }

    /** data.go.kr 응답 매핑(XML: {@code <response><header/><body/></response>}). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MolitApiResponse(
            @JacksonXmlProperty(localName = "header") Header header,
            @JacksonXmlProperty(localName = "body") Body body) {

        static MolitApiResponse empty() {
            return new MolitApiResponse(new Header("000", "EMPTY-FALLBACK"),
                    new Body(0, 1, 0, List.of()));
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Header(
                @JacksonXmlProperty(localName = "resultCode") String resultCode,
                @JacksonXmlProperty(localName = "resultMsg") String resultMsg) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Body(
                @JacksonXmlProperty(localName = "totalCount") int totalCount,
                @JacksonXmlProperty(localName = "pageNo") int pageNo,
                @JacksonXmlProperty(localName = "numOfRows") int numOfRows,
                @JacksonXmlElementWrapper(localName = "items")
                @JacksonXmlProperty(localName = "item") List<MolitAptTradeItem> items) {
        }
    }
}
