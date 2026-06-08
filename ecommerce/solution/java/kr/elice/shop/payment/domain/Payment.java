package kr.elice.shop.payment.domain;

import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Money;

/**
 * 결제 애그리거트입니다. 한 주문에 대한 한 번의 결제 시도를 표현합니다.
 *
 * <p>승인 여부는 외부 게이트웨이가 판정하고, 이 애그리거트는 그 결과를 상태로
 * 고정합니다. 환불은 캡처된 결제에 대해서만 허용됩니다. 이중 캡처·이중 환불은
 * 상태 가드로 막습니다.</p>
 */
public class Payment {

    private final String id;
    private final String orderId;
    private final Money amount;
    private final String method;
    private PaymentStatus status;

    public Payment(String id, String orderId, Money amount, String method) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method == null ? "card" : method;
        this.status = PaymentStatus.PENDING;
    }

    public void capture(boolean approved) {
        if (status != PaymentStatus.PENDING) {
            throw new DomainException(ErrorCode.INVALID_STATE_TRANSITION,
                    "PENDING 결제만 캡처할 수 있습니다. 현재: " + status);
        }
        this.status = approved ? PaymentStatus.CAPTURED : PaymentStatus.DECLINED;
    }

    public void refund() {
        if (status != PaymentStatus.CAPTURED) {
            throw new DomainException(ErrorCode.REFUND_NOT_ALLOWED,
                    "캡처된 결제만 환불할 수 있습니다. 현재: " + status);
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public boolean isCaptured() {
        return status == PaymentStatus.CAPTURED;
    }

    public String id() {
        return id;
    }

    public String orderId() {
        return orderId;
    }

    public Money amount() {
        return amount;
    }

    public String method() {
        return method;
    }

    public PaymentStatus status() {
        return status;
    }
}
