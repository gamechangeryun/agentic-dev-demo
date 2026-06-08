package kr.elice.shop.payment.web;

import kr.elice.shop.payment.domain.Payment;

/** 결제 응답 DTO 입니다. */
public record PaymentResponse(String id, String orderId, long amount, String method, String status) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.id(), p.orderId(), p.amount().amount(), p.method(),
                p.status().name());
    }
}
