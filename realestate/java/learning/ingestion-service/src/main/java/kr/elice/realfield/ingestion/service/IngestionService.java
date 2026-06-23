package kr.elice.realfield.ingestion.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.ingestion.client.MolitAptTradeItem;
import kr.elice.realfield.ingestion.client.MolitTradeSource;
import kr.elice.realfield.ingestion.domain.AptTransactionNormalizer;

/**
 * 수집 오케스트레이션 (AC-1). 원천에서 거래를 받아 표준 스키마로 정규화한 뒤,
 * transaction-service에 멱등 적재를 요청하고 신규 적재 건수를 반환한다.
 *
 * <p>원천(운영 data.go.kr / stub)은 {@link MolitTradeSource}로 추상화돼 있고,
 * 적재는 {@code lb://transaction-service}로 위임한다(CQRS write model 분리).
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final MolitTradeSource source;
    private final AptTransactionNormalizer normalizer;
    private final WebClient transactionClient;

    public IngestionService(
            MolitTradeSource source,
            AptTransactionNormalizer normalizer,
            WebClient.Builder loadBalancedWebClientBuilder) {
        this.source = source;
        this.normalizer = normalizer;
        this.transactionClient = loadBalancedWebClientBuilder
                .baseUrl("http://transaction-service")
                .build();
    }

    /**
     * 한 시군구·계약월을 수집·정규화·적재한다.
     * 변환 실패(품질 게이트) 건은 건너뛰고, 적재는 transaction-service의 멱등 upsert에 위임한다.
     *
     * @return 신규로 적재된 건수(재수집 시 멱등 0)
     */
    public int ingest(String lawdCd, String dealYmd) {
        List<MolitAptTradeItem> raw = source.fetchAll(lawdCd, dealYmd);

        List<AptTransaction> normalized = raw.stream()
                .map(this::normalizeQuietly)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (normalized.isEmpty()) {
            return 0;
        }

        UpsertResult result = transactionClient.post()
                .uri("/api/v1/transactions")
                .bodyValue(normalized)
                .retrieve()
                .bodyToMono(UpsertResult.class)
                .block();

        return result == null ? 0 : result.upserted();
    }

    /** 변환 실패는 적재하지 않고 스킵·보고한다(데이터 품질 게이트). */
    private AptTransaction normalizeQuietly(MolitAptTradeItem raw) {
        try {
            return normalizer.normalize(raw);
        } catch (RuntimeException e) {
            log.warn("정규화 스킵 aptNm={} 사유={}", raw.aptNm(), e.toString());
            return null;
        }
    }

    /** transaction-service 적재 응답(신규 건수). */
    public record UpsertResult(int upserted) {
    }
}
