package kr.elice.shop.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * 상품 저장소 포트입니다. 도메인은 이 인터페이스에만 의존하고,
 * 구현(인메모리·JPA 등)은 infrastructure 계층이 갈아끼웁니다.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    /** id 오름차순으로 전체를 돌려줍니다. 검색·페이징은 애플리케이션 계층에서 적용합니다. */
    List<Product> findAll();

    String nextId();
}
