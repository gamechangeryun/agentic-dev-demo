package com.datasense.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * proof 해피패스 통합 테스트: OTP 발급 → 회원가입 → 로그인.
 * 실제 HTTP 호출로 각 단계 2xx를 단언하고, 잘못된 OTP 가입은 4xx를 단언한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void happyPath_issueOtp_signup_login_allReturn2xx() {
        String email = "happy-path@datasense.test";

        // 1) OTP 발급 → 2xx, 응답에서 code 확보
        ResponseEntity<Map> issue = rest.postForEntity(
                "/auth/otp/issue", Map.of("email", email), Map.class);
        assertThat(issue.getStatusCode().is2xxSuccessful()).isTrue();
        String code = (String) issue.getBody().get("code");
        assertThat(code).matches("\\d{6}");

        // 2) 발급된 OTP로 회원가입 → 2xx (201 CREATED)
        ResponseEntity<Map> signup = rest.postForEntity(
                "/auth/signup", Map.of("email", email, "code", code), Map.class);
        assertThat(signup.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(signup.getBody().get("status")).isEqualTo("created");

        // 3) 가입된 이메일로 로그인 → 2xx, status=ok
        ResponseEntity<Map> login = rest.postForEntity(
                "/auth/login", Map.of("email", email), Map.class);
        assertThat(login.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(login.getBody().get("status")).isEqualTo("ok");
    }

    @Test
    void signup_withWrongOtp_returns4xx() {
        String email = "wrong-otp@datasense.test";

        // OTP를 발급해 (email, purpose) 레코드는 존재하되, 다른 6자리로 가입 시도
        ResponseEntity<Map> issue = rest.postForEntity(
                "/auth/otp/issue", Map.of("email", email), Map.class);
        assertThat(issue.getStatusCode().is2xxSuccessful()).isTrue();
        String wrongCode = "000000".equals(issue.getBody().get("code")) ? "111111" : "000000";

        ResponseEntity<Map> signup = rest.postForEntity(
                "/auth/signup", Map.of("email", email, "code", wrongCode), Map.class);
        assertThat(signup.getStatusCode().is4xxClientError()).isTrue();
        assertThat(signup.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(signup.getBody().get("status")).isEqualTo("rejected");
    }
}
