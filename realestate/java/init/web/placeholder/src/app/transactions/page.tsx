/**
 * 거래 조회 화면 — 시작 상태(placeholder).
 *
 * 정답(complete/web)에서는 QueryForm(sggCd·년·월) → getTransactions 조회 →
 * 동/단지 검색·면적 필터·해제거래 토글·정렬·페이징 테이블을 렌더합니다.
 *
 * TODO: 발화로 구현하세요. lib/api.ts 의 getTransactions, components/query-form 의
 *       QueryForm 을 사용하고, E2E가 기대하는 data-testid 를 답니다:
 *       tx-sggcd, tx-year, tx-month, btn-search, transactions-table,
 *       tx-row, cancel-badge, filter-canceled.
 */
export default function TransactionsPage() {
  return (
    <div className="mx-auto max-w-2xl space-y-2 py-10">
      <h1 className="text-2xl font-bold tracking-tight">거래 조회</h1>
      <p className="text-sm text-muted-foreground">
        TODO: 발화로 구현 — 조회 폼과 거래 원장 테이블을 만드세요.
      </p>
    </div>
  );
}
