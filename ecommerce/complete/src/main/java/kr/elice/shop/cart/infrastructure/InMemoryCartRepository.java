package kr.elice.shop.cart.infrastructure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kr.elice.shop.cart.domain.Cart;
import kr.elice.shop.cart.domain.CartRepository;
import org.springframework.stereotype.Repository;

/** 인메모리 장바구니 저장소 어댑터입니다. */
@Repository
public class InMemoryCartRepository implements CartRepository {

    private final ConcurrentHashMap<String, Cart> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Cart save(Cart cart) {
        store.put(cart.id(), cart);
        return cart;
    }

    @Override
    public Optional<Cart> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public String nextId() {
        return String.format("cart_%04d", seq.incrementAndGet());
    }
}
