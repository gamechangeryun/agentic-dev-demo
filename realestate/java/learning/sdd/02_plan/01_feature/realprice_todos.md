# 실거래 수집·집계 · 비중첩 병렬 작업 (T1~T4)

> `03_architecture` 경계를 따라 모듈이 겹치지 않게 나눈 todos. 서로 다른 모듈만 만지므로 네 작업을 **동시에 돌려도 충돌이 없다**(병렬 안전).
> 각 작업은 `01_feature`의 AC를 책임 범위로 가진다. (SDD 3단계 산출물)

## 작업 분할
| 작업 | 담당 | 범위 | 만지는 모듈 (only) | 책임 AC |
| --- | --- | --- | --- | --- |
| **T1** | `@ingestion-dev` | data.go.kr 수집 · XML 파싱 · 정규화 · 회복력 | `ingestion-service/*` | AC-1, AC-2, AC-3(변환) |
| **T2** | `@transaction-dev` | 거래원장 멱등 적재(자연키 upsert) + 거래 조회 | `transaction-service/*` | AC-4 |
| **T3** | `@analytics-dev` | 시세 통계 read model(MarketStat) 집계·조회 | `analytics-service/*` | AC-5, AC-3(해제 제외) |
| **T4** | `@platform-dev` | 인프라(discovery·config·gateway) 계약 검증 + **common 공유 계약 소유** | `service-discovery/*`·`config-server/*`·`api-gateway/*`·`common/*` | AC-R(라우팅·디스커버리·계약 무손상) |

## todos
- [ ] **T1 @ingestion-dev** — `MolitApiClient`(WebClient + resilience4j Retry/CircuitBreaker/TimeLimiter), XML 파싱·전량 페이징, `AptTransactionNormalizer`(common 사용), `IngestionService`/`IngestionController` (`POST /api/v1/ingest`)
- [ ] **T2 @transaction-dev** — `AptTransaction` 엔티티·리포지토리, 자연키 **유니크 제약** + `existsByNaturalKey` 멱등 upsert, 거래 조회 (`GET /api/v1/transactions`)
- [ ] **T3 @analytics-dev** — `MarketStatCalculator`(해제 제외 후 중위 거래금액·㎡당 단가), `MarketStatService`(transaction 조회), `AnalyticsController` (`GET /api/v1/market-stats`)
- [ ] **T4 @platform-dev** — `common` 계약(`AptTransaction`·`DealAmountParser`·naturalKey) 정리·**동결**, 게이트웨이 3라우트·디스커버리·config 검증

## 비중첩 경계 (서로 다른 모듈만 만짐 → 병렬 안전)
```
T1 → ingestion-service/*
T2 → transaction-service/*
T3 → analytics-service/*
T4 → service-discovery/* · config-server/* · api-gateway/* · common/*
```

## common 소유 규칙 (← 발화: "common 소유자도 정해줘")
- **소유자 = T4 `@platform-dev`.** `common`(표준 스키마·정합 변환·자연키)은 **단일 소유**.
- T1·T2·T3는 common을 **소비(read)만** 하고 수정하지 않는다 — 역의존 금지, `04_data` 계약이 정본.
- common 계약은 병렬 시작 전 **선합의·동결**한다. 변경이 필요하면 T4에 요청 → `04_data` 갱신 → 재동결(병렬 중 common 동시 수정 금지).
- 의존 방향: `T1·T2·T3 → common` 단방향, `T3(analytics) → T2(transaction)` 조회(CQRS).
