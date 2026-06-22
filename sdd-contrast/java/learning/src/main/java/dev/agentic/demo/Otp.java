package dev.agentic.demo;

import java.util.Collection;

/**
 * 채점기(Acceptance)가 호출하는 OTP 구현의 계약이다.
 *
 * spec.md의 인터페이스를 자바로 옮긴 것이다. 시간은 실제 시계 대신 정수 t(초)로
 * 주입해, sleep 없이도 '만료'를 결정적으로 재현한다. 같은 입력이면 항상 같은
 * 결과가 나온다(멱등).
 *
 * <ul>
 *   <li>{@code issue(email, t)}  : OTP 코드를 발급한다.</li>
 *   <li>{@code verify(email, code, t)} : 코드가 맞고 유효하면 true를 돌려준다.</li>
 *   <li>{@code signup(email, code, t)} : 검증을 통과하면 가입시키고 true를 돌려준다.</li>
 *   <li>{@code created()} : 가입된 사용자 모음 (AC-4 멱등 채점에 쓴다).</li>
 * </ul>
 */
public interface Otp {

    /** OTP 코드를 발급한다. */
    String issue(String email, int t);

    /** 코드가 맞고 유효하면 true를 돌려준다. */
    boolean verify(String email, String code, int t);

    /** 검증을 통과하면 가입시키고 true를 돌려준다. */
    boolean signup(String email, String code, int t);

    /** 가입된 사용자 모음을 돌려준다(멱등 채점에 쓴다). */
    Collection<String> created();
}
