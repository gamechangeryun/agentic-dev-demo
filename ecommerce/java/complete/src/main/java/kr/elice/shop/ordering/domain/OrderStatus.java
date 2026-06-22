package kr.elice.shop.ordering.domain;

/**
 * 주문 상태입니다.
 *
 * <p>정방향은 CREATED → PAID → FULFILLED 한 방향입니다. CREATED·PAID 에서만
 * 취소가 가능하고, 취소되면 CANCELLED 로 갑니다. FULFILLED 와 CANCELLED 는
 * terminal 이며 더 이상 어떤 전환도 허용되지 않습니다.</p>
 */
public enum OrderStatus {
    CREATED,
    PAID,
    FULFILLED,
    CANCELLED
}
