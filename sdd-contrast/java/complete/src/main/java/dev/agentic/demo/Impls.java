package dev.agentic.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 참고용 두 구현 — '명세 없이(vibe)'와 '명세 기반(SDD)'의 전형적인 결과물이다.
 *
 * <p>이 파일은 사실상 '정답지'다. 라이브 데모에서 학습자는 이 코드를 보지 않고,
 * Claude Code로 직접 Otp 구현을 만든다. 이 파일이 쓰이는 곳은 둘이다.</p>
 *
 * <ul>
 *   <li>{@link Contrast} : 양쪽을 한 번에 채점해 '기대 결과(vibe 1/4 vs SDD 4/4)'를 재현한다.</li>
 *   <li>학습자가 만든 구현과 비교해 볼 때의 레퍼런스로 쓴다.</li>
 * </ul>
 *
 * <p>두 구현의 차이는 코드 실력이 아니라 '무엇을 맞는 동작으로 정의했는가'다.</p>
 */
public final class Impls {

    private Impls() {
    }

    /**
     * 명세 없이 'happy path'만 짠 전형적 결과다 — 채점하면 보통 1/4가 나온다.
     *
     * <p>'회원가입 OTP 만들어줘'라는 한 마디만 듣고 짜면 대개 이렇게 나온다. 코드가
     * 일치하는지만 보고 바로 가입시킨다. 만료·잠금·멱등은 누가 말해 주지 않아 통째로
     * 빠진다. 그래서 AC-1(정상)만 통과하고 AC-2·3·4는 떨어진다. 못 짜서가 아니라
     * '무엇이 맞는지'를 아무도 정의하지 않았기 때문이다.</p>
     */
    public static final class VibeOtp implements Otp {

        private final Map<String, String> codes = new HashMap<>();
        // List라 같은 사람을 두 번 add → 멱등이 깨진다.
        private final List<String> created = new ArrayList<>();

        @Override
        public String issue(String email, int t) {
            codes.put(email, "123456");
            return "123456";
        }

        @Override
        public boolean verify(String email, String code, int t) {
            // 코드 일치만 본다 — 만료(t)도, 시도 횟수도 검사하지 않는다.
            String rec = codes.get(email);
            return rec != null && rec.equals(code);
        }

        @Override
        public boolean signup(String email, String code, int t) {
            if (!verify(email, code, t)) {
                return false;
            }
            created.add(email); // 중복 가입을 막는 장치가 없다.
            return true;
        }

        @Override
        public Collection<String> created() {
            return created;
        }
    }

    /**
     * EARS 수용기준대로 짠 결과다 — 채점하면 4/4가 나온다.
     *
     * <p>spec.md의 AC-2(만료)·AC-3(잠금)·AC-4(멱등)가 그대로 코드로 내려온 모습이다.
     * 바이브 코드와 실력 차이가 아니라, '무엇이 맞는지'를 명세가 먼저 정의해 줬다는
     * 차이뿐이다. 같은 채점기로 전부 통과한다.</p>
     */
    public static final class SddOtp implements Otp {

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
        // Set이라 같은 사람을 두 번 넣어도 1건이다 (AC-4 멱등).
        private final Set<String> created = new HashSet<>();

        @Override
        public String issue(String email, int t) {
            // 발급 시각(t)·실패 횟수·잠금 여부를 함께 들고 다닌다.
            codes.put(email, new Record("123456", t));
            return "123456";
        }

        @Override
        public boolean verify(String email, String code, int t) {
            Record r = codes.get(email);
            if (r == null || r.locked) { // 발급 안 됐거나 이미 잠긴 상태
                return false;
            }
            if (t - r.issuedAt > TTL) { // AC-2: 발급 후 TTL 초과 → 만료
                return false;
            }
            if (!r.code.equals(code)) { // 오답이면 실패 횟수를 센다
                r.fails += 1;
                if (r.fails >= MAX_ATTEMPTS) { // AC-3: 5회째 오답이면 잠금
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
            created.add(email); // AC-4: Set이라 자동으로 멱등
            return true;
        }

        @Override
        public Collection<String> created() {
            return created;
        }
    }
}
