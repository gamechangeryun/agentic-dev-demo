package kr.elice.shop.payment.infrastructure;

import kr.elice.shop.payment.domain.PaymentGateway;
import kr.elice.shop.shared.Money;
import org.springframework.stereotype.Component;

/**
 * 데모용 결정적 결제 게이트웨이 어댑터입니다.
 *
 * <p>결제 수단이 "declined" 이면 거절하고, 그 밖에는 승인합니다. 실시간·난수에
 * 의존하지 않으므로 테스트에서 거절 경로를 정확히 재현할 수 있습니다. 운영에서는
 * 실제 PG 연동 어댑터로 교체합니다.</p>
 */
@Component
public class DemoPaymentGateway implements PaymentGateway {

    @Override
    public boolean authorize(Money amount, String method) {
        if (method != null && method.equalsIgnoreCase("declined")) {
            return false;
        }
        return amount.isPositive();
    }
}
