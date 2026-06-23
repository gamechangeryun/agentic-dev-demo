package kr.elice.shop.cart.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;

/**
 * 장바구니 애그리거트입니다. 상품 id 별 수량을 담습니다.
 *
 * <p>같은 상품을 다시 담으면 수량이 합쳐지고, 수량을 0으로 바꾸면 항목이
 * 제거됩니다. 가격은 담는 시점이 아니라 checkout 시점에 스냅샷되므로,
 * 장바구니는 수량만 책임집니다. 빈 장바구니는 checkout 단계에서 거부됩니다.</p>
 */
public class Cart {

    private final String id;
    private final Map<String, Integer> lines = new LinkedHashMap<>();

    public Cart(String id) {
        this.id = id;
    }

    public void addItem(String productId, int qty) {
        if (qty <= 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "담는 수량은 1 이상이어야 합니다.");
        }
        lines.merge(productId, qty, Integer::sum);
    }

    public void updateQty(String productId, int qty) {
        requireLine(productId);
        if (qty < 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "수량은 음수일 수 없습니다.");
        }
        if (qty == 0) {
            lines.remove(productId);
        } else {
            lines.put(productId, qty);
        }
    }

    public void removeItem(String productId) {
        requireLine(productId);
        lines.remove(productId);
    }

    public void clear() {
        lines.clear();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    private void requireLine(String productId) {
        if (!lines.containsKey(productId)) {
            throw new DomainException(ErrorCode.NOT_FOUND, "장바구니에 없는 상품입니다: " + productId);
        }
    }

    public String id() {
        return id;
    }

    /** 읽기 전용 사본을 돌려줍니다. 외부에서 내부 맵을 직접 바꾸지 못합니다. */
    public Map<String, Integer> lines() {
        return Map.copyOf(lines);
    }
}
