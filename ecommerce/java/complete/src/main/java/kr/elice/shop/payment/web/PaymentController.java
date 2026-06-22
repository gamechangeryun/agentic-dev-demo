package kr.elice.shop.payment.web;

import kr.elice.shop.payment.application.PaymentService;
import kr.elice.shop.payment.domain.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결제 REST API 입니다. 주문 결제·조회·환불을 제공합니다.
 *
 * <p>결제 생성에는 {@code Idempotency-Key} 헤더로 이중 청구를 막습니다.
 * 환불은 보통 주문 취소 오케스트레이션이 호출하지만, 운영 점검을 위해
 * 단건 환불 엔드포인트도 노출합니다.</p>
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    public record PayRequest(String orderId, String method) {}

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @RequestBody PayRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        Payment p = payments.pay(req.orderId(), req.method(), idemKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(p));
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable String id) {
        return PaymentResponse.from(payments.get(id));
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable String id) {
        return PaymentResponse.from(payments.refund(id));
    }
}
