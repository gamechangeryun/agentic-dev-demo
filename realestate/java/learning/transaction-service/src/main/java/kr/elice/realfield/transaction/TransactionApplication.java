package kr.elice.realfield.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 거래원장 서비스 부트스트랩(write model). 멱등 적재(자연키 유니크)와 조회를 제공하고
 * Eureka에 등록되어 게이트웨이(`/api/v1/transactions/**`)로 노출된다.
 */
@SpringBootApplication
public class TransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionApplication.class, args);
    }
}
