# 프론트(웹) 구현 현황 (03_build · 02_screen)

> current-state 문서. Next.js 프론트의 현재 화면·계약·검증 상태를 설명한다(실행 서술·날짜 없음).
> 담당 @frontend-dev. 게이트웨이(8080) 단일 진입점 위에서 수집 → 거래조회 → 시세분석 3화면을 제공한다.

## 구현 범위 (현재 상태)
Next.js 15(App Router) + shadcn/ui로 3개 기능 화면과 홈을 구현했다. 모든 백엔드 호출은 8080을 직접 부르지 않고 Next route handler 프록시(`/api/gateway/...`)를 경유한다(CORS 회피). 타입은 `lib/types.ts` 백엔드 계약을 1:1로 따른다.

## 화면·컴포넌트
| 파일 | 책임 | 주요 testid |
| --- | --- | --- |
| `app/page.tsx` | 홈(랜딩) — 3화면 안내 | (nav는 SiteNav) |
| `app/ingest/page.tsx` | 수집 트리거 + 결과 카드(멱등 0건 안내) | `ingest-result` |
| `app/transactions/page.tsx` | 거래 조회 흐름 | — |
| `app/analytics/page.tsx` | 시세 카드 + 거래 분포 산점도 | `stat-tradecount`·`stat-median`·`price-chart` |
| `components/ingest-form.tsx` | lawdCd·dealYmd 입력 | `ingest-lawdcd`·`ingest-dealymd`·`btn-ingest` |
| `components/query-form.tsx` | sggCd·year·month 공용 폼(tx/an 프리픽스) | `tx/an-sggcd·-year·-month`·`btn-search/analyze` |
| `components/transaction-table.tsx` | 거래 표 + 해제 포함/제외 토글 | `transactions-table`·`tx-row`·`cancel-badge`·`filter-canceled` |
| `components/market-card.tsx` | 시세 지표 카드 | (value testid 주입) |
| `lib/api.ts` | 프록시 경유 API 래퍼(구현) | — |

> 보존된 스캐폴딩(layout·site-nav·theme·ui/shadcn·lib/types·lib/format·lib/utils·프록시 route)은 변경하지 않았다.

## 백엔드 계약 준수 (lib/types.ts)
- `ingestAptTrade(lawdCd, dealYmd)` → `POST /api/gateway/ingest/apt-trade?lawdCd=&dealYmd=` → `IngestResult{ lawdCd, dealYmd, upserted }`. 수집은 `lawdCd` 파라미터명.
- `getTransactions({sggCd, dealYear, dealMonth})` → `GET /api/gateway/transactions?...` → `AptTransaction[]`. 조회는 `sggCd` 파라미터명.
- `getMarketStats(...)` → `GET /api/gateway/market-stats?...` → `MarketStat{ tradeCount, medianPriceWon, medianPricePerM2Won }`.
- 프록시(`app/api/gateway/[...path]/route.ts`)가 `${GATEWAY_URL:-http://localhost:8080}/api/v1/<path>`로 포워딩.

## 현재 동작
- **수집**: lawdCd(5자리)·dealYmd(YYYYMM) 제출 → 결과 카드에 `upserted`건 노출(재수집 멱등 0건 안내).
- **거래 조회**: sggCd·연·월 조회 → 표 렌더. 해제거래는 `해제` 배지 표시, `해제거래 포함` 토글로 제외(행 수 감소).
- **시세 분석**: 시세 카드(유효 거래수·중위 거래금액·중위 ㎡당 단가) + 전용면적 대비 거래금액 산점도(해제 제외, recharts).
- 금액·면적·날짜 표기는 `lib/format`(formatWon "8억 5,000만원" 등)을 사용.

## 검증 (proof)
- `./lab.sh web-install` → `npm ci` 227패키지 결정적 설치.
- `./lab.sh web-build` → `next build` **BUILD OK**:
  - `✓ Compiled successfully`,
  - `✓ Linting and checking validity of types`(TypeScript strict 타입체크 통과),
  - `✓ Generating static pages (7/7)` — `/`·`/ingest`·`/transactions`·`/analytics`·`/api/gateway/[...path]`.

## 회귀 범위
- 직접: `web/src` 구현 6파일(page×4·query-form·api) + 신규 컴포넌트(ingest-form·transaction-table·market-card).
- 계약 의존: `lib/types.ts`(백엔드 계약). 백엔드 응답 형태가 바뀌면 이 타입과 함께 회귀.
- 제외(정당화): 백엔드 도메인/인프라(별도 담당), 보존된 스캐폴딩.

## 잔여 위험 / 메모
- **브라우저 E2E(`./lab.sh web-e2e`) 미실행**: docker compose 6서비스 + Next 기동 + Playwright가 필요해 본 게이트(web-build) 범위 밖. 화면은 e2e 스펙의 data-testid·동작 계약에 맞춰 구현했으나, 실런타임 통과는 백엔드 stub 기동 후 확인 대상.
- **`medianPricePerM2Won`**: 프론트 계약 타입에는 있으나 analytics-service read model이 아직 산출하지 않는다(03_build analytics 잔여). 백엔드 미제공 시 카드 값은 `-`로 표기된다.
- **HTTP 진입 미배선(백엔드)**: 각 도메인 서비스의 컨트롤러가 아직 없어, 실제 데이터 흐름은 백엔드 잔여 증분 + stub/E2E 기동에 의존한다.
