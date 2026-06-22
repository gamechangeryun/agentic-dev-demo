package com.datasense.finance.controller;

import com.datasense.finance.domain.RuleGraph;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** 미등록 민원 유형 등 도메인 예외를 깔끔한 4xx로 변환한다. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RuleGraph.UnknownMinwonException.class)
    public ResponseEntity<Map<String, Object>> handleUnknownMinwon(RuleGraph.UnknownMinwonException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "rejected", "reason", "unknown_minwon",
                        "minwonType", e.getMessage()));
    }
}
