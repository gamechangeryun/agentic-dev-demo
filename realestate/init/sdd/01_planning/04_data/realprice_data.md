# 데이터 모델 (04_data)

> 2단계 산출물입니다. 표준 거래 스키마와 멱등 자연키를 정의합니다.

## 표준 거래 스키마 (AptTransaction)

| 필드 | 타입 | 의미 | 원천 매핑 |
| --- | --- | --- | --- |
| sggCd | String(5) | 법정동 시군구코드 | `sggCd` |
| umdNm | String | 법정동(읍면동)명 | `umdNm` |
| aptNm | String | 단지명 | `aptNm` |
| exclusiveArea | double | 전용면적(㎡) | `excluUseAr` |
| floor | int | 층 | `floor` |
| buildYear | int | 건축년도 | `buildYear` |
| dealYear/Month/Day | int | 계약 연/월/일 | `dealYear/Month/Day` |
| dealAmountWon | long | 거래금액(원) | `dealAmount`(만원·콤마) → ×10000 |
| canceled | boolean | 해제여부 | `cdealType == "O"` |

## 멱등 자연키 (AC-4)
```
naturalKey = sggCd | umdNm | aptNm | exclusiveArea | floor
           | dealYear | dealMonth | dealDay | dealAmountWon
```
- 거래원장 테이블에 자연키 유니크 제약을 둡니다. 같은 구간 재수집 시 중복이 생기지 않습니다.

## 정합 규칙 (AC-3)
- `dealAmount` " 82,500"(만원) → 공백·콤마 제거 → 82500 → ×10000 → 825,000,000원.
- `canceled = true`(해제)인 거래는 적재하되 시세 집계에서 제외합니다.

## read model (MarketStat)
| 필드 | 의미 |
| --- | --- |
| tradeCount | 집계 대상 거래 수(해제 제외) |
| medianPriceWon | 중위 거래금액(원) |
| medianPricePerM2Won | 중위 ㎡당 단가(원) |
