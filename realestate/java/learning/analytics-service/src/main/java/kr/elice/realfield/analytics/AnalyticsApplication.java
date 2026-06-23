package kr.elice.realfield.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 시세 통계 서비스 부트스트랩(read model · CQRS). 거래원장을 조회해 집계만 하고,
 * Eureka에 등록되어 게이트웨이(`/api/v1/market-stats/**`)로 노출된다.
 */
@SpringBootApplication
public class AnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
