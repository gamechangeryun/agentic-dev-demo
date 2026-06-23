package kr.elice.realfield.transaction.adapter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 거래원장 Spring Data JPA 리포지토리.
 * 멱등 판정용 {@code existsByNaturalKey}와 시군구·연월 조회 파생 쿼리를 제공한다.
 */
interface AptTradeJpaRepository extends JpaRepository<AptTradeEntity, Long> {

    boolean existsByNaturalKey(String naturalKey);

    List<AptTradeEntity> findBySggCdAndDealYearAndDealMonth(String sggCd, int dealYear, int dealMonth);
}
