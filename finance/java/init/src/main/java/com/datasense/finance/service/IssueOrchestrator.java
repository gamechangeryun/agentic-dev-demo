package com.datasense.finance.service;

import com.datasense.finance.domain.CollectResult;
import com.datasense.finance.domain.IssueResult;
import com.datasense.finance.domain.RuleGraph;
import com.datasense.finance.repository.ConsentLedger;
import com.datasense.finance.repository.EligibilityPolicy;
import com.datasense.finance.repository.IdempotencyStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 전자민원 발급 오케스트레이션 (issue_svc.py 포팅).
 *
 * 흐름: 동의 확인(AC-1) → 자격·예외 가드레일(AC-3) → 서류 산출 → 기관 수집(AC-2)
 *       → 멱등 발급(AC-4). 동의 철회 시 처리 중단·파기(AC-5).
 */
@Service
public class IssueOrchestrator {

    private final ConsentLedger consent;
    private final EligibilityPolicy eligibility;
    private final IdempotencyStore idem;
    private final GatewayAdapter gateway;

    /** userId -> 발급된 idempotencyKey 목록 (AC-5 파기 대상 추적). */
    private final Map<String, List<String>> issued = new HashMap<>();

    public IssueOrchestrator(ConsentLedger consent, EligibilityPolicy eligibility,
                             IdempotencyStore idem, GatewayAdapter gateway) {
        this.consent = consent;
        this.eligibility = eligibility;
        this.idem = idem;
        this.gateway = gateway;
    }

    /**
     * 발급 요청을 처리한다.
     *
     * @param failFirstAttempts 기관 응답 시뮬레이션: 각 서류마다 앞선 N회 시도를 미응답 처리(AC-2 데모)
     */
    public IssueResult issue(String userId, String minwonType, int failFirstAttempts) {
        // AC-1: 동의 게이트
        if (!consent.isActive(userId, "mydata")) {
            return IssueResult.rejected("consent_required", null);
        }

        // 미등록 민원 유형 차단
        List<String> citations;
        try {
            citations = RuleGraph.trace(minwonType).citations();
        } catch (RuleGraph.UnknownMinwonException e) {
            return IssueResult.rejected("unknown_minwon", null);
        }

        // AC-3: 가드레일: 예외 조건 미충족 시 거부 (근거는 인용)
        if (!eligibility.isEligible(userId, minwonType)) {
            return IssueResult.rejected("exception_unmet", citations);
        }

        List<String> docs = RuleGraph.requiredDocuments(minwonType);
        String key = IdempotencyStore.idempotencyKey(userId + "|" + minwonType);

        // 기관 응답 시뮬레이션: attempt <= failFirstAttempts 면 미응답(null)
        BiFunction<String, Integer, Object> responder =
                (docCode, attempt) -> attempt <= failFirstAttempts ? null : docCode + ":ok";

        // AC-4: 멱등 발급
        IdempotencyStore.Outcome outcome = idem.issueOnce(key, () -> {
            GatewayAdapter.Session session = gateway.newSession();
            boolean degraded = false;
            for (String d : docs) {
                CollectResult r = session.collect(d, responder, code -> code + ":fallback");
                degraded = degraded || r.degraded();
            }
            return IssueResult.issued(docs, citations, key, degraded);
        });

        if (!outcome.replayed()) {
            issued.computeIfAbsent(userId, k -> new ArrayList<>()).add(key);
        }
        IssueResult result = outcome.result();
        result.setReplay(outcome.replayed());
        return result;
    }

    public int issuedCount(String userId) {
        return issued.getOrDefault(userId, List.of()).size();
    }

    /** AC-5: 동의 철회 → 중단·파기·원장 기록. */
    public void handleWithdrawal(String userId, String scope) {
        consent.withdraw(userId, scope);  // 원장에 철회 기록(append)
        issued.remove(userId);            // 수집·발급 데이터 파기
    }
}
