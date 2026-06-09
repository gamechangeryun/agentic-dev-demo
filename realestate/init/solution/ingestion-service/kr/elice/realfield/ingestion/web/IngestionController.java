package kr.elice.realfield.ingestion.web;

import kr.elice.realfield.ingestion.service.IngestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 수집 트리거 API입니다. 게이트웨이가 {@code /api/v1/ingest/**} 를 이 서비스로 라우팅합니다. */
@RestController
@RequestMapping("/api/v1/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /** 예) POST /api/v1/ingest/apt-trade?lawdCd=11110&dealYmd=202405 */
    @PostMapping("/apt-trade")
    public Map<String, Object> ingestAptTrade(@RequestParam String lawdCd,
                                              @RequestParam String dealYmd) {
        int upserted = ingestionService.ingest(lawdCd, dealYmd);
        return Map.of("lawdCd", lawdCd, "dealYmd", dealYmd, "upserted", upserted);
    }
}
