package kr.elice.shop.catalog.web;

import kr.elice.shop.catalog.domain.Product;

/** 상품 응답 DTO 입니다. 애그리거트의 외부 노출 형태를 고정합니다. */
public record ProductResponse(String id, String name, long price, int stockQuantity, String status) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.id(), p.name(), p.price().amount(),
                p.stockQuantity(), p.status().name());
    }
}
