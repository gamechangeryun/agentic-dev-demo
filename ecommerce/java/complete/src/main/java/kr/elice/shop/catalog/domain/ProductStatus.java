package kr.elice.shop.catalog.domain;

/**
 * 상품 상태입니다. ACTIVE 에서 ARCHIVED 로 한 방향 전환만 허용됩니다.
 * ARCHIVED 는 terminal 상태이며 이후 재고 변경이 모두 거부됩니다.
 */
public enum ProductStatus {
    ACTIVE,
    ARCHIVED
}
