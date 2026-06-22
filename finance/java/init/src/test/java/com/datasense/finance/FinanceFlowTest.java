package com.datasense.finance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proof 통합테스트: 전자민원 발급 핵심 흐름(동의 → 자격충족 → 발급)을 실제 HTTP로 검증한다.
 *
 * 강의 데모용 가상 금융·공공 서비스(MyLink). 실재 기관·시스템·개인정보 없음.
 * 흐름: POST /api/consent/grant → POST /api/eligibility/satisfy → POST /api/issue.
 * "전입신고"는 근거규정(전자정부법 §9)의 예외조건 "세대주 동의"를 충족해야 발급된다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FinanceFlowTest {

    @Autowired
    private TestRestTemplate http;

    /** 해피패스: 동의 → 자격충족 → 발급. 각 단계 2xx + 발급 성공을 단언한다. */
    @Test
    void happyPath_grantSatisfyIssue_allSucceed() {
        String userId = "u-happy-1";
        String minwonType = "전입신고";

        // 1) 마이데이터 동의 부여 (AC-1)
        ResponseEntity<Map> grant = http.postForEntity(
                "/api/consent/grant",
                Map.of("userId", userId, "scope", "mydata"),
                Map.class);
        assertThat(grant.getStatusCode().is2xxSuccessful())
                .as("동의 grant 는 2xx 여야 합니다").isTrue();
        assertThat(grant.getBody()).containsEntry("active", true);

        // 2) 예외 조건 충족 등록 (AC-3): 전입신고는 "세대주 동의" 필요
        ResponseEntity<Map> satisfy = http.postForEntity(
                "/api/eligibility/satisfy",
                Map.of("userId", userId, "condition", "세대주 동의"),
                Map.class);
        assertThat(satisfy.getStatusCode().is2xxSuccessful())
                .as("eligibility satisfy 는 2xx 여야 합니다").isTrue();
        assertThat(satisfy.getBody()).containsEntry("satisfied", true);

        // 3) 전자민원 발급 (AC-1·2·3·4)
        ResponseEntity<Map> issue = http.postForEntity(
                "/api/issue",
                Map.of("userId", userId, "minwonType", minwonType, "failFirstAttempts", 0),
                Map.class);
        assertThat(issue.getStatusCode().is2xxSuccessful())
                .as("issue 발급은 2xx 여야 합니다").isTrue();
        assertThat(issue.getBody()).containsEntry("status", "issued");
    }

    /** 정책 검증: 동의 없이 발급 시도하면 4xx 로 거부된다 (AC-1 동의 게이트). */
    @Test
    void issueWithoutConsent_isRejectedWith4xx() {
        String userId = "u-no-consent-1";

        ResponseEntity<Map> issue = http.postForEntity(
                "/api/issue",
                Map.of("userId", userId, "minwonType", "전입신고", "failFirstAttempts", 0),
                Map.class);

        assertThat(issue.getStatusCode().is4xxClientError())
                .as("동의 없는 발급은 4xx 여야 합니다").isTrue();
        assertThat(issue.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(issue.getBody())
                .containsEntry("status", "rejected")
                .containsEntry("reason", "consent_required");
    }
}
