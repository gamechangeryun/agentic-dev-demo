package kr.elice.shop.catalog.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.catalog.domain.ProductRepository;
import org.springframework.stereotype.Repository;

/**
 * 인메모리 상품 저장소 어댑터입니다. 데모는 외부 DB 없이 동작합니다.
 *
 * <p>ConcurrentHashMap 으로 동시 접근을 안전하게 처리하고, id 는 단조 증가
 * 시퀀스로 결정적으로 발급합니다. 운영에서는 이 어댑터를 JPA 구현으로 교체합니다.</p>
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final ConcurrentHashMap<String, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Product save(Product product) {
        store.put(product.id(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Product::id))
                .toList();
    }

    @Override
    public String nextId() {
        return String.format("prod_%04d", seq.incrementAndGet());
    }
}
