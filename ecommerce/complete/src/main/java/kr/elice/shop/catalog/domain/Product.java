package kr.elice.shop.catalog.domain;

import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Money;

/**
 * 상품 애그리거트입니다. 가격·재고·상태의 불변식을 스스로 지킵니다.
 *
 * <p>가격은 0원 이하로 생성될 수 없고, ARCHIVED 상태에서는 어떤 재고 변경도
 * 허용되지 않습니다. 재고는 음수가 될 수 없습니다. 이 규칙들은 모두 이 애그리거트
 * 안에 모여 있어, 외부에서 상태를 직접 바꾸지 못합니다.</p>
 */
public class Product {

    private final String id;
    private String name;
    private Money price;
    private int stockQuantity;
    private ProductStatus status;

    private Product(String id, String name, Money price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = ProductStatus.ACTIVE;
    }

    /** 새 상품을 생성합니다. 이름이 비었거나 가격이 0원 이하이면 거부합니다. */
    public static Product create(String id, String name, Money price, int initialStock) {
        if (name == null || name.isBlank()) {
            throw new DomainException(ErrorCode.EMPTY_NAME, "상품명은 비어 있을 수 없습니다.");
        }
        if (!price.isPositive()) {
            throw new DomainException(ErrorCode.INVALID_PRICE, "가격은 0원보다 커야 합니다.");
        }
        if (initialStock < 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "초기 재고는 음수일 수 없습니다.");
        }
        return new Product(id, name.strip(), price, initialStock);
    }

    public void addStock(int qty) {
        ensureActive();
        if (qty <= 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "추가 수량은 1 이상이어야 합니다.");
        }
        this.stockQuantity += qty;
    }

    public void reduceStock(int qty) {
        ensureActive();
        if (qty <= 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "차감 수량은 1 이상이어야 합니다.");
        }
        if (qty > stockQuantity) {
            throw new DomainException(ErrorCode.INSUFFICIENT_STOCK,
                    "재고가 부족합니다. 보유 " + stockQuantity + ", 요청 " + qty);
        }
        this.stockQuantity -= qty;
    }

    public void archive() {
        this.status = ProductStatus.ARCHIVED;
    }

    private void ensureActive() {
        if (status == ProductStatus.ARCHIVED) {
            throw new DomainException(ErrorCode.PRODUCT_ARCHIVED,
                    "ARCHIVED 상품은 재고를 변경할 수 없습니다.");
        }
    }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public int stockQuantity() {
        return stockQuantity;
    }

    public ProductStatus status() {
        return status;
    }
}
