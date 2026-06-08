package kr.elice.shop.ordering.web;

import java.util.List;
import kr.elice.shop.ordering.domain.Order;

/** 주문 응답 DTO 입니다. */
public record OrderResponse(String id, String status, long totalAmount,
        List<LineView> lines, String paymentId) {

    public record LineView(String productId, String name, long unitPrice, int qty, long lineTotal) {}

    public static OrderResponse from(Order o) {
        List<LineView> lines = o.lines().stream()
                .map(l -> new LineView(l.productId(), l.name(), l.unitPrice().amount(),
                        l.qty(), l.lineTotal().amount()))
                .toList();
        return new OrderResponse(o.id(), o.status().name(), o.totalAmount().amount(),
                lines, o.paymentId());
    }
}
