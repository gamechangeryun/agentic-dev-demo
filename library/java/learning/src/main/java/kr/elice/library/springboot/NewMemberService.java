package kr.elice.library.springboot;

import kr.elice.library.api.MemberService;
import kr.elice.library.domain.LibraryException;
import kr.elice.library.domain.Member;
import kr.elice.library.store.MemberStore;
import org.springframework.stereotype.Service;

/**
 * 신규 스프링부트 회원 모듈입니다. 레거시와 동일한 동작을 신규 패키지에 재구현합니다.
 *
 * <p>빈 이름이 {@code newMemberService} 가 되어 strangler 라우터가 자동 선택합니다.
 * 레거시·신규가 공유 {@link MemberStore} 를 사용하므로 같은 회원 데이터를 봅니다.</p>
 */
@Service
public class NewMemberService implements MemberService {

    private final MemberStore store;

    // 공유 저장소를 생성자 주입으로 받습니다.
    public NewMemberService(MemberStore store) {
        this.store = store;
    }

    /** AC-M1: 식별자와 이름을 가진 회원을 생성합니다. */
    @Override
    public Member register(String name) {
        return store.save(new Member(store.nextId(), name));
    }

    /** AC-M2: 없는 식별자로 조회하면 NOT_FOUND 로 거부합니다. */
    @Override
    public Member get(String id) {
        return store.find(id).orElseThrow(() ->
                new LibraryException(LibraryException.Code.NOT_FOUND, "회원을 찾을 수 없습니다: " + id));
    }
}
