package com.datasense.auth;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 화면 정합 게이트(SC-1): 서빙되는 회원가입 화면이 캐노니컬 OTP 스냅샷과 일치하는지 단언한다.
 *
 * <p>강의 데모의 Playwright exactness gate를 이 자바 레포(브라우저 비가용)에서 결정적
 * HTML 스냅샷 정합으로 대체한다. python {@code run_ui_parity.py}는 python 변형 전용
 * ({@code server.contexts.auth} 의존)이라 여기선 비가용 — 본 테스트가 캐노니컬 게이트다.
 */
class SignupScreenParityTest {

    private static final Path SNAPSHOT =
            Path.of("sdd/04_verify/10_test/ui_parity/signup_otp.html");

    @Test
    void servedScreen_containsCanonicalOtpSnapshot() throws Exception {
        String snapshot = Files.readString(SNAPSHOT, StandardCharsets.UTF_8).strip();
        String page = new String(
                new ClassPathResource("static/signup.html").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);

        // 서빙 화면이 스냅샷 영역(OTP 입력 main)을 verbatim 으로 포함해야 한다.
        assertThat(page)
                .as("served signup.html must embed the canonical signup_otp snapshot verbatim")
                .contains(snapshot);
    }

    @Test
    void servedScreen_wiresOtpEndpoints() throws Exception {
        String page = new String(
                new ClassPathResource("static/signup.html").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);

        // SC-2·SC-3: 화면이 OTP 발급·가입 엔드포인트를 호출하도록 연결돼 있어야 한다.
        assertThat(page).contains("/auth/otp/issue");
        assertThat(page).contains("/auth/signup");
    }
}
