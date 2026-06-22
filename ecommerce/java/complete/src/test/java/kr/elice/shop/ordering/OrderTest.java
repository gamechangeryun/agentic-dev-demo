package kr.elice.shop.ordering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.elice.shop.ordering.domain.Order;
import kr.elice.shop.ordering.domain.OrderLine;
import kr.elice.shop.ordering.domain.OrderStatus;
import kr.elice.shop.shared.DomainException;
import kr.elice.shop.shared.ErrorCode;
import kr.elice.shop.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 주문 상태머신 단위 테스트입니다. */
class OrderTest {

    private Order newOrder() {
        OrderLine line = new OrderLine("p1", "노트북", Money.won(1000), 2);
        return Order.create("ord_1", List.of(line), List.of("resv_1"));
    }

    @Test
    @DisplayName("AC-2 총액 0원 이하 주문은 생성 단계에서 거부한다")
    void rejectsZeroAmount() {
        OrderLine free = new OrderLine("p1", "사은품", Money.won(0), 1);
        assertThatThrownBy(() -> Order.create("ord_x", List.of(free), List.of()))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.INVALID_AMOUNT);
    }

    @Test
    @DisplayName("AC-3 CREATED → PAID → FULFILLED 정방향으로 전환한다")
    void forwardTransitions() {
        Order o = newOrder();
        assertThat(o.status()).isEqualTo(OrderStatus.CREATED);
        o.markPaid("pay_1");
        assertThat(o.status()).isEqualTo(OrderStatus.PAID);
        o.fulfill();
        assertThat(o.status()).isEqualTo(OrderStatus.FULFILLED);
    }

    @Test
    @DisplayName("AC-4 결제 전 주문은 이행할 수 없다")
    void cannotFulfillBeforePaid() {
        Order o = newOrder();
        assertThatThrownBy(o::fulfill)
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.PAYMENT_REQUIRED);
    }

    @Test
    @DisplayName("AC-5 이행 완료된 주문은 취소할 수 없다")
    void cannotCancelFulfilled() {
        Order o = newOrder();
        o.markPaid("pay_1");
        o.fulfill();
        assertThatThrownBy(o::cancel)
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).code())
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    @DisplayName("결제된 주문 취소는 환불 필요 신호(true)를 돌려준다")
    void cancelPaidSignalsRefund() {
        Order o = newOrder();
        o.markPaid("pay_1");
        assertThat(o.cancel()).isTrue();
        assertThat(o.status()).isEqualTo(OrderStatus.CANCELLED);
    }
}
