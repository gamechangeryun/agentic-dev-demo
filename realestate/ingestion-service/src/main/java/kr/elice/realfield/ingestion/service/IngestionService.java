package kr.elice.realfield.ingestion.service;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.ingestion.client.MolitApiClient;
import kr.elice.realfield.ingestion.domain.AptTransactionNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 수집 오케스트레이션입니다. data.go.kr에서 원천을 받아 표준 스키마로 정규화한 뒤,
 * 거래원장(transaction-service)에 멱등 적재를 요청합니다.
 */
@Service
public class IngestionService {

    private final MolitApiClient molitApiClient;
    private final AptTransactionNormalizer normalizer;
    private final WebClient transactionClient;

    public IngestionService(MolitApiClient molitApiClient,
                            AptTransactionNormalizer normalizer,
                            WebClient.Builder builder,
                            @Value("${transaction.base-url}") String transactionBaseUrl) {
        this.molitApiClient = molitApiClient;
        this.normalizer = normalizer;
        this.transactionClient = builder.baseUrl(transactionBaseUrl).build();
    }

    /** 한 시군구·계약월을 수집해 적재한 건수를 돌려줍니다. */
    public int ingest(String lawdCd, String dealYmd) {
        List<AptTransaction> normalized = molitApiClient.fetchAptTrades(lawdCd, dealYmd).stream()
                .map(normalizer::normalize)
                .toList();

        if (normalized.isEmpty()) return 0;

        // 거래원장이 자연키로 멱등 upsert 합니다(AC-4). 재수집해도 중복이 생기지 않습니다.
        Integer upserted = transactionClient.post()
                .uri("/api/v1/transactions/bulk")
                .bodyValue(normalized)
                .retrieve()
                .bodyToMono(Integer.class)
                .block();

        return upserted == null ? 0 : upserted;
    }
}
