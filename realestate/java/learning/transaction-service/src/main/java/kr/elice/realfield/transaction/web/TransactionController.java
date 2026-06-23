package kr.elice.realfield.transaction.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.transaction.port.AptTradeStore;
import kr.elice.realfield.transaction.service.TransactionCommandService;

/**
 * 거래원장 진입점. 게이트웨이 `/api/v1/transactions/**`로 노출된다.
 *
 * <ul>
 *   <li>POST: ingestion이 정규화한 거래 배치를 멱등 적재한다(AC-4). 신규 건수를 반환한다.</li>
 *   <li>GET: 시군구·계약 연월로 거래를 조회한다(프론트·analytics 공용, AC-1).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionCommandService commandService;
    private final AptTradeStore store;

    public TransactionController(
            TransactionCommandService commandService, AptTradeStore store) {
        this.commandService = commandService;
        this.store = store;
    }

    /** `POST /api/v1/transactions` (배치 멱등 적재) → 신규 건수. ingestion이 호출한다. */
    @PostMapping
    public UpsertResult upsert(@RequestBody List<AptTransaction> batch) {
        return new UpsertResult(commandService.upsertAll(batch));
    }

    /** `GET /api/v1/transactions?sggCd=&dealYear=&dealMonth=` → 거래 목록(해제 포함). */
    @GetMapping
    public List<AptTransaction> byRegionMonth(
            @RequestParam String sggCd,
            @RequestParam int dealYear,
            @RequestParam int dealMonth) {
        return store.findByRegionMonth(sggCd, dealYear, dealMonth);
    }

    /** 적재 응답(신규 건수). */
    public record UpsertResult(int upserted) {
    }
}
