package kr.elice.shop.shared;

import org.springframework.http.HttpStatus;

/**
 * 도메인 오류 코드입니다. 각 코드는 HTTP 상태로 매핑되어 일관된 응답을 만듭니다.
 *
 * <p>요구사항 원문의 거부 사유 하나하나가 이 코드 하나와 대응됩니다.
 * 예를 들어 "총액 0원 이하 주문 거부"는 {@link #INVALID_AMOUNT} 로 표현됩니다.</p>
 */
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_PRICE(HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST),
    INVALID_QTY(HttpStatus.BAD_REQUEST),
    EMPTY_NAME(HttpStatus.BAD_REQUEST),
    EMPTY_CART(HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT),
    PRODUCT_ARCHIVED(HttpStatus.CONFLICT),
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT),
    PAYMENT_REQUIRED(HttpStatus.CONFLICT),
    ALREADY_PAID(HttpStatus.CONFLICT),
    PAYMENT_DECLINED(HttpStatus.PAYMENT_REQUIRED),
    REFUND_NOT_ALLOWED(HttpStatus.CONFLICT);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
