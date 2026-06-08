package kr.elice.realfield.transaction;

import kr.elice.realfield.common.AptTransaction;
import kr.elice.realfield.transaction.port.AptTradeStore;
import kr.elice.realfield.transaction.service.TransactionCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** AC-4(멱등): 같은 배치를 두 번 적재해도 중복이 생기지 않습니다. DB 없이 인메모리 포트로 검증합니다. */
class IdempotentUpsertTest {

    /** 테스트용 인메모리 저장소(JPA 어댑터와 동일한 포트 계약). */
    static class InMemoryStore implements AptTradeStore {
        final Map<String, AptTransaction> data = new LinkedHashMap<>();
        public boolean existsByNaturalKey(String k) { return data.containsKey(k); }
        public void save(AptTransaction tx) { data.put(tx.naturalKey(), tx); }
        public List<AptTransaction> findByRegionMonth(String sggCd, int y, int m) {
            return data.values().stream()
                    .filter(t -> t.sggCd().equals(sggCd) && t.dealYear() == y && t.dealMonth() == m)
                    .toList();
        }
    }

    private AptTransaction sample() {
        return new AptTransaction("11110", "청운동", "경복궁아파트", 84.97, 10, 2003,
                2024, 5, 12, 825_000_000L, false);
    }

    @Test
    @DisplayName("AC-4: 동일 배치 재적재 시 두 번째는 0건만 삽입한다")
    void reingestionDoesNotDuplicate() {
        InMemoryStore store = new InMemoryStore();
        TransactionCommandService service = new TransactionCommandService(store);
        List<AptTransaction> batch = List.of(sample());

        int first = service.upsertAll(batch);
        int second = service.upsertAll(batch);

        assertEquals(1, first, "최초 적재는 1건");
        assertEquals(0, second, "재수집은 중복 0건");
        assertEquals(1, store.data.size(), "원장에는 1건만 남는다");
    }
}
