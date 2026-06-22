package kr.elice.shop.cart.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kr.elice.shop.cart.domain.Cart;
import kr.elice.shop.catalog.application.CatalogService;
import kr.elice.shop.catalog.domain.Product;

/**
 * 장바구니 응답 DTO 입니다. 담긴 수량에 현재 상품 가격을 곱해 줄별 합계와
 * 총액을 계산해 보여줍니다. 가격 스냅샷은 checkout 에서 확정되므로 여기서는
 * 표시용 계산입니다.
 */
public record CartView(String cartId, List<Line> items, int totalQty, long totalAmount) {

    public record Line(String productId, String name, long unitPrice, int qty, long lineTotal) {}

    public static CartView from(Cart cart, CatalogService catalog) {
        List<Line> lines = new ArrayList<>();
        int totalQty = 0;
        long totalAmount = 0;
        for (Map.Entry<String, Integer> e : cart.lines().entrySet()) {
            Product p = catalog.get(e.getKey());
            int qty = e.getValue();
            long lineTotal = p.price().amount() * qty;
            lines.add(new Line(p.id(), p.name(), p.price().amount(), qty, lineTotal));
            totalQty += qty;
            totalAmount += lineTotal;
        }
        return new CartView(cart.id(), lines, totalQty, totalAmount);
    }
}
