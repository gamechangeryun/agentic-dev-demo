package com.datasense.finance.domain;

/**
 * 동의 원장 레코드 (append-only). status: granted | withdrawn | expired.
 * (shared/consent_ledger.py 의 ConsentRecord 포팅)
 */
public record ConsentRecord(String userId, String scope, String status, int seq) {
}
