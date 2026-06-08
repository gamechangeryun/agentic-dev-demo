package kr.elice.shop.payment.domain;

import kr.elice.shop.shared.Money;

/**
 * 결제 게이트웨이 포트입니다. 실제 PG 연동의 자리를 표현합니다.
 *
 * <p>도메인은 승인 여부만 알면 되므로 인터페이스는 boolean 하나로 단순합니다.
 * 데모 어댑터는 결정적으로 동작하고, 운영에서는 실제 PG 어댑터로 교체합니다.</p>
 */
public interface PaymentGateway {

    boolean authorize(Money amount, String method);
}
