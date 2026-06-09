package kr.elice.shop.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.elice.shop.catalog.application.CatalogService;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.catalog.infrastructure.InMemoryProductRepository;
import kr.elice.shop.inventory.application.InventoryService;
import kr.elice.shop.inventory.domain.Reservation;
import kr.elice.shop.inventory.infrastructure.InMemoryReservationRepository;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 재고 예약 서비스 단위 테스트입니다. 인메모리 어댑터로 직접 조립합니다. */
class InventoryServiceTest {

    private CatalogService catalog;
    private InventoryService inventory;
    private String productId;

    @BeforeEach
    void setUp() {
        catalog = new CatalogService(new InMemoryProductRepository());
        inventory = new InventoryService(catalog, new InMemoryReservationRepository());
        Product p = catalog.create("노트북", 1_000_000, 5, null);
        productId = p.id();
    }

    @Test
    @DisplayName("예약은 가용 재고를 줄이지만 물리 재고는 그대로다")
    void reserveHoldsAvailability() {
        inventory.reserve(productId, 3);
        assertThat(inventory.available(productId)).isEqualTo(2);
        assertThat(catalog.get(productId).stockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("확정은 물리 재고를 실제로 차감한다")
    void confirmReducesPhysicalStock() {
        Reservation r = inventory.reserve(productId, 3);
        inventory.confirm(r.id());
        assertThat(catalog.get(productId).stockQuantity()).isEqualTo(2);
        assertThat(inventory.available(productId)).isEqualTo(2);
    }

    @Test
    @DisplayName("해제는 예약을 풀어 가용 재고를 되돌린다")
    void releaseRestoresAvailability() {
        Reservation r = inventory.reserve(productId, 3);
        inventory.release(r.id());
        assertThat(inventory.available(productId)).isEqualTo(5);
    }

    @Test
    @DisplayName("oversell 방지: 가용분을 초과하는 예약은 거부한다")
    void preventsOversell() {
        inventory.reserve(productId, 3);
        assertThatThrownBy(() -> inventory.reserve(productId, 3))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("동시 예약 100건도 물리 재고를 초과하지 않는다")
    void concurrentReservationsNeverOversell() throws InterruptedException {
        Product big = catalog.create("키보드", 50_000, 50, null);
        int threads = 100;
        Thread[] workers = new Thread[threads];
        java.util.concurrent.atomic.AtomicInteger success = new java.util.concurrent.atomic.AtomicInteger();
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    inventory.reserve(big.id(), 1);
                    success.incrementAndGet();
                } catch (DomainException ignored) {
                    // 가용분 소진 후 거부는 정상 경로입니다.
                }
            });
        }
        for (Thread w : workers) {
            w.start();
        }
        for (Thread w : workers) {
            w.join();
        }
        // 재고 50개에 100개 요청: 정확히 50건만 성공하고 가용분은 0이어야 합니다.
        assertThat(success.get()).isEqualTo(50);
        assertThat(inventory.available(big.id())).isEqualTo(0);
    }
}
