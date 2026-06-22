package kr.elice.shop.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 상품 애그리거트 불변식 단위 테스트입니다. Spring 컨텍스트 없이 빠르게 검증합니다. */
class ProductTest {

    @Test
    @DisplayName("AC-1 가격이 0원 이하이면 상품 생성을 거부한다")
    void rejectsNonPositivePrice() {
        assertThatThrownBy(() -> Product.create("p1", "노트북", Money.won(0), 5))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.INVALID_PRICE);
    }

    @Test
    @DisplayName("AC-3 ACTIVE 상품은 재고를 더하고 뺄 수 있다")
    void addAndReduceStock() {
        Product p = Product.create("p1", "노트북", Money.won(1000), 5);
        p.addStock(3);
        assertThat(p.stockQuantity()).isEqualTo(8);
        p.reduceStock(2);
        assertThat(p.stockQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("AC-4 보유 재고를 넘는 차감은 INSUFFICIENT_STOCK 으로 거부한다")
    void reduceBeyondStockRejected() {
        Product p = Product.create("p1", "노트북", Money.won(1000), 5);
        assertThatThrownBy(() -> p.reduceStock(6))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("AC-6 ARCHIVED 상품은 재고 변경이 모두 거부된다")
    void archivedBlocksStockChanges() {
        Product p = Product.create("p1", "노트북", Money.won(1000), 5);
        p.archive();
        assertThatThrownBy(() -> p.addStock(1))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.PRODUCT_ARCHIVED);
    }
}
