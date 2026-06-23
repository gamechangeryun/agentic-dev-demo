package kr.elice.realfield.analytics.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import kr.elice.realfield.common.AptTransaction;

/**
 * 거래원장 조회 클라이언트(CQRS read → write 조회). transaction-service의 조회 계약에만 의존하고
 * 코드 결합 없이 `lb://transaction-service`로 거래 목록을 받아온다.
 */
@Component
public class TransactionQueryClient {

    private final WebClient client;

    public TransactionQueryClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.client = loadBalancedWebClientBuilder
                .baseUrl("http://transaction-service")
                .build();
    }

    /** 시군구·계약 연월의 거래(해제 포함)를 조회한다. 집계 측에서 해제를 제외한다. */
    public List<AptTransaction> findByRegionMonth(String sggCd, int dealYear, int dealMonth) {
        List<AptTransaction> result = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/transactions")
                        .queryParam("sggCd", sggCd)
                        .queryParam("dealYear", dealYear)
                        .queryParam("dealMonth", dealMonth)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AptTransaction>>() {
                })
                .block();
        return result == null ? List.of() : result;
    }
}
