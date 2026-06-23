# 브라우저 E2E 검증 (04_verify · 02_screen)

> retained 검증 산출물. 게이트웨이 위 전체 스택을 로컬 부팅하고 실제 브라우저로 수집→조회→분석
> 한 흐름을 결정적으로 검증한 현재 상태. (Docker 미사용 — 로컬 6프로세스 부팅)

## 검증 대상 흐름
Next 프론트(:3100) → Next route handler 프록시(`/api/gateway`) → API 게이트웨이(:8080) →
Eureka `lb://`로 ingestion(stub)·transaction·analytics 라우팅 → H2 거래원장. 백엔드는 stub 프로필이라 값이 고정이다.

## 실행 스택 (로컬 6프로세스, Docker 없이)
| 프로세스 | 포트 | 비고 |
| --- | --- | --- |
| service-discovery (Eureka) | 8761 | `eureka.instance.hostname=localhost`로 등록 |
| transaction-service | 8082 | H2 in-memory write model |
| analytics-service | 8083 | CQRS read (transaction 조회) |
| ingestion-service | 8081 | `SPRING_PROFILES_ACTIVE=stub` (오프라인 캔드 데이터) |
| api-gateway | 8080 | 단일 진입점, discovery lb |
| Next (next start) | 3100 | `.next` 프로덕션 빌드, `GATEWAY_URL=http://localhost:8080` |
- config-server(8888)는 stub 흐름에 불필요하여 생략(ingestion의 `spring.config.import`는 `optional:`).

## 백엔드 HTTP 단언 (게이트웨이 경유)
- `POST /api/v1/ingest/apt-trade?lawdCd=11110&dealYmd=202405` → `{"upserted":5}` (최초), 재호출 → `{"upserted":0}` (멱등, AC-4).
- `GET /api/v1/transactions?sggCd=11110&dealYear=2024&dealMonth=5` → 5행(정상 4 + 해제 1).
- `GET /api/v1/market-stats?sggCd=11110&dealYear=2024&dealMonth=5` → `{"tradeCount":4,"medianPriceWon":850000000,"medianPricePerM2Won":8841410}` — 해제 1건 제외 후 중위 8.5억(AC-5·AC-3).

## 브라우저 E2E (Playwright · chromium)
```
npx playwright test  (BASE_URL=http://localhost:3100, GATEWAY_URL=http://localhost:8080)
  ok 1  [chromium] RealField 실거래 분석 3화면 › 수집 → 거래조회 → 시세분석 전 흐름이 결정적으로 통과한다
  1 passed
```
검증 항목(`e2e/realfield.spec.ts`):
- 수집: lawdCd·dealYmd 입력 → `btn-ingest` → `ingest-result`에 `N건` 노출.
- 거래조회: 조회 → `transactions-table` 5행(`tx-row`×5), `cancel-badge`×1 → `filter-canceled` 토글 → 4행·배지 0.
- 시세분석: 분석 → `stat-tradecount`="4", `stat-median`에 "8억", `price-chart`(산점도) 표시.

## 검증 환경에서 해결한 이슈 (로컬 부팅 특이사항)
1. **Eureka lb 해소 실패**: 인스턴스가 머신 호스트명(Tailscale IP `100.91.154.104`)으로 등록돼 `lb://` 연결이 타임아웃 → 모든 서비스 `EUREKA_INSTANCE_HOSTNAME=localhost`로 재기동해 해결.
2. **포트 3000 점유(Grafana)**: Playwright `reuseExistingServer`가 기존 :3000(Grafana)을 재사용 → Next를 :3100으로 띄우고 `BASE_URL`로 지정해 해결.
3. **Playwright 브라우저 미설치**: 1.55.1이 요구하는 chromium 빌드 1193 부재 → `npx playwright install chromium`으로 설치.
4. smoke.sh의 최종 python 단언은 `/tmp` 경로가 MSYS↔Windows Python 간 불일치라 직접 curl 단언으로 대체(백엔드 응답값은 정확).

## 잔여 / 메모
- **로컬 부팅 = E2E 검증용**이며 DEV/PROD 환경 배포(rollout)는 아니다. 배포 게이트(05_operate)는 본 작업 범위 밖.
- H2 in-memory라 transaction-service 재기동 시 거래원장은 비고, 재수집으로 복원된다(멱등).
- 외부 data.go.kr 실연동은 stub이 아닌 운영 프로필 + `MOLIT_SERVICE_KEY` 환경변수에서 별도 검증 대상.
