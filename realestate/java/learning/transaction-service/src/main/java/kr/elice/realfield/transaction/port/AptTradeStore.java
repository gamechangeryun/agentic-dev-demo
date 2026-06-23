package kr.elice.realfield.transaction.port;

import kr.elice.realfield.common.AptTransaction;

import java.util.List;

/**
 * 거래원장 저장 포트(헥사고날 outbound port).
 *
 * <p>커맨드 서비스는 이 계약에만 의존하고, 영속화 기술(JPA·H2)은 어댑터에서 교체한다.
 * 멱등(AC-4) 판정은 {@link AptTransaction#naturalKey()} 기준이며, 적재 측이 중복을 차단한다.
 */
public interface AptTradeStore {

    /** 자연키로 이미 적재된 거래인지 확인한다(멱등 판정). */
    boolean existsByNaturalKey(String naturalKey);

    /** 거래 한 건을 원장에 저장한다. */
    void save(AptTransaction tx);

    /** 시군구·계약 연월로 거래를 조회한다(집계·조회용). */
    List<AptTransaction> findByRegionMonth(String sggCd, int dealYear, int dealMonth);
}
