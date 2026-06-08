package kr.elice.shop.inventory.domain;

/**
 * 재고 예약 상태입니다. RESERVED 로 시작해 결제 확정 시 CONFIRMED,
 * 주문 취소 시 RELEASED 로 전환됩니다. CONFIRMED·RELEASED 는 terminal 입니다.
 */
public enum ReservationStatus {
    RESERVED,
    CONFIRMED,
    RELEASED
}
