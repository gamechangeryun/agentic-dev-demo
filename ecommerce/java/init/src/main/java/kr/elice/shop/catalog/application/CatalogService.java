package kr.elice.shop.catalog.application;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.catalog.domain.ProductRepository;
import kr.elice.shop.catalog.domain.ProductStatus;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Money;
import kr.elice.shop.shared.Page;
import org.springframework.stereotype.Service;

/**
 * 상품 카탈로그 유스케이스입니다.
 *
 * <p>생성은 idempotency_key 로 멱등을 보장하고, 목록 조회는 이름 검색·상태 필터·
 * 페이징을 한 번에 처리합니다. 도메인 규칙은 모두 {@link Product} 애그리거트가
 * 지키고, 이 서비스는 흐름을 조율하고 저장소와 연결합니다.</p>
 */
@Service
public class CatalogService {

    private final ProductRepository products;
    private final ConcurrentHashMap<String, String> idempotency = new ConcurrentHashMap<>();

    public CatalogService(ProductRepository products) {
        this.products = products;
    }

    public Product create(String name, long price, int initialStock, String idemKey) {
        if (idemKey != null && idempotency.containsKey(idemKey)) {
            return products.findById(idempotency.get(idemKey)).orElseThrow();
        }
        Product product = Product.create(products.nextId(), name, Money.won(price), initialStock);
        products.save(product);
        if (idemKey != null) {
            idempotency.put(idemKey, product.id());
        }
        return product;
    }

    public Product get(String id) {
        return products.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다: " + id));
    }

    public Page<Product> search(String q, ProductStatus status, int page, int size) {
        List<Product> rows = products.findAll().stream()
                .filter(p -> q == null || q.isBlank()
                        || p.name().toLowerCase().contains(q.strip().toLowerCase()))
                .filter(p -> status == null || p.status() == status)
                .toList();
        return Page.of(rows, page, size);
    }

    public Product addStock(String id, int qty) {
        Product product = get(id);
        product.addStock(qty);
        return products.save(product);
    }

    public Product reduceStock(String id, int qty) {
        Product product = get(id);
        product.reduceStock(qty);
        return products.save(product);
    }

    public Product archive(String id) {
        Product product = get(id);
        product.archive();
        return products.save(product);
    }
}
