package com.datasense.finance.controller;

import com.datasense.finance.domain.IssueResult;
import com.datasense.finance.dto.ConsentRequest;
import com.datasense.finance.dto.IssueRequest;
import com.datasense.finance.dto.SatisfyRequest;
import com.datasense.finance.repository.ConsentLedger;
import com.datasense.finance.repository.EligibilityPolicy;
import com.datasense.finance.service.AdvisoryService;
import com.datasense.finance.service.IssueOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 전자민원 발급 데모 API. 흐름: 요청 → 검증(동의·자격) → 처리(수집·멱등) → 응답.
 * 강의 데모용 가상 금융·공공 서비스(MyLink). 실재 기관·시스템·개인정보 없음.
 */
@RestController
@RequestMapping("/api")
public class FinanceController {

    private final ConsentLedger consent;
    private final EligibilityPolicy eligibility;
    private final IssueOrchestrator orchestrator;
    private final AdvisoryService advisory;

    public FinanceController(ConsentLedger consent, EligibilityPolicy eligibility,
                             IssueOrchestrator orchestrator, AdvisoryService advisory) {
        this.consent = consent;
        this.eligibility = eligibility;
        this.orchestrator = orchestrator;
        this.advisory = advisory;
    }

    /** AC-1: 마이데이터 동의 부여. */
    @PostMapping("/consent/grant")
    public Map<String, Object> grant(@Valid @RequestBody ConsentRequest req) {
        consent.grant(req.userId(), req.scopeOrDefault());
        return Map.of("userId", req.userId(), "scope", req.scopeOrDefault(),
                "active", consent.isActive(req.userId(), req.scopeOrDefault()));
    }

    /** AC-5: 동의 철회 → 처리 중단·파기·원장 기록. */
    @PostMapping("/consent/withdraw")
    public Map<String, Object> withdraw(@Valid @RequestBody ConsentRequest req) {
        orchestrator.handleWithdrawal(req.userId(), req.scopeOrDefault());
        return Map.of("userId", req.userId(), "scope", req.scopeOrDefault(),
                "active", consent.isActive(req.userId(), req.scopeOrDefault()),
                "issuedCount", orchestrator.issuedCount(req.userId()));
    }

    /** AC-3: 예외 조건(자격) 충족 등록. 예: condition="세대주 동의". */
    @PostMapping("/eligibility/satisfy")
    public Map<String, Object> satisfy(@Valid @RequestBody SatisfyRequest req) {
        eligibility.satisfy(req.userId(), req.condition());
        return Map.of("userId", req.userId(), "condition", req.condition(), "satisfied", true);
    }

    /** AC-1·2·3·4: 전자민원 발급 (동의→자격→수집→멱등). */
    @PostMapping("/issue")
    public ResponseEntity<Map<String, Object>> issue(@Valid @RequestBody IssueRequest req) {
        IssueResult r = orchestrator.issue(req.userId(), req.minwonType(), req.failFirstAttempts());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", r.getStatus());
        out.put("reason", r.getReason());
        out.put("documents", r.getDocuments());
        out.put("citations", r.getCitations());
        out.put("idempotencyKey", r.getIdempotencyKey());
        out.put("replay", r.isReplay());
        out.put("degraded", r.isDegraded());
        return ResponseEntity.status(statusFor(r)).body(out);
    }

    /** 거부 사유를 적절한 HTTP 상태로 변환한다. 발급 성공·재발급은 2xx. */
    private static HttpStatus statusFor(IssueResult r) {
        if (!"rejected".equals(r.getStatus())) {
            return HttpStatus.OK;
        }
        return switch (r.getReason()) {
            case "consent_required" -> HttpStatus.FORBIDDEN;     // AC-1: 동의 게이트
            case "exception_unmet" -> HttpStatus.UNPROCESSABLE_ENTITY; // AC-3: 자격 가드레일
            case "unknown_minwon" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    /** AC-3: 근거 규정 다단계 인용 조회 (자격 미달이면 거부). */
    @GetMapping("/advisory/{minwonType}")
    public Map<String, Object> advisory(@PathVariable String minwonType,
                                        @RequestParam(defaultValue = "true") boolean eligible) {
        return advisory.answer(minwonType, eligible);
    }
}
