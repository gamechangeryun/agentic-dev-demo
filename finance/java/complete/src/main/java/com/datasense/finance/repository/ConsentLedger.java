package com.datasense.finance.repository;

import com.datasense.finance.domain.ConsentRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 동의 원장: 부여·철회·만료를 append-only로 기록 (AC-5).
 * 최신 레코드가 현재 상태를 결정한다. (shared/consent_ledger.py 포팅, 인메모리)
 */
@Repository
public class ConsentLedger {

    private final List<ConsentRecord> records = new ArrayList<>();

    private synchronized ConsentRecord append(String userId, String scope, String status) {
        ConsentRecord rec = new ConsentRecord(userId, scope, status, records.size() + 1);
        records.add(rec);
        return rec;
    }

    public ConsentRecord grant(String userId, String scope) {
        return append(userId, scope, "granted");
    }

    public ConsentRecord withdraw(String userId, String scope) {
        return append(userId, scope, "withdrawn");
    }

    public ConsentRecord expire(String userId, String scope) {
        return append(userId, scope, "expired");
    }

    /** 최신 레코드가 granted면 활성. */
    public synchronized boolean isActive(String userId, String scope) {
        ConsentRecord latest = null;
        for (ConsentRecord r : records) {
            if (r.userId().equals(userId) && r.scope().equals(scope)) {
                latest = r;
            }
        }
        return latest != null && "granted".equals(latest.status());
    }

    public synchronized List<ConsentRecord> records() {
        return new ArrayList<>(records);
    }

    public synchronized int count() {
        return records.size();
    }
}
