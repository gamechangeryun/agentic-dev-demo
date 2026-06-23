package kr.elice.shop.shared;

/**
 * 금액 값 객체입니다. 원 단위 정수로만 표현하며 음수를 허용하지 않습니다.
 *
 * <p>값 객체이므로 불변이며, 연산은 항상 새 인스턴스를 반환합니다.</p>
 */
public record Money(long amount) {

    public Money {
        if (amount < 0) {
            throw new DomainException(ErrorCode.INVALID_PRICE, "금액은 음수일 수 없습니다: " + amount);
        }
    }

    public static Money won(long amount) {
        return new Money(amount);
    }

    public static final Money ZERO = new Money(0);

    public Money plus(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new DomainException(ErrorCode.INVALID_QTY, "수량은 음수일 수 없습니다: " + quantity);
        }
        return new Money(this.amount * quantity);
    }

    public boolean isPositive() {
        return amount > 0;
    }
}
