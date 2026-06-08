package kr.elice.shop.inventory.domain;

import java.util.List;
import java.util.Optional;

/** 예약 저장소 포트입니다. 상품별 예약 합계를 구하는 질의를 포함합니다. */
public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(String id);

    List<Reservation> findActiveByProduct(String productId);

    String nextId();
}
