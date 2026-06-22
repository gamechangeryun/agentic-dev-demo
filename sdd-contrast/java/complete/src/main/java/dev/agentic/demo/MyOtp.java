package dev.agentic.demo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 학습자 구현 (SDD 라운드 · 정답) — spec.md의 AC-1~AC-4를 그대로 옮긴 결과입니다.
 *
 * 명세가 만료(AC-2)·잠금(AC-3)·멱등(AC-4)을 '맞는 동작'으로 정의해 줬기 때문에
 * 같은 채점기로 4/4를 통과합니다. ./gradlew grade 로 채점합니다.
 */
public class MyOtp implements Otp {

    private static final int TTL = 300;
    private static final int MAX_ATTEMPTS = 5;

    private static final class Record {
        final String code;
        final int issuedAt;
        int fails;
        boolean locked;

        Record(String code, int issuedAt) {
            this.code = code;
            this.issuedAt = issuedAt;
        }
    }

    private final Map<String, Record> codes = new HashMap<>();
    private final Set<String> created = new HashSet<>(); // Set이라 멱등(AC-4)

    @Override
    public String issue(String email, int t) {
        codes.put(email, new Record("123456", t)); // 발급 시각을 함께 보관
        return "123456";
    }

    @Override
    public boolean verify(String email, String code, int t) {
        Record r = codes.get(email);
        if (r == null || r.locked) {
            return false;
        }
        if (t - r.issuedAt > TTL) { // AC-2 만료
            return false;
        }
        if (!r.code.equals(code)) {
            r.fails += 1;
            if (r.fails >= MAX_ATTEMPTS) { // AC-3 5회 오류 잠금
                r.locked = true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean signup(String email, String code, int t) {
        if (!verify(email, code, t)) {
            return false;
        }
        created.add(email); // AC-4 멱등
        return true;
    }

    @Override
    public Collection<String> created() {
        return created;
    }
}
