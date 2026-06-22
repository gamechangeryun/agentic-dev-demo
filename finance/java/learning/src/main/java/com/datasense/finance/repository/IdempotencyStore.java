package com.datasense.finance.repository;

import com.datasense.finance.domain.IssueResult;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 멱등 처리 (AC-4): idempotency_key로 중복 발급을 차단한다.
 * 같은 키는 한 번만 실행하고, 이후 호출은 저장된 결과를 재생(replay)한다.
 * (eminwon/idem.py 포팅, 인메모리)
 */
@Repository
public class IdempotencyStore {

    private final Map<String, IssueResult> seen = new HashMap<>();

    /** payload(예: "user|minwon")를 sha256 16진수로. */
    public static String idempotencyKey(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 결과와 replay 여부. */
    public record Outcome(IssueResult result, boolean replayed) {
    }

    public synchronized Outcome issueOnce(String key, Supplier<IssueResult> fn) {
        if (seen.containsKey(key)) {
            return new Outcome(seen.get(key), true); // replay (중복 아님)
        }
        IssueResult result = fn.get();
        seen.put(key, result);
        return new Outcome(result, false);
    }
}
