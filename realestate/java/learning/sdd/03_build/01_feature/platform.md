# 플랫폼(인프라) 구현 현황 (03_build · T4 platform)

> current-state 문서. 인프라 3서비스의 현재 런타임 구성과 공유 계약 검증 결과를 설명한다(실행 서술·날짜 없음).
> 담당 @platform-dev · 책임 AC-R(라우팅·디스커버리·외부화·계약 무손상). 도메인 모듈은 이 인프라 위에서 동작한다.

## 구현 범위 (현재 상태)
단일 진입점·서비스 디스커버리·설정 외부화 3서비스가 부트스트랩까지 갖춰져 있다. 인프라는 제공·동결 대상이며, T4는 도메인 작업(T1·T2·T3)과 충돌하지 않는 공유 계약(라우트·인스턴스명·외부화 키)을 **검증·정합**한다.

## 모듈·컴포넌트
| 모듈 | 진입 클래스 | 포트 | 책임 |
| --- | --- | --- | --- |
| `service-discovery` | `DiscoveryApplication`(`@EnableEurekaServer`) | 8761 | Eureka 서버. 자신은 미등록(`register/fetch=false`) |
| `config-server` | `ConfigServerApplication`(`@EnableConfigServer`) | 8888 | 설정 외부화. `native` classpath:/config 백엔드 |
| `api-gateway` | `ApiGatewayApplication` | 8080 | 단일 진입점. 디스커버리 locator + 명시 라우트 |

## 현재 동작 (런타임)
- **단일 진입점**(AC-R): 게이트웨이(8080)가 3개 도메인 라우트를 노출한다 — `/api/v1/ingest/** → lb://ingestion-service`, `/api/v1/transactions/** → lb://transaction-service`, `/api/v1/market-stats/** → lb://analytics-service`. discovery locator 활성(`lower-case-service-id`)으로 Eureka 등록 서비스명 기반 자동 라우팅도 동작한다.
- **서비스 디스커버리**(AC-R): 모든 도메인 서비스가 Eureka(8761)에 등록되고, 게이트웨이·analytics가 `lb://`로 등록명을 통해 호출한다.
- **설정 외부화**(AC-R · SECR-001): config-server(8888)가 `ingestion-service.yml`을 제공한다 — MOLIT base-url/path, `num-of-rows`, **인증키는 `${MOLIT_SERVICE_KEY}` 환경변수 주입**(평문 저장 금지), resilience4j 인스턴스 `molitApi`(circuitbreaker sliding-window·failure-rate, retry max-attempts 3·backoff).

## 공유 계약 검증 (T4의 핵심 작업)
- **게이트웨이 3라우트** ↔ `05_api` 정본 일치(ingest·transactions·market-stats). 도메인 서비스명·경로 무손상 확인.
- **resilience4j 인스턴스명 정합**: config-server 외부화 인스턴스명은 `molitApi`. ingestion T1 클라이언트의 `@Retry`/`@CircuitBreaker` 인스턴스명을 `molitApi`로 정합해 외부화 설정이 실제 바인딩되도록 확정했다(불일치 시 정책 미적용·디폴트 폴백 위험을 제거).
- **CQRS 분리**: analytics → `lb://transaction-service` 조회 계약(읽기/쓰기 분리) 유지.

## 검증 (proof)
- 아키텍처 게이트(`python sdd/99_toolchain/01_automation/run_arch_check.py`) → **RESULT: PASS (7/7)**:
  1. 필수 7모듈 포함, 2. common 역의존 없음, 3. 도메인 3모듈 → common 의존, 4. 게이트웨이 3라우트, 5. analytics → transaction(CQRS).
- `./gradlew test` → **BUILD SUCCESSFUL**(전 모듈). 인프라 3모듈은 테스트 소스 없음(NO-SOURCE) — 구조·계약 검증은 arch 게이트가 담당.

## 회귀 범위
- 직접: `service-discovery/*`·`config-server/*`·`api-gateway/*`.
- 공유 영향: 라우트·인스턴스명·외부화 키 변경은 도메인 전체 회귀 → 본 증분은 라우트·`molitApi` 명을 정본으로 고정.
- 제외(정당화): 도메인 비즈니스 로직(T1·T2·T3 소유), web 프론트(별도 담당).

## 잔여 범위 / 메모
- 외부화 설정은 현재 `ingestion-service.yml`만 제공(transaction·analytics는 로컬 `application.yml` 사용). 운영은 native 대신 git backend 권장(현재 데모는 classpath native).
- 게이트웨이 표준 오류 매핑·타임아웃·CORS 등 cross-cutting 정책은 후속 증분 대상.
