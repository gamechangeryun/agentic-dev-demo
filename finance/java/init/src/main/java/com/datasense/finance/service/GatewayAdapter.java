package com.datasense.finance.service;

import com.datasense.finance.domain.CollectResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

/**
 * 연계 게이트웨이 어댑터 (AC-2 회복력): 재시도 → 서킷브레이커 → 대체 경로.
 *
 * 기관 응답을 responder(docCode, attempt) 로 주입한다. responder가 null을 반환하면
 * '3초 내 미응답'(TimeoutError)을 시뮬레이션한다. (base_adapter.py 포팅)
 *
 * 인스턴스는 호출 단위로 상태(연속 실패·서킷)를 가지므로, 매 발급마다 새로 생성한다.
 */
@Service
public class GatewayAdapter {

    private final int maxRetries;
    private final int failureThreshold;

    public GatewayAdapter(
            @Value("${demo.gateway.max-retries:3}") int maxRetries,
            @Value("${demo.gateway.failure-threshold:3}") int failureThreshold) {
        this.maxRetries = maxRetries;
        this.failureThreshold = failureThreshold;
    }

    /** 호출 단위 회복력 상태를 들고 다니는 세션. */
    public Session newSession() {
        return new Session(maxRetries, failureThreshold);
    }

    public static class Session {
        private final int maxRetries;
        private final int failureThreshold;
        private int attempts = 0;
        private boolean fallbackUsed = false;
        private int consecutiveFailures = 0;
        private boolean circuitOpen = false;

        Session(int maxRetries, int failureThreshold) {
            this.maxRetries = maxRetries;
            this.failureThreshold = failureThreshold;
        }

        public boolean isCircuitOpen() {
            return circuitOpen;
        }

        public boolean isFallbackUsed() {
            return fallbackUsed;
        }

        /**
         * 서류 1건 수집. responder가 null을 반환하면 미응답(타임아웃)으로 간주한다.
         *
         * @param responder    (docCode, attempt) -> 응답 데이터, null이면 타임아웃
         * @param fallbackRoute docCode -> 대체 데이터 (없으면 null 전달)
         */
        public CollectResult collect(String docCode,
                                     BiFunction<String, Integer, Object> responder,
                                     java.util.function.Function<String, Object> fallbackRoute) {
            if (circuitOpen) {
                return fallback(docCode, fallbackRoute);
            }
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                attempts++;
                Object data = responder.apply(docCode, attempt);
                if (data == null) { // TimeoutError 시뮬레이션
                    consecutiveFailures++;
                    if (consecutiveFailures >= failureThreshold) {
                        circuitOpen = true;
                        break;
                    }
                    continue;
                }
                consecutiveFailures = 0;
                return new CollectResult(docCode, data, "agency", attempt, false, false);
            }
            return fallback(docCode, fallbackRoute);
        }

        private CollectResult fallback(String docCode,
                                       java.util.function.Function<String, Object> fallbackRoute) {
            fallbackUsed = true;
            Object data = fallbackRoute != null ? fallbackRoute.apply(docCode) : null;
            return new CollectResult(docCode, data, "fallback", attempts, true, true);
        }
    }
}
