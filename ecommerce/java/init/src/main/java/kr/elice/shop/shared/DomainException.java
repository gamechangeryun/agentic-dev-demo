package kr.elice.shop.shared;

/**
 * 도메인 규칙 위반을 표현하는 예외입니다.
 *
 * <p>도메인 계층은 이 예외만 던지고, 웹 계층의 {@code ApiExceptionHandler} 가
 * 오류 코드에 맞는 HTTP 상태와 본문으로 변환합니다. 도메인은 HTTP 를 알지 못합니다.</p>
 */
public class DomainException extends RuntimeException {

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
