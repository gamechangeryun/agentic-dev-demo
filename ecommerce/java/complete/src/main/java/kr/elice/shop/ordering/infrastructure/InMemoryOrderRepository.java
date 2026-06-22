package kr.elice.shop.ordering.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.domain.OrderRepository;
import org.springframework.stereotype.Repository;

/** 인메모리 주문 저장소 어댑터입니다. */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<String, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Order save(Order order) {
        store.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Order::id))
                .toList();
    }

    @Override
    public String nextId() {
        return String.format("ord_%04d", seq.incrementAndGet());
    }
}
