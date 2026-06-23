# 실거래 수집·집계 · 작업 계획 (02_plan)

> SDD 'plan' 산출물. 03_architecture 경계를 비중첩 T1~T4로 내리고 proof 게이트를 확정한다.
> 작업 분할 상세는 `realprice_todos.md`.

## Scope
- 변경 대상 모듈: 도메인 4모듈 `common` · `ingestion-service` · `transaction-service` · `analytics-service` 구현으로 AC-1~AC-5 + AC-R 충족.
- 엔드포인트: `POST /api/v1/ingest` · `GET /api/v1/transactions` · `GET /api/v1/market-stats`(게이트웨이 경유).
- 범위 밖: 인프라 3모듈(discovery·config·gateway)은 제공됨 — 계약 검증만. 전월세·타 부동산 유형, 프론트(web)는 별도.

## Assumptions
- 환경: DEV(개발계).
- 의존/선결: `common` 계약(`AptTransaction`·`DealAmountParser`·naturalKey)을 `04_data` 정본으로 **선합의·동결** 후 T1~T3 병렬 시작.
- 단위 검증은 외부 data.go.kr 호출 없이 결정적(stub·순수 도메인). 인증키·네트워크는 런타임 전용.
- 스택 Java 21 · Spring Boot 3.5 · Spring Cloud 2025.0 고정.

## Acceptance Criteria (← 01_feature)
- AC-1 수집·표준적재 / AC-2 회복력 / AC-3 금액변환·해제제외 / AC-4 멱등 / AC-5 CQRS / AC-R 회귀(라우팅·디스커버리·기존 계약 무손상).
- 배포는 이번 플랜 범위 밖 → DEV/PROD 게이트·rollback 미적용(구현 + 게이트 검증까지가 완료선).
- persistence 변경(거래원장 자연키 유니크) → 단위 멱등 테스트로 검증(외부 스키마 게이트 비대상, DEV 한정).

## 모듈 의존 그래프
```
common ──┬─→ ingestion-service ──(정규화 결과 적재)──→ transaction-service
         ├─→ transaction-service
         └─→ analytics-service ──(WebClient, lb)──→ transaction-service
(common 역의존 없음 · 도메인 → common 단방향)
```

## 런타임 흐름 (end-to-end)
```
POST /api/v1/ingest → gateway → ingestion-service → data.go.kr(회복력) → 정규화 → transaction-service(멱등 upsert)
GET  /api/v1/transactions → gateway → transaction-service(조회)
GET  /api/v1/market-stats → gateway → analytics-service → transaction-service(조회) → 해제 제외 중위 집계
```

## 회귀 범위 (regression scope)
- 직접: 도메인 4모듈.
- 상류: 게이트웨이 라우팅(3라우트), 디스커버리 등록.
- 하류: analytics → transaction 조회 계약.
- 공유: `common`(표준 스키마·정합 변환·자연키) — 변경 시 세 도메인 전체 회귀.
- 정당화된 제외: web 프론트(별도 담당), 인프라 내부 구현(제공·불변).

## Execution Checklist
- [ ] **(in-progress)** T4 — `common` 계약 정리·동결 (선행: 공유 계약)
- [ ] T1 — ingestion (병렬)
- [ ] T2 — transaction (병렬)
- [ ] T3 — analytics (병렬, transaction 조회 의존)
- [ ] 통합 — 게이트웨이 경유 `ingest → transactions → market-stats` 흐름 확인

## Current Notes
- 병렬 안전의 전제는 **모듈 비중첩**. 공유점은 `common` 하나뿐이며 단일 소유(T4)로 충돌 제거.
- `analytics → transaction`은 코드 결합이 아니라 `05_api` 조회 계약으로 결합 최소화.

## Validation (proof 게이트)
- `./gradlew test` exit 0 — AC-3 금액/해제 · AC-4 멱등 · AC-5 중위 집계.
- `python3 sdd/99_toolchain/01_automation/run_arch_check.py` exit 0 — 7모듈·common 역의존 없음·도메인→common·게이트웨이 3라우트·analytics→transaction(CQRS).
