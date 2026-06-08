package kr.elice.shop.payment.infrastructure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kr.elice.shop.payment.domain.Payment;
import kr.elice.shop.payment.domain.PaymentRepository;
import org.springframework.stereotype.Repository;

/** 인메모리 결제 저장소 어댑터입니다. */
@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final ConcurrentHashMap<String, Payment> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Payment save(Payment payment) {
        store.put(payment.id(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public String nextId() {
        return String.format("pay_%04d", seq.incrementAndGet());
    }
}
