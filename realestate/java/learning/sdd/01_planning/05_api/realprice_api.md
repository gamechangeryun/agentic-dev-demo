# API 계약 — 게이트웨이 라우팅 · 엔드포인트

> 출처(00_sources): 요구사항정의서(SFR-007/008/010, SIR-003) + 실제 게이트웨이 구성.
> 모든 내부 API는 `api-gateway`(:8080) 단일 진입점으로 노출하고, 디스커버리 LB로 라우팅한다. (SDD 2단계 산출물)

## 1. 게이트웨이 라우팅 (정본 — 실제 구성과 일치)
| 라우트 | Path 예측자 | 대상 서비스 |
| --- | --- | --- |
| ingestion | `/api/v1/ingest/**` | `lb://ingestion-service` |
| transaction | `/api/v1/transactions/**` | `lb://transaction-service` |
| analytics | `/api/v1/market-stats/**` | `lb://analytics-service` |
- discovery locator 활성(`lower-case-service-id`), Eureka(:8761) 등록 서비스명 기준 자동 라우팅.

## 2. 엔드포인트 계약

### 2.1 수집 트리거 — `POST /api/v1/ingest` (AC-1, SFR-010)
요청(JSON):
```json
{ "lawdCd": "11110", "dealYmd": "202405" }
```
응답(JSON):
```json
{ "lawdCd": "11110", "dealYmd": "202405",
  "fetched": 143, "upserted": 140, "skipped": 3, "canceled": 1 }
```
- `lawdCd`(5자리)·`dealYmd`(YYYYMM) 필수. 전 페이지 전량 수집(SFR-002).
- 멱등: 동일 구간 재호출 시 `upserted`는 신규/변경분만(중복 0, AC-4).

### 2.2 거래 조회 — `GET /api/v1/transactions` (AC-1, SFR-007)
쿼리: `sggCd`(필수), `year`(필수), `month`(필수), `canceled`(선택, 기본 포함)
응답: `AptTransaction[]` (umdNm·aptNm·exclusiveArea·floor·dealDate·dealAmountWon·canceled …)

### 2.3 시세 통계 — `GET /api/v1/market-stats` (AC-5, SFR-008)
쿼리: `sggCd`(필수), `year`(필수), `month`(필수)
응답(JSON):
```json
{ "sggCd": "11110", "year": 2024, "month": 5,
  "tradeCount": 4, "medianAmountWon": 800000000, "medianPricePerArea": 9400000 }
```
- analytics **read model**에서 산출(거래원장 직접 조회 아님, CQRS). 해제 거래 제외.

## 3. 공통 규약
- 응답 `Content-Type: application/json`. 시각·금액은 원 단위 정수(`dealAmountWon`).
- 오류는 게이트웨이 표준 상태코드로 매핑(외부 data.go.kr 장애는 ingestion 내부에서 흡수, §07_integration).
