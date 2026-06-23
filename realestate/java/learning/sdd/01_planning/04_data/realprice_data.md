# 데이터 설계 — 표준 스키마 · 자연키 · 정합 규칙

> 출처(00_sources): 데이터 명세서(항목 사전·코드 도메인·표기 규칙) + API 공개명세(item 필드).
> 원천 item을 내부 표준 스키마 **AptTransaction**으로 사상하는 단일 정본. (SDD 2단계 산출물 · DAR-001/002/003/007)

## 1. 표준 스키마 AptTransaction (원천 → 표준 사상)
| 원천(item) | 표준 필드 | 타입 | 변환 |
| --- | --- | --- | --- |
| sggCd | sggCd | String(5) | 동일 |
| umdNm | umdNm | String | trim |
| aptNm | aptNm | String | trim |
| aptSeq | aptSeq | String | 단지 식별(시군구-단지번호) |
| jibun | jibun | String | trim, 결측 허용 |
| excluUseAr | exclusiveArea | double | 문자열 → double (㎡) |
| floor | floor | int | 문자열 → int |
| buildYear | buildYear | Integer | 문자열 → int, **결측 허용** |
| dealYear / dealMonth / dealDay | dealYear / dealMonth / dealDay | int | 문자열 → int |
| dealAmount | **dealAmountWon** | long | **콤마·공백 제거 → 만원 정수 → ×10000** (§3) |
| cdealType | **canceled** | boolean | `== "O"` → true |
| cdealDay | canceledDate | LocalDate? | `YY.MM.DD` → date, 결측 null |
| rgstDate | rgstDate | LocalDate? | `YY.MM.DD` → date, 결측 null |
| dealingGbn / slerGbn / buyerGbn / estateAgentSggNm / aptDong / landLeaseholdGbn | 동명 보존 | String | trim, 결측 허용(범주값 원천 보존) |

## 2. 자연키 (거래 동일성) — DAR-003
재수집 멱등(AC-4)의 기준. 거래원장에 **유니크 제약**을 둔다.

```
naturalKey = sggCd + aptSeq + exclusiveArea + floor
           + dealYear + dealMonth + dealDay + dealAmountWon
```
- **근거**: 같은 시군구·단지·전용면적·층·계약일·거래금액이면 동일 신고 거래로 본다.
- `aptSeq` 결측 시 보조키로 `umdNm + aptNm + jibun`을 사용한다.
- 재수집 시 동일 자연키면 **insert가 아니라 update** — `canceled`(사후 해제)만 갱신하고 중복 행을 만들지 않는다(CONR-005).

## 3. 금액 정규화 규칙 (DAR-002) — AC-3 정합
```
dealAmountWon = parseLong( dealAmount.replace(" ", "").replace(",", "") ) * 10000
  "  82,500"  → "82500" → 82500 → 825000000 (원)
```
- 변환 실패·0 이하 → 적재하지 않고 스킵·보고(데이터 품질 게이트).
- 이 규칙은 `common/DealAmountParser` 한곳에서만 강제한다(수집·집계 공유).

## 4. 해제 처리 (DAR-004) — AC-3 정합
| cdealType | canceled | 시세 집계 |
| --- | --- | --- |
| `O` | true | **제외** (cdealDay 보존) |
| (공백) | false | 포함 |
- 원장에는 해제 거래도 **남긴다**(논리 제외, DAR-005). 집계에서만 빠진다.

## 5. 집계 read model — MarketStat (DAR-007)
시군구·계약 연월 단위 집계. 해제(`canceled=true`) 제외 후 산출.
| 필드 | 산출 |
| --- | --- |
| sggCd, dealYear, dealMonth | 집계 키 |
| tradeCount | 유효(해제 제외) 거래 수 |
| medianAmountWon | 거래금액(원) **중위값** |
| medianPricePerArea | 거래별 `dealAmountWon / exclusiveArea`의 **중위값** (㎡당 단가) |

> 정합의 핵심 변환 4가지(만원→원, 문자열→숫자, 해제코드→boolean, `YY.MM.DD`→date)는 `common`에서 단일 강제한다.
