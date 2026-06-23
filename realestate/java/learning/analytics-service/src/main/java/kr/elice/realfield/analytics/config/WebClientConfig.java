package kr.elice.realfield.analytics.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 거래원장 조회용 로드밸런싱 WebClient 빌더. `http://transaction-service/...`로 호출하면
 * Eureka 등록 인스턴스로 분산 라우팅된다(CQRS read → write 조회).
 */
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
