package kr.elice.shop.ordering.domain;

import kr.elice.shop.shared.Money;

/**
 * 주문 줄 항목 값 객체입니다. 주문 시점의 상품 가격을 스냅샷으로 고정합니다.
 *
 * <p>주문 이후 상품 가격이 바뀌어도 주문 총액은 변하지 않아야 하므로, 가격을
 * 장바구니나 상품을 다시 참조하지 않고 이 값 객체에 박아 둡니다.</p>
 */
public record OrderLine(String productId, String name, Money unitPrice, int qty) {

    public Money lineTotal() {
        return unitPrice.times(qty);
    }
}
