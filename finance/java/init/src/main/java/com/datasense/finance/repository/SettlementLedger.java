package com.datasense.finance.repository;

import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

/**
 * 발급 수수료 정산 원장 (AC-4 멱등): 이미 정산한 idempotency_key 집합.
 * (settlement/batch.py 의 SettlementLedger 포팅, 인메모리)
 */
@Repository
public class SettlementLedger {

    private final Set<String> settledKeys = new HashSet<>();

    public synchronized boolean alreadySettled(String key) {
        return settledKeys.contains(key);
    }

    public synchronized void markSettled(String key) {
        settledKeys.add(key);
    }
}
