package kr.elice.shop.cart.application;

import kr.elice.shop.catalog.application.CatalogService;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.cart.domain.Cart;
import kr.elice.shop.cart.domain.CartRepository;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 장바구니 유스케이스입니다.
 *
 * <p>상품을 담을 때 해당 상품이 존재하고 ACTIVE 인지 검증합니다. ARCHIVED 상품은
 * 담을 수 없습니다. 수량 규칙 자체는 {@link Cart} 애그리거트가 지킵니다.</p>
 */
@Service
public class CartService {

    private final CartRepository carts;
    private final CatalogService catalog;

    public CartService(CartRepository carts, CatalogService catalog) {
        this.carts = carts;
        this.catalog = catalog;
    }

    public Cart create() {
        return carts.save(new Cart(carts.nextId()));
    }

    public Cart get(String cartId) {
        return carts.findById(cartId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND,
                        "장바구니를 찾을 수 없습니다: " + cartId));
    }

    public Cart addItem(String cartId, String productId, int qty) {
        Cart cart = get(cartId);
        Product product = catalog.get(productId);
        if (!product.isActive()) {
            throw new DomainException(ErrorCode.PRODUCT_ARCHIVED,
                    "ARCHIVED 상품은 장바구니에 담을 수 없습니다: " + productId);
        }
        cart.addItem(productId, qty);
        return carts.save(cart);
    }

    public Cart updateQty(String cartId, String productId, int qty) {
        Cart cart = get(cartId);
        cart.updateQty(productId, qty);
        return carts.save(cart);
    }

    public Cart removeItem(String cartId, String productId) {
        Cart cart = get(cartId);
        cart.removeItem(productId);
        return carts.save(cart);
    }

    public Cart clear(String cartId) {
        Cart cart = get(cartId);
        cart.clear();
        return carts.save(cart);
    }
}
