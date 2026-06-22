package com.datasense.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 전자민원 발급 요청.
 * failFirstAttempts: 기관 응답 회복력(AC-2) 데모용. 각 서류마다 앞선 N회 시도를 미응답 처리한다.
 */
public record IssueRequest(
        @NotBlank String userId,
        @NotBlank String minwonType,
        @PositiveOrZero int failFirstAttempts) {
}
