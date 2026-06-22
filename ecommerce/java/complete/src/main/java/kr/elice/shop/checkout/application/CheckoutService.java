package kr.elice.shop.checkout.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.elice.shop.cart.application.CartService;
import kr.elice.shop.cart.domain.Cart;
import kr.elice.shop.catalog.application.CatalogService;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.inventory.application.InventoryService;
import kr.elice.shop.inventory.domain.Reservation;
import kr.elice.shop.ordering.application.OrderService;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.domain.OrderLine;
import kr.elice.shop.payment.application.PaymentService;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 체크아웃 오케스트레이션입니다. 여러 bounded context 를 한 흐름으로 묶습니다.
 *
 * <p>체크아웃은 장바구니를 주문으로 바꾸는 과정입니다. 각 줄 항목의 재고를 먼저
 * 예약하고, 모두 성공하면 가격 스냅샷으로 주문을 생성합니다. 중간에 한 줄이라도
 * 재고가 부족하면, 이미 잡아 둔 예약을 모두 풀어 원자성을 흉내 내고 거부합니다.
 * 취소는 반대 방향 보상입니다. 예약을 풀고, 이미 결제된 주문이면 환불까지 수행합니다.</p>
 */
@Service
public class CheckoutService {

    private final CartService carts;
    private final CatalogService catalog;
    private final InventoryService inventory;
    private final OrderService orders;
    private final PaymentService payments;
    private final ConcurrentHashMap<String, String> idempotency = new ConcurrentHashMap<>();

    public CheckoutService(CartService carts, CatalogService catalog, InventoryService inventory,
            OrderService orders, PaymentService payments) {
        this.carts = carts;
        this.catalog = catalog;
        this.inventory = inventory;
        this.orders = orders;
        this.payments = payments;
    }

    public Order checkout(String cartId, String idemKey) {
        if (idemKey != null && idempotency.containsKey(idemKey)) {
            return orders.get(idempotency.get(idemKey));
        }
        Cart cart = carts.get(cartId);
        if (cart.isEmpty()) {
            throw new DomainException(ErrorCode.EMPTY_CART, "빈 장바구니는 주문할 수 없습니다.");
        }

        List<OrderLine> lines = new ArrayList<>();
        List<String> reservationIds = new ArrayList<>();
        try {
            for (Map.Entry<String, Integer> e : cart.lines().entrySet()) {
                Product product = catalog.get(e.getKey());
                int qty = e.getValue();
                Reservation reservation = inventory.reserve(product.id(), qty);
                reservationIds.add(reservation.id());
                lines.add(new OrderLine(product.id(), product.name(), product.price(), qty));
            }
        } catch (DomainException ex) {
            // 보상: 이미 잡은 예약을 모두 풀고 거부합니다(부분 예약을 남기지 않습니다).
            reservationIds.forEach(inventory::release);
            throw ex;
        }

        Order order = orders.create(lines, reservationIds);
        carts.clear(cartId);
        if (idemKey != null) {
            idempotency.put(idemKey, order.id());
        }
        return order;
    }

    public Order cancel(String orderId) {
        Order order = orders.get(orderId);
        List<String> reservationIds = order.reservationIds();
        String paymentId = order.paymentId();

        boolean wasPaid = orders.cancel(orderId);
        reservationIds.forEach(inventory::release);
        if (wasPaid && paymentId != null) {
            payments.refund(paymentId);
        }
        return orders.get(orderId);
    }
}
