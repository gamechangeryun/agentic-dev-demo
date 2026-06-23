package kr.elice.shop.checkout.web;

import kr.elice.shop.checkout.application.CheckoutService;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.web.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 체크아웃 오케스트레이션 REST API 입니다.
 *
 * <p>체크아웃(장바구니→주문)과 주문 취소(예약 해제 + 환불 보상)를 제공합니다.
 * 두 동작 모두 여러 컨텍스트를 가로지르므로 checkout 컨텍스트가 담당합니다.
 * 취소 경로는 사용자 관점에서 주문 자원에 속하므로 {@code /api/orders/{id}/cancel}
 * 로 노출하되, 구현은 오케스트레이션 쪽에 둡니다.</p>
 */
@RestController
public class CheckoutController {

    private final CheckoutService checkout;

    public CheckoutController(CheckoutService checkout) {
        this.checkout = checkout;
    }

    public record CheckoutRequest(String cartId) {}

    @PostMapping("/api/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @RequestBody CheckoutRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        Order order = checkout.checkout(req.cartId(), idemKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/api/orders/{id}/cancel")
    public OrderResponse cancel(@PathVariable String id) {
        return OrderResponse.from(checkout.cancel(id));
    }
}
