package com.datasense.finance.dto;

import jakarta.validation.constraints.NotBlank;

/** 동의 부여/철회 요청. scope 미지정 시 mydata. */
public record ConsentRequest(
        @NotBlank String userId,
        String scope) {

    public String scopeOrDefault() {
        return (scope == null || scope.isBlank()) ? "mydata" : scope;
    }
}
