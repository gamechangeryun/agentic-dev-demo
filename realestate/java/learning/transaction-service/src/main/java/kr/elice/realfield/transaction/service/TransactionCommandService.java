package kr.elice.realfield.transaction.service;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.transaction.port.AptTradeStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 거래원장 적재 커맨드(write model · AC-4 멱등).
 *
 * <p>배치를 적재하되 자연키({@link AptTransaction#naturalKey()})가 이미 있으면 건너뛴다(중복 0).
 * 같은 배치를 두 번 적재해도 원장은 한 번만 기록된다. 신규로 삽입한 건수만 반환한다.
 */
@Service
public class TransactionCommandService {

    private final AptTradeStore store;

    public TransactionCommandService(AptTradeStore store) {
        this.store = store;
    }

    /**
     * 배치 멱등 적재. 이미 존재하는(naturalKey) 거래는 skip, 신규만 save.
     *
     * @return 신규로 삽입한 건수(재수집 시 0)
     */
    public int upsertAll(List<AptTransaction> batch) {
        int inserted = 0;
        for (AptTransaction tx : batch) {
            if (store.existsByNaturalKey(tx.naturalKey())) {
                continue; // 멱등: 이미 적재된 거래는 건너뛴다
            }
            store.save(tx);
            inserted++;
        }
        return inserted;
    }
}
