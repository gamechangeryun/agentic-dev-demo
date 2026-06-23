package kr.elice.realfield.transaction.adapter;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.transaction.port.AptTradeStore;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link AptTradeStore} 포트의 JPA 어댑터(H2).
 *
 * <p>{@code natural_key} 유니크 제약 + {@code existsByNaturalKey}로 멱등을 강제한다(AC-4).
 * 도메인 ↔ 엔티티 변환을 흡수하고, 커맨드 서비스는 이 어댑터의 영속화 기술을 알지 못한다.
 */
@Repository
public class JpaAptTradeStore implements AptTradeStore {

    private final AptTradeJpaRepository repository;

    public JpaAptTradeStore(AptTradeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByNaturalKey(String naturalKey) {
        return repository.existsByNaturalKey(naturalKey);
    }

    @Override
    public void save(AptTransaction tx) {
        repository.save(AptTradeEntity.from(tx));
    }

    @Override
    public List<AptTransaction> findByRegionMonth(String sggCd, int dealYear, int dealMonth) {
        return repository.findBySggCdAndDealYearAndDealMonth(sggCd, dealYear, dealMonth)
                .stream()
                .map(AptTradeEntity::toDomain)
                .toList();
    }
}
