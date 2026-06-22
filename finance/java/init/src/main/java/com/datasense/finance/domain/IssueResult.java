package com.datasense.finance.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 전자민원 발급 결과. status: issued | rejected. (issue_svc.py 의 IssueResult 포팅)
 */
public class IssueResult {
    private String status;
    private String reason = "";
    private List<String> documents = new ArrayList<>();
    private List<String> citations = new ArrayList<>();
    private String idempotencyKey = "";
    private boolean replay = false;
    private boolean degraded = false;

    public static IssueResult rejected(String reason, List<String> citations) {
        IssueResult r = new IssueResult();
        r.status = "rejected";
        r.reason = reason;
        if (citations != null) {
            r.citations = citations;
        }
        return r;
    }

    public static IssueResult issued(List<String> documents, List<String> citations,
                                     String idempotencyKey, boolean degraded) {
        IssueResult r = new IssueResult();
        r.status = "issued";
        r.documents = documents;
        r.citations = citations;
        r.idempotencyKey = idempotencyKey;
        r.degraded = degraded;
        return r;
    }

    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public List<String> getDocuments() { return documents; }
    public List<String> getCitations() { return citations; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public boolean isReplay() { return replay; }
    public boolean isDegraded() { return degraded; }

    public void setReplay(boolean replay) { this.replay = replay; }
}
