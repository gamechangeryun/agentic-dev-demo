package kr.elice.shop.payment.domain;

/**
 * 결제 상태입니다. PENDING 으로 시작해 승인되면 CAPTURED, 거절되면 DECLINED 입니다.
 * CAPTURED 결제만 REFUNDED 로 환불할 수 있습니다.
 */
public enum PaymentStatus {
    PENDING,
    CAPTURED,
    DECLINED,
    REFUNDED
}
