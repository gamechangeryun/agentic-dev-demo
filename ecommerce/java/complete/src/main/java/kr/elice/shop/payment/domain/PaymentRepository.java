package kr.elice.shop.payment.domain;

import java.util.Optional;

/** 결제 저장소 포트입니다. */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(String id);

    String nextId();
}
