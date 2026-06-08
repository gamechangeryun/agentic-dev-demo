package kr.elice.shop.cart.web;

import kr.elice.shop.cart.application.CartService;
import kr.elice.shop.catalog.application.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장바구니 REST API 입니다. 생성·조회·담기·수량변경·삭제·비우기를 제공합니다.
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final CatalogService catalog;

    public CartController(CartService cartService, CatalogService catalog) {
        this.cartService = cartService;
        this.catalog = catalog;
    }

    public record AddItemRequest(String productId, int qty) {}

    public record QtyRequest(int qty) {}

    @PostMapping
    public ResponseEntity<CartView> create() {
        CartView view = CartView.from(cartService.create(), catalog);
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @GetMapping("/{cartId}")
    public CartView get(@PathVariable String cartId) {
        return CartView.from(cartService.get(cartId), catalog);
    }

    @PostMapping("/{cartId}/items")
    public CartView addItem(@PathVariable String cartId, @RequestBody AddItemRequest req) {
        return CartView.from(cartService.addItem(cartId, req.productId(), req.qty()), catalog);
    }

    @PatchMapping("/{cartId}/items/{productId}")
    public CartView updateQty(@PathVariable String cartId, @PathVariable String productId,
            @RequestBody QtyRequest req) {
        return CartView.from(cartService.updateQty(cartId, productId, req.qty()), catalog);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public CartView removeItem(@PathVariable String cartId, @PathVariable String productId) {
        return CartView.from(cartService.removeItem(cartId, productId), catalog);
    }

    @PostMapping("/{cartId}/clear")
    public CartView clear(@PathVariable String cartId) {
        return CartView.from(cartService.clear(cartId), catalog);
    }
}
