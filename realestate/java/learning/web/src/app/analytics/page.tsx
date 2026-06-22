/**
 * 시세 분석 화면 — 시작 상태(placeholder).
 *
 * 정답(complete/web)에서는 QueryForm(sggCd·년·월) → getMarketStats + getTransactions →
 * 유효 거래건수·중위 거래금액·㎡당 중위단가 카드 + 면적-가격 산점도를 그립니다.
 *
 * TODO: 발화로 구현하세요. lib/api.ts 의 getMarketStats/getTransactions,
 *       components/query-form 의 QueryForm 을 사용하고, E2E가 기대하는 data-testid 를 답니다:
 *       an-sggcd, an-year, an-month, btn-analyze,
 *       stat-tradecount, stat-median, stat-perm2, price-chart.
 */
export default function AnalyticsPage() {
  return (
    <div className="mx-auto max-w-2xl space-y-2 py-10">
      <h1 className="text-2xl font-bold tracking-tight">시세 분석</h1>
      <p className="text-sm text-muted-foreground">
        TODO: 발화로 구현 — 시세 통계 카드와 산점도를 만드세요.
      </p>
    </div>
  );
}
