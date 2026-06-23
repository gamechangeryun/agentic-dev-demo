package kr.elice.shop.inventory.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kr.elice.shop.inventory.domain.Reservation;
import kr.elice.shop.inventory.domain.ReservationRepository;
import kr.elice.shop.inventory.domain.ReservationStatus;
import org.springframework.stereotype.Repository;

/** 인메모리 예약 저장소 어댑터입니다. */
@Repository
public class InMemoryReservationRepository implements ReservationRepository {

    private final ConcurrentHashMap<String, Reservation> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Reservation save(Reservation reservation) {
        store.put(reservation.id(), reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Reservation> findActiveByProduct(String productId) {
        return store.values().stream()
                .filter(r -> r.productId().equals(productId))
                .filter(r -> r.status() == ReservationStatus.RESERVED)
                .toList();
    }

    @Override
    public String nextId() {
        return String.format("resv_%04d", seq.incrementAndGet());
    }
}
