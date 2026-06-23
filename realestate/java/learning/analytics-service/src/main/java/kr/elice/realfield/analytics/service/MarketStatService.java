package kr.elice.realfield.analytics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.elice.realfield.analytics.client.TransactionQueryClient;
import kr.elice.realfield.analytics.domain.MarketStat;
import kr.elice.realfield.analytics.domain.MarketStatCalculator;
import kr.elice.realfield.common.AptTransaction;

/**
 * 시세 통계 서비스(read model 조립). 거래원장을 조회해 순수 계산기에 넘겨 집계한다.
 * write model을 변경하지 않는다(CQRS read 분리).
 */
@Service
public class MarketStatService {

    private final TransactionQueryClient queryClient;
    private final MarketStatCalculator calculator = new MarketStatCalculator();

    public MarketStatService(TransactionQueryClient queryClient) {
        this.queryClient = queryClient;
    }

    /** 시군구·계약 연월의 시세 통계를 산출한다(해제 제외 중위값). */
    public MarketStat marketStat(String sggCd, int dealYear, int dealMonth) {
        List<AptTransaction> transactions = queryClient.findByRegionMonth(sggCd, dealYear, dealMonth);
        return calculator.calculate(sggCd, dealYear, dealMonth, transactions);
    }
}
