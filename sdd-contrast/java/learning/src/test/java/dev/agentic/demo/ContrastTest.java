package dev.agentic.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 같은 채점기(Acceptance)로 두 구현을 채점해, '명세 유무가 결과를 가른다'를
 * 결정적으로 증명한다. SDD 구현은 4/4를 통과하고, vibe 구현은 4/4 미만이다.
 */
class ContrastTest {

    private static final int TOTAL = Acceptance.CRITERIA.size();

    @Test
    @DisplayName("SDD 구현은 모든 수용기준(AC-1~AC-4)을 통과한다 (4/4)")
    void sddPassesAll() {
        Map<String, Boolean> result = Acceptance.run(Impls.SddOtp::new);
        for (Acceptance.Criterion c : Acceptance.CRITERIA) {
            assertTrue(result.get(c.name), c.name + " 는 통과해야 한다");
        }
        assertEquals(TOTAL, Acceptance.score(result), "SDD 구현은 4/4여야 한다");
    }

    @Test
    @DisplayName("vibe 구현은 일부 수용기준에서 떨어진다 (4/4 미만)")
    void vibeFailsSome() {
        Map<String, Boolean> result = Acceptance.run(Impls.VibeOtp::new);
        // happy path(AC-1)는 통과한다.
        assertTrue(result.get("AC-1 정상 발급·검증"), "AC-1 happy path는 통과해야 한다");
        // 만료·잠금·멱등은 명세하지 않아 빠진다.
        assertFalse(result.get("AC-2 만료 OTP 거부"), "vibe는 만료를 거부하지 못한다");
        assertFalse(result.get("AC-3 5회 오류 잠금"), "vibe는 잠금이 없다");
        assertFalse(result.get("AC-4 재요청 멱등"), "vibe는 중복 가입을 막지 못한다");
        assertTrue(Acceptance.score(result) < TOTAL, "vibe 구현은 4/4 미만이어야 한다");
    }

    @Test
    @DisplayName("대조: vibe < SDD = 4/4 (데모가 유효한 조건)")
    void contrastHolds() {
        int vibe = Acceptance.score(Acceptance.run(Impls.VibeOtp::new));
        int sdd = Acceptance.score(Acceptance.run(Impls.SddOtp::new));
        assertEquals(TOTAL, sdd);
        assertTrue(vibe < sdd);
    }
}
