# 거래원장 서비스 구현 현황 (03_build · T2 transaction)

> current-state 문서. transaction-service 적재 코어의 현재 런타임 구성을 설명한다(실행 서술·날짜 없음).
> 담당 @transaction-dev · 책임 AC-4(멱등 적재). 표준 거래 계약은 `common/AptTransaction`(T1·T2·T3 공유).

## 구현 범위 (현재 상태)
거래원장 멱등 적재가 헥사고날(port·adapter·service) 구조로 **HTTP 진입까지 구현**되어 있다: 표준 거래를 자연키 기준으로 적재하되 재수집 중복을 차단하고, `TransactionController`가 `POST /api/v1/transactions`(ingestion의 배치 적재 수신)·`GET /api/v1/transactions`(조회)를 노출한다 + `TransactionApplication`(부트). 게이트웨이 경유 브라우저 E2E로 검증됨([[realfield_web_e2e]]).

## 모듈·컴포넌트
| 파일 | 책임 | AC |
| --- | --- | --- |
| `transaction/port/AptTradeStore.java` | 저장 포트(헥사고날): `existsByNaturalKey`·`save`·`findByRegionMonth`. 서비스는 이 계약에만 의존 | AC-4 |
| `transaction/service/TransactionCommandService.java` | 배치 멱등 적재 커맨드. 존재하면 skip, 신규만 save, 신규 건수 반환 | AC-4 |
| `transaction/adapter/JpaAptTradeStore.java` | 포트의 JPA 어댑터. 도메인 ↔ 엔티티 변환 흡수 | AC-4 |
| `transaction/adapter/AptTradeEntity.java` | 영속 엔티티. `natural_key` **유니크 제약**으로 DB 레벨 멱등 보장 | AC-4 |
| `transaction/adapter/AptTradeJpaRepository.java` | Spring Data JPA. 파생 쿼리 `existsByNaturalKey`·`findBySggCdAndDealYearAndDealMonth` | AC-4 |

> `common/AptTransaction`(11필드 + `naturalKey()`)을 계약 그대로 소비한다(역의존 없음). T1이 만든 정합 규칙(`DealAmountParser`)·표준 스키마를 변경 없이 사용한다.

## 현재 동작 (런타임)
- **멱등 적재**(AC-4): `TransactionCommandService.upsertAll(batch)`는 각 거래의 `naturalKey()`로 `existsByNaturalKey`를 확인해, 이미 있으면 건너뛰고(중복 0) 신규만 `save`한다. 신규로 삽입한 건수를 반환한다. 같은 배치를 두 번 적재해도 원장은 한 번만 기록된다.
- **자연키**(거래 동일성): `sggCd|umdNm|aptNm|전용면적|층|계약연·월·일|거래금액(원)`. 원천에 aptSeq가 없어 단지 식별은 umdNm·aptNm으로 대체한다. 해제 여부·건축년도는 동일성이 아니므로 키에서 제외.
- **이중 방어**: 애플리케이션 레벨 `existsByNaturalKey` skip + DB 레벨 `natural_key` 유니크 제약. 동시 적재 경합에서도 유니크 제약이 중복 행을 최종 차단한다.
- **포트/어댑터 분리**: 커맨드 서비스는 `AptTradeStore` 포트에만 의존하고 영속화 기술(JPA·H2)을 모른다. 단위 검증은 인메모리 포트 구현으로 DB 없이 수행한다.

## 검증 (proof)
- `./gradlew :common:test :ingestion-service:test :transaction-service:test` → **BUILD SUCCESSFUL**.
  - `IdempotentUpsertTest` 1/1 통과 (AC-4: 동일 배치 재적재 → 1차 1건, 2차 0건, 원장 1건 유지).
  - 공유 계약 수렴 회귀: `DealAmountParserTest` 2/2 · `AptTransactionNormalizerTest` 2/2 동반 통과(common `AptTransaction` 변경이 T1을 깨지 않음).
- 멱등 검증은 인메모리 포트로 결정적 수행. JPA 어댑터(H2)는 컴파일·빈 배선까지 현재 상태에 포함.

## 회귀 범위
- 직접: `transaction-service/*`.
- 공유: `common/AptTransaction`(T2가 `naturalKey()`·11필드 생성자를 사용·확정). 이 형태가 T1 정규화기·T3 집계기와 공유되므로, 본 증분에서 common을 정본 형태로 수렴시키고 T1 테스트로 회귀를 확인했다.
- 하류: analytics(T3)는 `findByRegionMonth` 조회 계약에 의존 예정(미구현). 상류: ingestion(T1) 적재 호출 대상.
- 제외(정당화): 게이트웨이 라우팅·디스커버리(인프라 제공·불변), web 프론트(별도 담당).

## 계약 드리프트 메모 (04_data 정합 필요)
- `01_planning/04_data`의 자연키는 `sggCd + aptSeq + …`(aptSeq 1차)로 기술돼 있으나, 제공된 원천 item·표준 스키마에는 aptSeq가 없다. 현재 구현은 문서에 명시된 **보조키(umdNm·aptNm)** 경로를 정본으로 채택했다. 04_data를 이 형태로 정합하는 것이 후속 작업으로 남는다.

## 잔여 범위 (T2 다음 증분)
- `GET /api/v1/transactions` 조회 컨트롤러(`findByRegionMonth` 노출, `canceled` 필터) — SFR-007.
- ingestion → transaction 적재 수신 경로(내부 API 또는 메시지) 배선 — T1 적재 호출과 짝.
- `@SpringBootApplication` 부트스트랩 + `@Entity` 스캔/`@EnableJpaRepositories` 확정, H2 스키마 유니크 제약 실런타임 검증.
