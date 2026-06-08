package kr.elice.shop.inventory.application;

import kr.elice.shop.catalog.application.CatalogService;
import kr.elice.shop.catalog.domain.Product;
import kr.elice.shop.inventory.domain.Reservation;
import kr.elice.shop.inventory.domain.ReservationRepository;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * 재고 예약 유스케이스입니다. oversell 을 막는 핵심 게이트입니다.
 *
 * <p>가용 재고는 물리 재고에서 활성 예약 합계를 뺀 값입니다. reserve 는
 * 가용 재고를 확인하고 점유하는 두 동작이 원자적으로 일어나야 하므로
 * 동기화합니다. 이 덕분에 동시에 들어온 두 주문이 같은 재고를 중복 점유하지
 * 못하고, 가용분을 초과하는 뒤 주문은 INSUFFICIENT_STOCK 으로 거부됩니다.</p>
 */
@Service
public class InventoryService {

    private final CatalogService catalog;
    private final ReservationRepository reservations;

    public InventoryService(CatalogService catalog, ReservationRepository reservations) {
        this.catalog = catalog;
        this.reservations = reservations;
    }

    public int available(String productId) {
        Product product = catalog.get(productId);
        int reserved = reservations.findActiveByProduct(productId).stream()
                .mapToInt(Reservation::qty).sum();
        return product.stockQuantity() - reserved;
    }

    public synchronized Reservation reserve(String productId, int qty) {
        Product product = catalog.get(productId);
        if (!product.isActive()) {
            throw new DomainException(ErrorCode.PRODUCT_ARCHIVED,
                    "ARCHIVED 상품은 예약할 수 없습니다: " + productId);
        }
        if (qty <= 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "예약 수량은 1 이상이어야 합니다.");
        }
        int available = available(productId);
        if (qty > available) {
            throw new DomainException(ErrorCode.INSUFFICIENT_STOCK,
                    "가용 재고가 부족합니다. 가용 " + available + ", 요청 " + qty);
        }
        Reservation reservation = new Reservation(reservations.nextId(), productId, qty);
        return reservations.save(reservation);
    }

    public synchronized Reservation confirm(String reservationId) {
        Reservation reservation = find(reservationId);
        reservation.confirm();
        catalog.reduceStock(reservation.productId(), reservation.qty());
        return reservations.save(reservation);
    }

    public synchronized Reservation release(String reservationId) {
        Reservation reservation = find(reservationId);
        boolean wasConfirmed = reservation.release();
        if (wasConfirmed) {
            // 이미 물리 재고에서 빠진 예약을 되돌리므로 재고를 복원합니다.
            catalog.addStock(reservation.productId(), reservation.qty());
        }
        return reservations.save(reservation);
    }

    private Reservation find(String reservationId) {
        return reservations.findById(reservationId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND,
                        "예약을 찾을 수 없습니다: " + reservationId));
    }
}
