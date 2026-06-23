# 실거래 수집·집계 · 검증 정리 (04_verify · proof 게이트)

> retained 검증 산출물. 완료선은 사람의 눈이 아니라 게이트 통과다. `./lab.sh verify` 한 줄로
> AC별 단위 테스트와 아키텍처 게이트가 결정적·오프라인으로 함께 돈다. 테스트 하나가 AC 한 줄과 1:1로 맞물린다.

## proof 게이트
```
$ export MOLIT_SERVICE_KEY='data.go.kr 인증키'   # 런타임 전용(단위 테스트는 외부 호출 없이 결정적)
$ ./lab.sh verify
[verify] 1/2 아키텍처 게이트 → RESULT: PASS (7/7)
[verify] 2/2 gradle 단위 테스트 → 단위 8/8
[verify] 통과: 아키텍처 게이트 + 단위 8/8
```

## AC ↔ 단위 테스트 1:1 (8/8 통과)
| AC | 테스트 클래스 · 메서드 | 검증 내용 | 결과 |
| --- | --- | --- | --- |
| AC-3 | `DealAmountParserTest.parsesCommaSeparatedAmount` | 콤마·공백 만원 → 원 정수(`" 82,500"`→825,000,000) | ✅ |
| AC-3 | `DealAmountParserTest.rejectsInvalidAmount` | 빈 값·숫자 아님 거부(품질 게이트) | ✅ |
| AC-1 | `AptTransactionNormalizerTest.normalizesRawItem` | 원천 item → 표준 `AptTransaction` 정규화 | ✅ |
| AC-3 | `AptTransactionNormalizerTest.marksCanceledDeal` | `cdealType="O"` → `canceled=true` 표시 | ✅ |
| AC-4 | `IdempotentUpsertTest.reingestionDoesNotDuplicate` | 동일 배치 재적재 → 1차 1건·2차 0건·원장 1건 | ✅ |
| AC-5 | `MarketStatCalculatorTest.medianOfOddCount` | 홀수 건 중위 거래금액(700·800·900M→800M) | ✅ |
| AC-3·5 | `MarketStatCalculatorTest.excludesCanceledDeals` | 해제 제외 후 중위(700·800M+해제 5,000M→2건·750M) | ✅ |
| AC-5 | `MarketStatCalculatorTest.emptyWhenNoTrades` | 빈 입력 경계(0건·0원) | ✅ |
- 클래스 4 · 메서드 8 · failures 0 · errors 0. data.go.kr 호출 없이 순수 도메인으로 결정적 검증.

## 아키텍처 게이트 (run_arch_check.py · 7/7 PASS)
| # | 규칙 | 결과 |
| --- | --- | --- |
| 1 | 필수 7모듈 포함(settings.gradle) | ✅ |
| 2 | common이 도메인 모듈에 역의존하지 않음 | ✅ |
| 3 | ingestion·transaction·analytics → common 계약 의존 | ✅ (3건) |
| 4 | 게이트웨이 3라우트(ingest·transactions·market-stats) | ✅ |
| 5 | analytics → transaction 조회(CQRS read 분리) | ✅ |
- AC-R(라우팅·디스커버리·계약 무손상)은 이 구조 게이트가 기계로 강제한다.

## 회귀 범위 (02_plan·03_build에서 이어진 retained 선택)
- 직접: 도메인 4모듈(common·ingestion·transaction·analytics) 전 구현.
- 공유 수렴 회귀: `common/AptTransaction`(11필드+`naturalKey()`) 변경이 T1 정규화·T3 집계를 깨지 않음을 동일 게이트로 확인(T1·T3 테스트 동반 통과).
- 상·하류 계약: 게이트웨이 3라우트·analytics→transaction 조회 계약을 arch 게이트로 검증.
- 정당화된 제외: web 프론트(별도 담당·`web-e2e` 게이트), 인프라 내부 구현(제공·불변).

## 잔여 위험 (residual risk)
- **AC-2(회복력)은 결정적 단위 테스트 부재**: resilience4j(`@Retry`/`@CircuitBreaker` name=`molitApi`, fallback)·외부화 설정으로 구현됐고 인스턴스명 정합까지 확인했으나, 단위 게이트로는 검증되지 않는다(런타임/통합 영역). 서비스 부팅 E2E(`./lab.sh e2e`)에서 확인 대상.
- **미배선 표면**: 각 서비스의 HTTP 진입(`POST /api/v1/ingest`, `GET /api/v1/transactions`, `GET /api/v1/market-stats`)·서비스 간 호출(WebClient `lb://`)·`@SpringBootApplication`은 다음 증분. 현재 게이트는 도메인 코어·구조까지 보증한다.
- **계약 드리프트(문서 정합 필요)**: 자연키 `aptSeq`(04_data) vs 원천 무 aptSeq→`umdNm·aptNm` 보조키 채택, 중위가격 필드명 `medianAmountWon`(05_api) vs `medianPriceWon`(계약), `medianPricePerArea`(DAR-007) 미구현. 03_build 각 문서에 기록됨.
- **E2E 미실행**: `./lab.sh e2e`(docker compose 6서비스)·`web-e2e`(Playwright)는 본 proof 게이트 범위 밖.

## 검증 환경 (재현 메모)
- OS Windows · JDK 17(Adoptium, `JAVA_HOME` 설정) · Gradle(toolchain 17).
- `lab.sh`는 `python3`를 호출한다. Windows의 `python3`가 Microsoft Store 스텁이면 실제 파이썬으로 향하는 shim을 PATH 앞에 두고 실행한다(검증 결과 동일·결정적).
- 단위 테스트 소스(UTF-8)와 JVM 기본 charset(MS949) 불일치로 `unmappable character` 경고가 출력되나 컴파일·통과에는 영향 없다(단언문은 수치). 근본 해결은 루트 `build.gradle`에 `compileJava.options.encoding='UTF-8'` 추가(인프라=T4 소유 영역).
