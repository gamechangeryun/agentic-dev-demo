package kr.elice.shop.ordering.application;

import java.util.List;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.domain.OrderLine;
import kr.elice.shop.ordering.domain.OrderRepository;
import kr.elice.shop.ordering.domain.OrderStatus;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Page;
import org.springframework.stereotype.Service;

/**
 * 주문 유스케이스입니다. 주문 애그리거트의 상태 전환과 영속화만 책임집니다.
 *
 * <p>재고 예약·결제 같은 부수효과는 checkout·payment 오케스트레이션이 담당하고,
 * 이 서비스는 그 결과로 도착하는 상태 전환 요청을 애그리거트에 위임합니다.
 * 목록 조회는 상태 필터와 페이징을 제공합니다.</p>
 */
@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public Order create(List<OrderLine> lines, List<String> reservationIds) {
        Order order = Order.create(orders.nextId(), lines, reservationIds);
        return orders.save(order);
    }

    public Order get(String id) {
        return orders.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다: " + id));
    }

    public Page<Order> search(OrderStatus status, int page, int size) {
        List<Order> rows = orders.findAll().stream()
                .filter(o -> status == null || o.status() == status)
                .toList();
        return Page.of(rows, page, size);
    }

    public Order markPaid(String orderId, String paymentId) {
        Order order = get(orderId);
        order.markPaid(paymentId);
        return orders.save(order);
    }

    public Order fulfill(String orderId) {
        Order order = get(orderId);
        order.fulfill();
        return orders.save(order);
    }

    /** 주문 상태만 취소로 전환하고, 취소 직전 결제 완료였는지 돌려줍니다. */
    public boolean cancel(String orderId) {
        Order order = get(orderId);
        boolean wasPaid = order.cancel();
        orders.save(order);
        return wasPaid;
    }
}
