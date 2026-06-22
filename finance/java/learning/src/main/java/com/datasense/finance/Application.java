package com.datasense.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyLink 금융·공공 데모 (강의 13강).
 *
 * 파이썬 데모(server/)의 "동의 완료 후 전자민원 자동 발급" 흐름을 Spring Boot로 포팅.
 * 흐름: 동의 확인(AC-1) → 자격·예외 가드레일(AC-3) → 서류 산출 → 기관 수집(AC-2)
 *       → 멱등 발급(AC-4). 동의 철회 시 처리 중단·파기(AC-5).
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
