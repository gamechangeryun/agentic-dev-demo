package kr.elice.library.springboot;

import kr.elice.library.api.LoanService;
import kr.elice.library.domain.LibraryException;
import kr.elice.library.domain.Loan;
import kr.elice.library.platform.CatalogRouter;
import kr.elice.library.store.LoanStore;
import org.springframework.stereotype.Service;

/**
 * 신규 대출 모듈입니다. 빈 이름은 {@code newLoanService} 가 되어 라우터가 자동 선택합니다.
 *
 * <p>레거시와 동일한 업무 규칙을 지킵니다. AC-1 은 한 회원이 동시에 5권을 넘겨 빌릴 수
 * 없다는 것이고, AC-2 는 연체 중인 회원은 새로 빌릴 수 없다는 것입니다. 다만 도서·회원
 * 조회는 레거시처럼 직접 부르지 않고 {@link CatalogRouter} 로 활성 구현을 받아 호출해,
 * 전환 중간 상태에서도 끊기지 않습니다. 저장소는 공유 {@link LoanStore} 를 씁니다.</p>
 */
@Service
public class NewLoanService implements LoanService {

    private static final int LIMIT = 5; // AC-1: 대출 한도 5권

    private final CatalogRouter router;
    private final LoanStore store;

    public NewLoanService(CatalogRouter router, LoanStore store) {
        this.router = router;
        this.store = store;
    }

    @Override
    public Loan borrow(String memberId, String bookId, int daysUntilDue) {
        // AC-L1: 라우터로 받은 활성 구현으로 회원·도서 존재 검증 (없으면 NOT_FOUND)
        router.members().get(memberId);
        router.books().get(bookId);
        if (activeCount(memberId) >= LIMIT) {                       // AC-1
            throw new LibraryException(LibraryException.Code.LOAN_LIMIT_EXCEEDED,
                    "대출 한도 " + LIMIT + "권을 초과했습니다.");
        }
        if (hasOverdue(memberId)) {                                 // AC-2
            throw new LibraryException(LibraryException.Code.OVERDUE_EXISTS,
                    "연체 중인 대출이 있어 새로 빌릴 수 없습니다.");
        }
        return store.save(new Loan(store.nextId(), memberId, bookId, daysUntilDue));
    }

    @Override
    public void giveBack(String loanId) {
        // AC-L2: 해당 대출을 반납 처리해 활성에서 제외
        Loan loan = store.find(loanId).orElseThrow(() ->
                new LibraryException(LibraryException.Code.NOT_FOUND, "대출을 찾을 수 없습니다: " + loanId));
        loan.markReturned();
    }

    @Override
    public int activeCount(String memberId) {
        return store.activeByMember(memberId).size();
    }

    @Override
    public boolean hasOverdue(String memberId) {
        return store.activeByMember(memberId).stream().anyMatch(Loan::isOverdue);
    }
}
