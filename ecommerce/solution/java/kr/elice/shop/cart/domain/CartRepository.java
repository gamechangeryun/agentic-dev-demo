package kr.elice.shop.cart.domain;

import java.util.Optional;

/** 장바구니 저장소 포트입니다. */
public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(String id);

    String nextId();
}
