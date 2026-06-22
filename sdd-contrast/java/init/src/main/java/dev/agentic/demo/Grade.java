package dev.agentic.demo;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 학습자가 만든 Otp 구현을 같은 수용기준으로 채점한다 — 라이브 데모의 핵심 도구다.
 *
 * <p>이 파일은 '심판'이다. 라운드마다 학습자가 Claude Code로 Otp 구현(클래스)을 만든
 * 직후 이걸 돌리면, AC-1~AC-4를 채점해 점수를 보여 준다.</p>
 *
 * <pre>
 *   mvn -q -B exec 또는 java로 실행 시 클래스 이름을 인자로 준다.
 *   $ java dev.agentic.demo.Grade dev.agentic.demo.MyOtp
 *     점수: 1/4    # 명세 없이(바이브) 짠 경우 — 보통 happy path만 통과
 *     점수: 4/4    # spec.md대로 짠 경우 — 만료·잠금·멱등까지 통과
 * </pre>
 *
 * <p>채점 기준(Acceptance)은 두 라운드가 똑같다. 점수가 갈리는 건 오직 '명세가
 * 있었느냐'뿐이다. 인자가 없으면 참고 SDD 구현으로 채점해 4/4를 보여 준다.</p>
 */
public final class Grade {

    private Grade() {
    }

    public static void main(String[] args) {
        Supplier<Otp> make;
        String label;

        if (args.length >= 1) {
            // 학습자가 방금 만든 클래스를 리플렉션으로 가져온다.
            String className = args[0];
            try {
                Class<?> clazz = Class.forName(className);
                make = () -> instantiate(clazz);
                label = className;
            } catch (ClassNotFoundException e) {
                System.out.println(className + " 클래스를 찾을 수 없습니다.");
                System.out.println("먼저 Otp 인터페이스를 구현한 클래스를 만들고, 그 이름을 인자로 주세요.");
                System.exit(2);
                return;
            }
        } else {
            // 인자가 없으면 참고 SDD 구현으로 채점한다(기대 결과 4/4).
            make = Impls.SddOtp::new;
            label = "Impls.SddOtp (참고 구현)";
        }

        System.out.println("채점 대상: " + label + "\n");
        Map<String, Boolean> result = Acceptance.run(make);
        for (Acceptance.Criterion c : Acceptance.CRITERIA) {
            String mark = Boolean.TRUE.equals(result.get(c.name)) ? "PASS" : "FAIL";
            System.out.printf("  %-20s %s%n", c.name, mark);
        }

        int score = Acceptance.score(result);
        int total = Acceptance.CRITERIA.size();
        System.out.printf("%n점수: %d/%d%n", score, total);

        if (score < total) {
            StringBuilder missing = new StringBuilder();
            for (Acceptance.Criterion c : Acceptance.CRITERIA) {
                if (!Boolean.TRUE.equals(result.get(c.name))) {
                    if (missing.length() > 0) {
                        missing.append(", ");
                    }
                    missing.append(c.name);
                }
            }
            System.out.println("빠진 기준: " + missing);
            System.out.println("→ 명세에 없던 동작은 구현에서도 빠집니다. spec.md를 주고 다시 시켜 보세요.");
        } else {
            System.out.println("→ 명세가 정의한 '맞는 동작'을 모두 만족했습니다.");
        }

        System.exit(score == total ? 0 : 1);
    }

    private static Otp instantiate(Class<?> clazz) {
        try {
            Object o = clazz.getDeclaredConstructor().newInstance();
            return (Otp) o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
