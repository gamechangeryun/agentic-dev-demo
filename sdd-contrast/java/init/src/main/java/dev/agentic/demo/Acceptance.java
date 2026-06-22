package dev.agentic.demo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 수용기준(Acceptance Criteria)을 코드로 옮긴 '채점기'다.
 *
 * <p>이 파일이 데모의 심장이다. 같은 기능을 명세 없이(바이브) 짜든 명세대로(SDD)
 * 짜든, '맞게 동작하는가'는 결국 누군가 정해 둔 기준으로 판정해야 한다. 그 기준을
 * 사람의 눈이 아니라 코드로 박아 둔 것이 {@link #CRITERIA}와 {@link #run}이다.</p>
 *
 * <p>설계 원칙 두 가지를 일부러 지킨다.</p>
 * <ol>
 *   <li>같은 기준을 양쪽에 똑같이 돌린다. 바이브 구현이든 SDD 구현이든 run()은
 *       동일한 AC-1~AC-4를 그대로 적용한다. 점수 차이는 오직 '구현이 그 기준을
 *       만족하느냐'에서만 나온다.</li>
 *   <li>한 기준이 터져도 나머지는 끝까지 채점한다. 각 기준을 try/catch로 감싸,
 *       예외가 나면 그 기준만 '불합격(false)'으로 처리하고 다음 기준으로 넘어간다.</li>
 * </ol>
 *
 * <p>시간은 실제 시계가 아니라 정수 t(초)로 주입한다. sleep 없이도 '만료'를
 * 결정적으로 재현하기 위해서다. 같은 입력이면 항상 같은 점수가 나온다(멱등).</p>
 */
public final class Acceptance {

    private Acceptance() {
    }

    /** 사람이 읽는 기준 이름과 한 줄 설명. 콘솔 출력에 그대로 쓴다. */
    public static final class Criterion {
        public final String name;
        public final String desc;

        Criterion(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    /** CRITERIA 순서와 1:1로 맞춘 판정 함수의 타입이다. */
    @FunctionalInterface
    private interface Check {
        boolean test(Supplier<Otp> make);
    }

    /** 사람이 읽는 기준 이름과 한 줄 설명. CRITERIA 순서가 곧 채점 순서다. */
    public static final List<Criterion> CRITERIA = List.of(
            new Criterion("AC-1 정상 발급·검증", "유효한 OTP로 가입하면 성공한다"),
            new Criterion("AC-2 만료 OTP 거부", "발급 후 TTL(300초)이 지난 OTP는 거부한다"),
            new Criterion("AC-3 5회 오류 잠금", "5회 연속 틀리면 정답을 넣어도 거부한다(무차별 대입 차단)"),
            new Criterion("AC-4 재요청 멱등", "같은 사람이 두 번 가입해도 계정은 1개만 생긴다")
    );

    // --- 기준별 판정 함수 ---------------------------------------------------
    // make: () -> Otp 구현 인스턴스. 각 기준마다 새 인스턴스로 시작해, 앞 기준이
    // 남긴 상태(잠금·생성 기록)가 다음 기준에 새지 않도록 격리한다.

    private static boolean ac1Happy(Supplier<Otp> make) {
        // 0초에 발급한 코드로 10초에 가입 → 정상 흐름이니 성공해야 한다.
        Otp o = make.get();
        o.issue("a@x.com", 0);
        return o.signup("a@x.com", "123456", 10);
    }

    private static boolean ac2Expiry(Supplier<Otp> make) {
        // 999초는 TTL 300초를 한참 넘긴 시점 → 만료로 거부되어야 정상이다.
        Otp o = make.get();
        o.issue("a@x.com", 0);
        return !o.signup("a@x.com", "123456", 999);
    }

    private static boolean ac3Lock(Supplier<Otp> make) {
        // 일부러 5회 오답을 낸 뒤, '정답'을 넣어도 거부되어야 한다(무차별 대입 차단).
        Otp o = make.get();
        o.issue("a@x.com", 0);
        for (int i = 0; i < 5; i++) {
            o.signup("a@x.com", "000000", 10);
        }
        return !o.signup("a@x.com", "123456", 10);
    }

    private static boolean ac4Idempotent(Supplier<Otp> make) {
        // 같은 사람이 두 번 가입 요청해도 계정은 1건이어야 한다.
        Otp o = make.get();
        o.issue("a@x.com", 0);
        o.signup("a@x.com", "123456", 10);
        o.signup("a@x.com", "123456", 10);
        return o.created().size() == 1;
    }

    /** CRITERIA 순서와 1:1로 맞춘 판정 함수 목록이다. */
    private static final List<Check> CHECKS = List.of(
            Acceptance::ac1Happy,
            Acceptance::ac2Expiry,
            Acceptance::ac3Lock,
            Acceptance::ac4Idempotent
    );

    /**
     * make로 만든 구현을 AC-1~AC-4로 채점해 {기준이름: true/false}를 돌려준다.
     *
     * <p>각 판정은 try/catch로 감싸므로, 구현에 메서드가 빠지거나 예외가 나도 그
     * 기준만 false가 되고 채점은 끝까지 진행된다(라이브 데모 안전장치).</p>
     */
    public static Map<String, Boolean> run(Supplier<Otp> make) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (int i = 0; i < CRITERIA.size(); i++) {
            Criterion c = CRITERIA.get(i);
            Check check = CHECKS.get(i);
            boolean ok;
            try {
                ok = check.test(make);
            } catch (Exception e) {
                // 메서드 미구현·예외 발생 등 → '그 기준을 만족하지 못함'으로 본다.
                ok = false;
            }
            result.put(c.name, ok);
        }
        return result;
    }

    /** 결과 맵에서 PASS 개수를 센다. */
    public static int score(Map<String, Boolean> result) {
        return (int) result.values().stream().filter(Boolean::booleanValue).count();
    }
}
