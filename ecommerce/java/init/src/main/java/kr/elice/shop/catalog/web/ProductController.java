package kr.elice.shop.catalog.web;

import java.util.List;
import java.util.Map;
import kr.elice.shop.catalog.application.CatalogService;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.catalog.domain.ProductStatus;
import kr.elice.shop.shared.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 카탈로그 REST API 입니다.
 *
 * <p>등록·단건 조회·검색/페이징 목록·재고 가감·아카이브를 제공합니다.
 * 멱등이 필요한 생성에는 {@code Idempotency-Key} 헤더를 사용합니다.</p>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalog;

    public ProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    public record CreateRequest(String name, long price, int initialStock) {}

    public record StockRequest(int qty) {}

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @RequestBody CreateRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        Product p = catalog.create(req.name(), req.price(), req.initialStock(), idemKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(p));
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable String id) {
        return ProductResponse.from(catalog.get(id));
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> result = catalog.search(q, status, page, size);
        List<ProductResponse> items = result.items().stream().map(ProductResponse::from).toList();
        return Map.of("items", items, "total", result.total(),
                "page", result.page(), "size", result.size(), "pages", result.pages());
    }

    @PostMapping("/{id}/stock-additions")
    public ProductResponse addStock(@PathVariable String id, @RequestBody StockRequest req) {
        return ProductResponse.from(catalog.addStock(id, req.qty()));
    }

    @PostMapping("/{id}/stock-reductions")
    public ProductResponse reduceStock(@PathVariable String id, @RequestBody StockRequest req) {
        return ProductResponse.from(catalog.reduceStock(id, req.qty()));
    }

    @PostMapping("/{id}/archive")
    public ProductResponse archive(@PathVariable String id) {
        return ProductResponse.from(catalog.archive(id));
    }
}
