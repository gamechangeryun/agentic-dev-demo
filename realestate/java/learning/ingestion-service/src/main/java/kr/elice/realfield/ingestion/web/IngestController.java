package kr.elice.realfield.ingestion.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.elice.realfield.ingestion.service.IngestionService;

/**
 * 수집 트리거 진입점 (AC-1, SFR-010). 게이트웨이 `/api/v1/ingest/**`로 노출된다.
 * 수집은 `lawdCd`(5자리)·`dealYmd`(YYYYMM) 파라미터명을 쓴다.
 */
@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final IngestionService ingestionService;

    public IngestController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /** `POST /api/v1/ingest/apt-trade?lawdCd=...&dealYmd=...` → 신규 적재 건수(재수집 시 멱등 0). */
    @PostMapping("/apt-trade")
    public IngestResult ingest(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd) {
        int upserted = ingestionService.ingest(lawdCd, dealYmd);
        return new IngestResult(lawdCd, dealYmd, upserted);
    }

    /** 수집 응답(프론트 `lib/types.ts`의 IngestResult와 1:1). */
    public record IngestResult(String lawdCd, String dealYmd, int upserted) {
    }
}
