package com.datasense.finance.dto;

import jakarta.validation.constraints.NotBlank;

/** 예외 조건 충족 등록 요청 (AC-3 자격). 예: condition="세대주 동의". */
public record SatisfyRequest(
        @NotBlank String userId,
        @NotBlank String condition) {
}
