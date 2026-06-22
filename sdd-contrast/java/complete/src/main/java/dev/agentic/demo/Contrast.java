package dev.agentic.demo;

import java.util.Map;

/**
 * 참고 정답(Impls)으로 양쪽을 한 번에 채점해 '기대 결과'를 재현하는 폴백 도구다.
 *
 * <p>라이브 데모의 본 흐름은 {@link Grade}다(학습자가 만든 구현을 채점한다). 이 파일은
 * 그것 없이도 vibe 1/4 vs SDD 4/4 대조를 결정적으로 보여 준다. 쓰임새는 둘이다.</p>
 * <ul>
 *   <li>멱등 검증/CI: 같은 입력이면 같은 결과가 나온다.</li>
 *   <li>강사 폴백: 라이브 코딩이 막혔을 때 기대 결과를 바로 보여 준다.</li>
 * </ul>
 *
 * <p>exit 0 조건: SDD 측 4/4 통과 + vibe 측은 4/4 미만(누락이 그대로 드러난다).</p>
 */
public final class Contrast {

    private Contrast() {
    }

    /** 바이브 구현이 떨어지는 기준에 붙일 '왜 떨어졌는가' 주석이다. */
    private static final Map<String, String> GAPS = Map.of(
            "AC-2 만료 OTP 거부", "만료된 코드도 통과 (만료를 명세 안 함)",
            "AC-3 5회 오류 잠금", "무한 시도 가능 (잠금을 명세 안 함)",
            "AC-4 재요청 멱등", "중복 가입 발생 (멱등을 명세 안 함)"
    );

    private static String line(String name, boolean ok, String note) {
        String mark = ok ? "PASS" : "FAIL";
        String tail = (note != null && !note.isEmpty() && !ok) ? "   ← " + note : "";
        return String.format("  %-20s %s%s", name, mark, tail);
    }

    public static void main(String[] args) {
        System.out.println("같은 요구사항: '회원가입 OTP를 만들어줘'\n");

        Map<String, Boolean> vibe = Acceptance.run(Impls.VibeOtp::new);
        Map<String, Boolean> sdd = Acceptance.run(Impls.SddOtp::new);

        int total = Acceptance.CRITERIA.size();

        System.out.println("[ 명세 없이 — vibe coding ]");
        for (Acceptance.Criterion c : Acceptance.CRITERIA) {
            System.out.println(line(c.name, vibe.get(c.name), GAPS.getOrDefault(c.name, "")));
        }
        int vpass = Acceptance.score(vibe);
        System.out.printf("  → %d/%d 통과. 게다가 검증 게이트(테스트)가 없어 이 빈틈이 그대로 배포됩니다.%n%n", vpass, total);

        System.out.println("[ 명세 기반 — SDD ]");
        for (Acceptance.Criterion c : Acceptance.CRITERIA) {
            System.out.println(line(c.name, sdd.get(c.name), ""));
        }
        int spass = Acceptance.score(sdd);
        System.out.printf("  → %d/%d 통과. EARS 수용기준이 만료·잠금·멱등을 '맞는 동작'으로 정의하고, proof 게이트가 강제합니다.%n%n", spass, total);

        System.out.println("교훈: '돌아간다'와 '맞다'는 다릅니다. 명세가 무엇이 맞는지 정의하고, 게이트가 그것을 검증합니다.");

        // SDD 측은 반드시 전부 통과해야 데모가 유효하다.
        boolean ok = spass == total && vpass < total;
        System.out.printf("%nRESULT: vibe %d/%d · SDD %d/%d  ·  %s%n",
                vpass, total, spass, total, ok ? "PASS" : "FAIL");
        System.exit(ok ? 0 : 1);
    }
}
