package kr.elice.realfield.ingestion.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 서비스 간 호출용 로드밸런싱 WebClient 빌더. `http://transaction-service/...`처럼 서비스명을
 * 호스트로 쓰면 Eureka 등록 인스턴스로 분산 라우팅된다(lb). 외부(data.go.kr) 호출은 별도(MolitApiClient).
 */
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
