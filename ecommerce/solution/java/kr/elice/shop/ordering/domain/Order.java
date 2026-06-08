package kr.elice.shop.ordering.domain;

import java.util.List;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Money;

/**
 * 주문 애그리거트입니다. 상태 전환 규칙의 단일 소유자입니다.
 *
 * <p>주문은 줄 항목 가격 스냅샷의 합을 총액으로 가지며, 총액이 0원 이하이면
 * 생성 자체가 거부됩니다. 결제·이행·취소는 모두 이 애그리거트의 상태 가드를
 * 통과해야 합니다. 외부 오케스트레이션은 재고 예약·결제 같은 부수효과를 맡고,
 * 상태 전환의 정합성은 여기서 보장됩니다.</p>
 */
public class Order {

    private final String id;
    private final List<OrderLine> lines;
    private final Money totalAmount;
    private final List<String> reservationIds;
    private OrderStatus status;
    private String paymentId;

    private Order(String id, List<OrderLine> lines, Money totalAmount, List<String> reservationIds) {
        this.id = id;
        this.lines = List.copyOf(lines);
        this.totalAmount = totalAmount;
        this.reservationIds = List.copyOf(reservationIds);
        this.status = OrderStatus.CREATED;
    }

    /** 주문을 생성합니다. 빈 주문이거나 총액이 0원 이하이면 거부합니다. */
    public static Order create(String id, List<OrderLine> lines, List<String> reservationIds) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException(ErrorCode.EMPTY_CART, "주문 항목이 비어 있습니다.");
        }
        Money total = lines.stream().map(OrderLine::lineTotal).reduce(Money.ZERO, Money::plus);
        if (!total.isPositive()) {
            throw new DomainException(ErrorCode.INVALID_AMOUNT, "주문 총액은 0원보다 커야 합니다.");
        }
        return new Order(id, lines, total, reservationIds);
    }

    public void markPaid(String paymentId) {
        if (status != OrderStatus.CREATED) {
            throw new DomainException(ErrorCode.INVALID_STATE_TRANSITION,
                    "CREATED 주문만 결제 완료로 전환할 수 있습니다. 현재: " + status);
        }
        this.status = OrderStatus.PAID;
        this.paymentId = paymentId;
    }

    public void fulfill() {
        if (status != OrderStatus.PAID) {
            throw new DomainException(ErrorCode.PAYMENT_REQUIRED,
                    "PAID 주문만 이행할 수 있습니다. 현재: " + status);
        }
        this.status = OrderStatus.FULFILLED;
    }

    /** 취소합니다. CREATED·PAID 에서만 허용되며, 취소 직전 결제 완료였는지 돌려줍니다. */
    public boolean cancel() {
        if (status == OrderStatus.FULFILLED) {
            throw new DomainException(ErrorCode.INVALID_STATE_TRANSITION,
                    "이행 완료된 주문은 취소할 수 없습니다.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_STATE_TRANSITION,
                    "이미 취소된 주문입니다.");
        }
        boolean wasPaid = status == OrderStatus.PAID;
        this.status = OrderStatus.CANCELLED;
        return wasPaid;
    }

    public String id() {
        return id;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public List<String> reservationIds() {
        return reservationIds;
    }

    public OrderStatus status() {
        return status;
    }

    public String paymentId() {
        return paymentId;
    }
}
