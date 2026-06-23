package kr.elice.library.springboot;

import java.util.List;
import kr.elice.library.api.BookService;
import kr.elice.library.domain.Book;
import kr.elice.library.domain.LibraryException;
import kr.elice.library.store.BookStore;
import org.springframework.stereotype.Service;

/**
 * 신규 도서 모듈입니다. 레거시 LegacyBookService 와 동일한 동작을 스펙대로 재구현합니다.
 *
 * <p>스프링 기본 빈 이름은 클래스명에서 유도된 {@code newBookService} 이며,
 * 라우터가 이 이름으로 활성 구현을 자동 선택합니다. 저장소는 공유 BookStore 를
 * 생성자 주입으로 받아 레거시 구현과 같은 데이터를 봅니다.</p>
 */
@Service
public class NewBookService implements BookService {

    private final BookStore store;

    // 공유 BookStore 생성자 주입 (레거시와 같은 데이터 공유)
    public NewBookService(BookStore store) {
        this.store = store;
    }

    @Override
    public Book register(String title) {
        // AC-B1: 식별자와 제목을 가진 도서를 생성해 저장
        return store.save(new Book(store.nextId(), title));
    }

    @Override
    public Book get(String id) {
        // AC-B2: 없는 식별자면 NOT_FOUND 로 거부
        return store.find(id).orElseThrow(() ->
                new LibraryException(LibraryException.Code.NOT_FOUND, "도서를 찾을 수 없습니다: " + id));
    }

    @Override
    public List<Book> list() {
        // AC-B3: 등록된 도서 전체 반환
        return store.all();
    }
}
