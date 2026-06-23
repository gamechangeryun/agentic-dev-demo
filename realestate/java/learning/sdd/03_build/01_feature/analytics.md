# 시세 통계 서비스 구현 현황 (03_build · T3 analytics)

> current-state 문서. analytics-service 집계 코어의 현재 런타임 구성을 설명한다(실행 서술·날짜 없음).
> 담당 @analytics-dev · 책임 AC-5(CQRS read model)·AC-3(해제 제외). 표준 거래 계약은 `common/AptTransaction`.

## 구현 범위 (현재 상태)
시세 통계 read model이 **HTTP 진입까지 구현**되어 있다: 조회된 거래 목록에서 해제거래를 제외하고 중위 거래금액·중위 ㎡당 단가를 산출한다(거래원장 미복제 CQRS). `TransactionQueryClient`(`lb://transaction-service` WebClient)로 거래를 조회하고 `AnalyticsController`가 `GET /api/v1/market-stats`를 노출한다 + `AnalyticsApplication`(부트). 게이트웨이 경유 브라우저 E2E로 검증됨([[realfield_web_e2e]]).

## 모듈·컴포넌트
| 파일 | 책임 | AC |
| --- | --- | --- |
| `analytics/domain/MarketStat.java` | 집계 결과 read model(record): 시군구·연월·거래수·중위가격(원) | AC-5 |
| `analytics/domain/MarketStatCalculator.java` | 해제 제외 → 정렬 → 중위 거래금액 산출(순수 도메인) | AC-5·3 |
| `sdd/03_build/01_feature/analytics.md` | current-state 갱신 | — |

> `common/AptTransaction`을 계약 그대로 소비한다(역의존 없음). T2가 확정한 11필드 스키마·`canceled` 플래그를 변경 없이 사용한다.

## 현재 동작 (런타임)
- **CQRS read model**(AC-5): `MarketStatCalculator.calculate(sggCd, year, month, transactions)`가 거래 목록을 입력받아 `MarketStat`를 산출한다. 거래원장을 직접 변경/조회하지 않고 read 전용으로 분리된다(write=transaction-service, read=analytics-service).
- **해제 제외**(AC-3): 집계 전 `canceled=true` 거래를 필터링한다. 해제 이상치(예: 50억 해제건)가 중위가격을 왜곡하지 않는다.
- **중위가격**: 금액 오름차순 정렬 후 홀수 개면 가운데 값, 짝수 개면 가운데 두 값의 평균(원 단위 정수). 거래가 없으면 거래수 0·중위가격 0.
- **순수 도메인**: 계산기는 DB·네트워크에 의존하지 않아 결정적으로 단위 검증된다. 거래 조회는 상위 서비스(미배선)가 주입한다.

## 검증 (proof)
- `./gradlew test` → **BUILD SUCCESSFUL**. `MarketStatCalculatorTest` 3/3 통과:
  - AC-5 홀수 건 중위값(700·800·900M → 800M),
  - AC-3 해제 제외(700·800M + 해제 5,000M → 거래수 2·중위 750M),
  - 빈 입력(거래수 0·중위 0).
- 아키텍처 게이트(`run_arch_check.py`) **7/7 PASS**: analytics → common 의존·analytics → transaction(CQRS read 분리) 포함.

## 회귀 범위
- 직접: `analytics-service/*`.
- 공유: `common/AptTransaction`(소비만, 역의존 없음).
- 하류 의존: transaction-service 거래 조회(`lb://transaction-service`, 현재 application.yml에 base-url 존재, Java 배선은 잔여).
- 제외(정당화): 게이트웨이 라우팅·디스커버리(인프라 제공·불변), web 프론트(별도 담당).

## 계약 드리프트 메모 (05_api·04_data 정합 필요)
- 집계 read model의 중위 거래금액 필드명은 계약 테스트 기준 **`medianPriceWon`**으로 확정했다. `01_planning/05_api`의 응답 예시는 `medianAmountWon`으로 기술돼 있어 명칭 정합이 필요하다.
- `04_data`(DAR-007)의 `medianPricePerArea`(㎡당 단가 중위값)는 본 계약 테스트 범위 밖이라 미구현이다(아래 잔여 범위).

## 잔여 범위 (T3 다음 증분)
- 거래 조회 어댑터(`WebClient` `lb://transaction-service`)와 `MarketStatService` — read model에 거래를 주입.
- `GET /api/v1/market-stats` 컨트롤러 — SFR-008.
- `medianPricePerArea`(㎡당 단가 중위값) 산출 추가 — `04_data` DAR-007 충족.
- `@SpringBootApplication` 부트스트랩.
