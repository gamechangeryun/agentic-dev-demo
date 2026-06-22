package com.datasense.finance.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 근거 조회·자격 검색: 결정적 규칙 그래프 (민원 → 필요서류 → 근거규정 → 예외).
 *
 * GraphRAG가 아니라 결정적 규칙 그래프로 '근거 규정 여러 단계 조회'를 구현한다.
 * 강의 데모용 가상 규칙: 실재 기관·규정 해석이 아니다. (shared/rules.py 포팅)
 */
public final class RuleGraph {

    /** 등록되지 않은 민원 유형. */
    public static class UnknownMinwonException extends RuntimeException {
        public UnknownMinwonException(String minwonType) {
            super(minwonType);
        }
    }

    /** 인용 그래프의 한 간선. */
    public record CitationStep(String relation, String src, String dst) {
    }

    // 민원 유형 → 필요 서류
    private static final Map<String, List<String>> REQUIRED_DOCS = new LinkedHashMap<>();
    // 서류 → 근거 규정
    private static final Map<String, String> DOC_BASIS = new LinkedHashMap<>();
    // 근거 규정 → 예외 조건 (충족해야 발급 가능, null이면 예외 없음)
    private static final Map<String, String> BASIS_EXCEPTION = new LinkedHashMap<>();

    static {
        REQUIRED_DOCS.put("전입신고", List.of("주민등록표"));
        REQUIRED_DOCS.put("사업자등록", List.of("사업자등록증", "임대차계약서"));
        REQUIRED_DOCS.put("복지급여신청", List.of("주민등록표", "소득증명원"));

        DOC_BASIS.put("주민등록표", "전자정부법 §9");
        DOC_BASIS.put("사업자등록증", "부가가치세법 §8");
        DOC_BASIS.put("임대차계약서", "상가건물 임대차보호법 §3");
        DOC_BASIS.put("소득증명원", "국민기초생활보장법 §21");

        BASIS_EXCEPTION.put("전자정부법 §9", "세대주 동의");
        BASIS_EXCEPTION.put("부가가치세법 §8", null);
        BASIS_EXCEPTION.put("상가건물 임대차보호법 §3", null);
        BASIS_EXCEPTION.put("국민기초생활보장법 §21", "소득 기준 충족");
    }

    private RuleGraph() {
    }

    /** 민원 유형에 필요한 서류 목록. 미등록이면 예외. */
    public static List<String> requiredDocuments(String minwonType) {
        List<String> docs = REQUIRED_DOCS.get(minwonType);
        if (docs == null) {
            throw new UnknownMinwonException(minwonType);
        }
        return new ArrayList<>(docs);
    }

    /** 이 민원 발급에 필요한 예외 조건 목록 (가드레일 판정용). */
    public static List<String> exceptionsOf(String minwonType) {
        List<String> conds = new ArrayList<>();
        for (String doc : requiredDocuments(minwonType)) {
            String basis = DOC_BASIS.get(doc);
            String exc = basis != null ? BASIS_EXCEPTION.get(basis) : null;
            if (exc != null) {
                conds.add(exc);
            }
        }
        return conds;
    }

    /** 추적 결과: 관계 그래프(steps)와 인용 순서 노드(citations). */
    public record Trace(List<CitationStep> steps, List<String> citations) {
    }

    /**
     * 민원 → 필요서류 → 근거규정 → 예외 여러 단계 경로를 끝까지 따라간다.
     */
    public static Trace trace(String minwonType) {
        List<String> docs = requiredDocuments(minwonType);
        List<CitationStep> steps = new ArrayList<>();
        List<String> citations = new ArrayList<>();
        for (String doc : docs) {
            steps.add(new CitationStep("필요서류", minwonType, doc));
            citations.add(doc);
            String basis = DOC_BASIS.get(doc);
            if (basis != null) {
                steps.add(new CitationStep("근거규정", doc, basis));
                citations.add(basis);
                String exc = BASIS_EXCEPTION.get(basis);
                if (exc != null) {
                    steps.add(new CitationStep("예외", basis, exc));
                    citations.add(exc);
                }
            }
        }
        return new Trace(steps, citations);
    }
}
