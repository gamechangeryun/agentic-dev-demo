package com.datasense.finance.service;

import com.datasense.finance.domain.RuleGraph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 상담 응답 (AC-3): 근거 규정을 여러 단계로 인용하고, 자격 미달이면 발급을 거부한다.
 * (contexts/advisory/citation.py 포팅)
 */
@Service
public class AdvisoryService {

    /**
     * 근거 경로(민원→서류→규정→예외)를 끝까지 인용한다.
     * eligible=false(자격 미달)면 답을 지어내지 않고 거부한다 (가드레일).
     */
    public Map<String, Object> answer(String minwonType, boolean eligible) {
        RuleGraph.Trace t = RuleGraph.trace(minwonType);
        Map<String, Object> out = new LinkedHashMap<>();
        if (!eligible) {
            out.put("status", "refused");
            out.put("reason", "자격 미달: 발급 거부");
            out.put("citations", t.citations());
            return out;
        }
        List<List<String>> steps = new ArrayList<>();
        for (RuleGraph.CitationStep s : t.steps()) {
            steps.add(List.of(s.relation(), s.src(), s.dst()));
        }
        out.put("status", "answered");
        out.put("citations", t.citations());
        out.put("steps", steps);
        out.put("exactness", t.citations().size() + "/" + t.citations().size());
        return out;
    }
}
