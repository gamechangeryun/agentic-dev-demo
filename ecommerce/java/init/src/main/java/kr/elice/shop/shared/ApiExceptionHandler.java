package kr.elice.shop.shared;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 도메인 예외를 일관된 JSON 오류 응답으로 변환하는 전역 핸들러입니다.
 *
 * <p>모든 컨트롤러는 도메인 서비스를 그대로 호출하고, 규칙 위반은 예외로 전파됩니다.
 * 이 핸들러가 오류 코드에 맞는 HTTP 상태와 {@code {"code","message"}} 본문을 만듭니다.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handle(DomainException ex) {
        return ResponseEntity.status(ex.code().status())
                .body(Map.of("code", ex.code().name(), "message", ex.getMessage()));
    }
}
