package kr.elice.shop.ordering.web;

import java.util.List;
import java.util.Map;
import kr.elice.shop.ordering.application.OrderService;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.domain.OrderStatus;
import kr.elice.shop.shared.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 REST API 입니다. 목록(상태 필터·페이징)·단건 조회·이행을 제공합니다.
 *
 * <p>이행은 주문 상태만 바꾸므로 주문 서비스를 직접 호출합니다. 취소는 재고 해제와
 * 환불 보상이 필요해 체크아웃 오케스트레이션이 담당하므로, 취소 엔드포인트는
 * checkout 컨텍스트(CheckoutController)에 둡니다. 이렇게 ordering 이 checkout 을
 * 역참조하지 않아 컨텍스트 의존이 단방향으로 유지됩니다.</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> result = orders.search(status, page, size);
        List<OrderResponse> items = result.items().stream().map(OrderResponse::from).toList();
        return Map.of("items", items, "total", result.total(),
                "page", result.page(), "size", result.size(), "pages", result.pages());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable String id) {
        return OrderResponse.from(orders.get(id));
    }

    @PostMapping("/{id}/fulfill")
    public OrderResponse fulfill(@PathVariable String id) {
        return OrderResponse.from(orders.fulfill(id));
    }
}
