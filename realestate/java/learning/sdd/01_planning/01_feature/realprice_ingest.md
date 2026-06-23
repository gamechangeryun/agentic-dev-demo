# 기능 수용기준 — 실거래 수집·정규화·조회 (EARS)

> 출처(00_sources): 요구사항정의서(SFR/DAR) · API 공개명세(getRTMSDataSvcAptTradeDev) · 데이터 명세서.
> 원문 핵심요구 1~5를 통과/실패를 판정할 수 있는 EARS 수용기준 AC-1~AC-5로 정제한다.
> 구현·검증 에이전트는 이 수용기준을 단일 기준으로 삼는다. (SDD 1단계 '구조화' 산출물)

## EARS 표기 규약
- 이벤트형: **WHEN** \<트리거\>, the system **shall** \<응답\>
- 상태형: **WHILE** \<상태\>, the system **shall** \<응답\>
- 조건형: **IF** \<조건\>, **THEN** the system **shall** \<응답\>
- 항상형: the system **shall** \<불변 규칙\>

---

### AC-1 · 수집과 표준 적재  (← SFR-001~003, SFR-002, DAR-001)
**WHEN** 시군구코드(LAWD_CD, 5자리)와 계약월(DEAL_YMD, YYYYMM)로 수집을 요청하면,
the system **shall** data.go.kr `getRTMSDataSvcAptTradeDev`를 호출하고,
`totalCount` 기준으로 전 페이지를 누락 없이(numOfRows 페이징) 수집하며,
각 `item`을 표준 스키마 **AptTransaction**으로 정규화해 거래원장에 적재한다.

### AC-2 · 외부 장애 무중단  (← SFR-011, SIR-005, PER-003, CONR-001)
**WHILE** 외부 API가 지연되거나 오류(`resultCode ≠ 000`, HTTP 오류)를 반환하는 동안,
the system **shall** 타임아웃 → 재시도(최대 3회) → 서킷브레이커(개방 시 빈 결과) 순으로 우아하게 저하하고,
특정 시군구·페이지의 실패가 배치 전체를 멈추지 않도록 **성공 구간만 적재**하며 수집을 계속한다. (회복력)

### AC-3 · 데이터 정합 — 금액 변환·해제 제외  (← SFR-004, SFR-006, DAR-002, DAR-004) **[핵심]**
the system **shall** `dealAmount` 문자열의 **선행 공백·천단위 콤마를 제거**해 만원 정수를 얻고,
**×10,000** 하여 원 단위 정수 `dealAmountWon`으로 저장한다.
> 예: `"  82,500"` → `825,000,000` (원)

**IF** `cdealType == "O"` (계약 해제), **THEN** the system **shall** `canceled = true`로 표시하고
해제사유발생일(`cdealDay`)을 보존하되, **시세 집계에서 해당 거래를 제외**한다.
(정상 거래 `cdealType == ""` → `canceled = false`, 집계 포함)

### AC-4 · 멱등 적재  (← SFR-005, SFR-013, DAR-003, CONR-005)
**WHEN** 동일 (시군구·계약월) 구간을 재수집하거나 과거 구간을 백필하면,
the system **shall** 거래 **자연키 기준 upsert**로 중복 행을 만들지 않는다.
원천에서 사후에 해제로 전환된 거래는 새 행을 만들지 않고 기존 행의 `canceled` 상태만 갱신한다.

### AC-5 · 시세는 분리된 조회 모델  (← SFR-008, SFR-009, DAR-007, PER-001/003)
**WHEN** 시군구·계약 연월로 시세 통계를 조회하면,
the system **shall** 거래원장(write model)이 아니라 **분석 read model(MarketStat)**에서,
해제 거래를 제외한 뒤 **집계 거래 수 · 중위 거래금액 · 중위 ㎡당 단가**를 반환한다. (CQRS 읽기 분리)

### AC-R · 회귀
기존 게이트웨이 단일 진입 라우팅 · 디스커버리 등록 · 거래/통계 조회 API 계약이 무손상이어야 한다.

---

## 추적 매트릭스 (원문 → AC → 근거 요구)
| 원문 핵심요구 | AC | SFR/DAR/PER 근거 |
| --- | --- | --- |
| 1. 시군구·계약월 수집·표준 적재 | AC-1 | SFR-001~003, SFR-002, DAR-001 |
| 2. 외부 지연·실패 무중단(회복력) | AC-2 | SFR-011, SIR-005, PER-003 |
| 3. 콤마 금액 변환·해제 제외(정합) | AC-3 | SFR-004, SFR-006, DAR-002, DAR-004 |
| 4. 재수집 중복 0(멱등) | AC-4 | SFR-005, SFR-013, DAR-003 |
| 5. 통계는 분리된 조회 모델(CQRS) | AC-5 | SFR-008, SFR-009, DAR-007 |

> 필수항목·금액 양수·면적 양수 등 적재 전 품질 게이트는 데이터 명세서 §5를 따른다(위반 건 스킵·보고, 부분 수집 허용).
