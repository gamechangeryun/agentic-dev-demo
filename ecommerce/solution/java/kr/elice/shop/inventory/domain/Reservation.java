package kr.elice.shop.inventory.domain;

import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;

/**
 * 재고 예약 애그리거트입니다. 한 상품에 대한 특정 수량의 점유를 표현합니다.
 *
 * <p>예약은 가용 재고를 미리 묶어 두는 장치입니다. 결제가 끝나면 confirm 으로
 * 물리 재고에 반영되고, 주문이 취소되면 release 로 점유가 풀립니다. 상태 전환은
 * 이 애그리거트가 직접 검증하여 잘못된 이중 확정·이중 해제를 막습니다.</p>
 */
public class Reservation {

    private final String id;
    private final String productId;
    private final int qty;
    private ReservationStatus status;

    public Reservation(String id, String productId, int qty) {
        if (qty <= 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "예약 수량은 1 이상이어야 합니다.");
        }
        this.id = id;
        this.productId = productId;
        this.qty = qty;
        this.status = ReservationStatus.RESERVED;
    }

    public void confirm() {
        if (status != ReservationStatus.RESERVED) {
            throw new DomainException(ErrorCode.INVALID_STATE_TRANSITION,
                    "RESERVED 상태에서만 확정할 수 있습니다. 현재: " + status);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    /** 점유를 해제합니다. 이미 해제된 예약을 다시 해제하면 멱등하게 통과합니다. */
    public boolean release() {
        if (status == ReservationStatus.RELEASED) {
            return false;
        }
        boolean wasConfirmed = status == ReservationStatus.CONFIRMED;
        this.status = ReservationStatus.RELEASED;
        return wasConfirmed;
    }

    public String id() {
        return id;
    }

    public String productId() {
        return productId;
    }

    public int qty() {
        return qty;
    }

    public ReservationStatus status() {
        return status;
    }
}
