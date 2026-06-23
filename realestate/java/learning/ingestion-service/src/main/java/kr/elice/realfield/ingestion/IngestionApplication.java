package kr.elice.realfield.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 수집 서비스 부트스트랩. data.go.kr 수집·정규화 후 transaction-service에 멱등 적재를 요청한다.
 * Eureka에 등록되어 게이트웨이(`/api/v1/ingest/**`)로 노출된다.
 */
@SpringBootApplication
public class IngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(IngestionApplication.class, args);
    }
}
