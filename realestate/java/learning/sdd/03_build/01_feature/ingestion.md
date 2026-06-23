# 수집 서비스 구현 현황 (03_build · T1 ingestion)

> current-state 문서. ingestion-service 수집 코어의 현재 런타임 구성을 설명한다(실행 서술·날짜 없음).
> 담당 @ingestion-dev · 책임 AC-1(수집·정규화) · AC-2(회복력) · AC-3(정합). 계약 정본은 `01_planning`.

## 구현 범위 (현재 상태)
data.go.kr(MOLIT) 아파트 매매 실거래가 수집이 **HTTP 진입까지 구현**되어 있다: 외부 호출(회복력)·원천 사상·표준 정규화·정합 변환 + `POST /api/v1/ingest/apt-trade` 컨트롤러 + `IngestionService`(수집→정규화→`lb://transaction-service` 멱등 적재) + `IngestionApplication`(부트). `stub` 프로필(`StubMolitClient`)로 오프라인 결정적 데이터를 공급한다. 게이트웨이 경유 브라우저 E2E로 검증됨([[realfield_web_e2e]]).

## 모듈·컴포넌트
| 파일 | 책임 | AC |
| --- | --- | --- |
| `common/DealAmountParser.java` | 정합 변환 단일 강제점: `toWon`(만원·콤마·공백 → 원 정수), `isCanceled`(`"O"`→true) | AC-3 |
| `common/AptTransaction.java` | 내부 표준 거래 스키마(record). 금액=원 정수, 해제=boolean. 세 도메인 공유 계약 | AC-1 |
| `ingestion/client/MolitAptTradeItem.java` | 원천 item 매핑(record, XML 필드명 사상). 외부 응답 의존을 client 경계에 격리 | AC-1 |
| `ingestion/client/MolitApiClient.java` | data.go.kr 호출 + 회복력 + 전량 페이징 | AC-1·2 |
| `ingestion/domain/AptTransactionNormalizer.java` | 원천 item → 표준 `AptTransaction` 변환, 금액·해제는 `DealAmountParser`에 위임 | AC-1·3 |

> `common/*`는 계약상 T4 @platform-dev 단일 소유다. 본 증분은 T1이 필요로 하는 표준 스키마·정합 규칙을 구현해 동결 후보로 올린 상태이며, 정합 규칙은 `DealAmountParser` 한곳에서만 강제한다(수집·집계 공유, 역의존 없음).

## 현재 동작 (런타임)
- **수집**(AC-1): `MolitApiClient.fetchAll(lawdCd, dealYmd)`가 `pageNo`를 1부터 증가시키며 누적 건수 ≥ `totalCount`까지 전 페이지를 수집한다. base-url·경로·인증키·numOfRows는 설정(`molit.*`)에서 주입하고, 인증키는 `MOLIT_SERVICE_KEY` 환경변수로만 받는다(SECR-001).
- **회복력**(AC-2): 페이지 호출에 `@Retry(name="molitApi", 최대 3회)` + `@CircuitBreaker(name="molitApi", fallback)`. 인스턴스명은 config-server가 외부화한 `resilience4j.*.instances.molitApi`(ingestion-service.yml)와 일치한다. open·실패 시 빈 결과로 폴백해 한 구간 실패가 배치 전체를 멈추지 않는다(부분 수집, SFR-011). 자기 호출이 프록시를 거치도록 self 참조로 페이지 단위 어드바이스를 보장한다.
- **정규화**(AC-1): `AptTransactionNormalizer`가 원천 문자열 item을 표준 `AptTransaction`(11필드: 면적 double, 층·건축년도·연·월·일 int, 금액 long, 해제 boolean)으로 사상한다. 표준 스키마는 거래 동일성에 필요한 필드만 보존하며(원천 item의 jibun·dealingGbn·등기일자 등은 비보존), 원천 형태 의존은 client 경계에 남긴다.
- **정합**(AC-3): 거래금액은 `replace(",")·replace(" ")` 후 만원 정수 → ×10,000 원 변환(예: `" 82,500"` → `825,000,000`). 빈 값·숫자 아님·0 이하는 예외로 거부(품질 게이트). 해제(`cdealType="O"`)는 `canceled=true`로 표시하며 원장에는 남기고 집계에서만 제외한다.

## 검증 (proof)
- `./gradlew :common:test :ingestion-service:test` → **BUILD SUCCESSFUL**.
  - `DealAmountParserTest` 2/2 통과 (AC-3 콤마 금액 변환·잘못된 형식 거부).
  - `AptTransactionNormalizerTest` 2/2 통과 (AC-1 표준 정규화·AC-3 해제 표시).
- 단위 검증은 외부 data.go.kr 호출 없이 결정적(순수 도메인). 인증키·네트워크는 런타임 전용.

## 회귀 범위
- 직접: `ingestion-service/*`.
- 공유: `common`(표준 스키마·정합 변환) — 변경 시 transaction·analytics 회귀 동반. 현재 소비처는 transaction-service(T2, 구현됨)이며, T2 계약 테스트가 `AptTransaction`(11필드+`naturalKey()`)을 함께 검증해 회귀를 막는다. analytics(T3)는 미구현.
- 제외(정당화): 게이트웨이 라우팅·디스커버리(인프라 제공·불변), web 프론트(별도 담당).

## 잔여 범위 (T1 다음 증분)
- `IngestionService`/`IngestionController` — `POST /api/v1/ingest` 진입 + 수집 결과(`fetched/upserted/skipped/canceled`) 응답(SFR-010).
- transaction-service 적재 호출(`lb://transaction-service`) 배선 — T2 멱등 upsert 계약 확정 후 연결.
- `@SpringBootApplication` 부트스트랩 + resilience4j 인스턴스(`molit`) 설정값 외부화(config-server).
