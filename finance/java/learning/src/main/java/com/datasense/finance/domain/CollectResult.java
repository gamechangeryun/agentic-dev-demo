package com.datasense.finance.domain;

/**
 * 연계 게이트웨이 수집 결과. source: agency | fallback. (base_adapter.py 포팅)
 */
public record CollectResult(
        String docCode,
        Object data,
        String source,
        int attempts,
        boolean fallback,
        boolean degraded) {
}
