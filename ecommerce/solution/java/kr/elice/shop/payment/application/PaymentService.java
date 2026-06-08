package kr.elice.shop.payment.application;

import java.util.concurrent.ConcurrentHashMap;
import kr.elice.shop.inventory.application.InventoryService;
import kr.elice.shop.ordering.application.OrderService;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.domain.OrderStatus;
import kr.elice.shop.payment.domain.Payment;
import kr.elice.shop.payment.domain.PaymentGateway;
import kr.elice.shop.payment.domain.PaymentRepository;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 결제 유스케이스입니다. 주문 결제와 환불을 오케스트레이션합니다.
 *
 * <p>결제 승인이 떨어지면 주문을 PAID 로 전환하고, 주문에 묶인 재고 예약을
 * 확정해 물리 재고를 실제로 차감합니다. 같은 idempotency_key 재요청은 기존
 * 결제를 그대로 돌려주어 이중 청구를 막습니다. 결제가 거절되면 주문은
 * CREATED 로 남아 재시도하거나 취소할 수 있습니다.</p>
 */
@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final OrderService orders;
    private final InventoryService inventory;
    private final ConcurrentHashMap<String, String> idempotency = new ConcurrentHashMap<>();

    public PaymentService(PaymentRepository payments, PaymentGateway gateway,
            OrderService orders, InventoryService inventory) {
        this.payments = payments;
        this.gateway = gateway;
        this.orders = orders;
        this.inventory = inventory;
    }

    public Payment pay(String orderId, String method, String idemKey) {
        if (idemKey != null && idempotency.containsKey(idemKey)) {
            return payments.findById(idempotency.get(idemKey)).orElseThrow();
        }
        Order order = orders.get(orderId);
        if (order.status() == OrderStatus.PAID) {
            throw new DomainException(ErrorCode.ALREADY_PAID, "이미 결제된 주문입니다: " + orderId);
        }
        if (order.status() != OrderStatus.CREATED) {
            throw new DomainException(ErrorCode.INVALID_STATE_TRANSITION,
                    "CREATED 주문만 결제할 수 있습니다. 현재: " + order.status());
        }

        Payment payment = new Payment(payments.nextId(), orderId, order.totalAmount(), method);
        boolean approved = gateway.authorize(payment.amount(), method);
        payment.capture(approved);
        payments.save(payment);

        if (!approved) {
            throw new DomainException(ErrorCode.PAYMENT_DECLINED,
                    "결제가 거절되었습니다. 결제 id: " + payment.id());
        }

        orders.markPaid(orderId, payment.id());
        // 결제 확정과 동시에 예약 재고를 실제 차감합니다.
        order.reservationIds().forEach(inventory::confirm);
        if (idemKey != null) {
            idempotency.put(idemKey, payment.id());
        }
        return payment;
    }

    public Payment get(String paymentId) {
        return payments.findById(paymentId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND,
                        "결제를 찾을 수 없습니다: " + paymentId));
    }

    public Payment refund(String paymentId) {
        Payment payment = get(paymentId);
        payment.refund();
        return payments.save(payment);
    }
}
