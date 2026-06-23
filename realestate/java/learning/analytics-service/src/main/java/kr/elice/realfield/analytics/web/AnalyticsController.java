package kr.elice.realfield.analytics.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.elice.realfield.analytics.domain.MarketStat;
import kr.elice.realfield.analytics.service.MarketStatService;

/**
 * 시세 통계 진입점 (AC-5, SFR-008). 게이트웨이 `/api/v1/market-stats/**`로 노출된다.
 * 조회는 `sggCd`·`dealYear`·`dealMonth` 파라미터명을 쓴다.
 */
@RestController
@RequestMapping("/api/v1/market-stats")
public class AnalyticsController {

    private final MarketStatService marketStatService;

    public AnalyticsController(MarketStatService marketStatService) {
        this.marketStatService = marketStatService;
    }

    /** `GET /api/v1/market-stats?sggCd=&dealYear=&dealMonth=` → 해제 제외 중위 시세. */
    @GetMapping
    public MarketStat marketStat(
            @RequestParam String sggCd,
            @RequestParam int dealYear,
            @RequestParam int dealMonth) {
        return marketStatService.marketStat(sggCd, dealYear, dealMonth);
    }
}
