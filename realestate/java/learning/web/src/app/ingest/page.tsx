/**
 * 데이터 수집 화면 — 시작 상태(placeholder).
 *
 * 정답(complete/web)에서는 lawdCd(5자리)·dealYmd(YYYYMM) 입력 → ingestAptTrade 호출 →
 * upserted 건수 카드 노출 + 멱등(재수집 시 0건) 확인을 합니다.
 *
 * TODO: 발화로 구현하세요. lib/api.ts 의 ingestAptTrade 를 사용하고,
 *       E2E가 기대하는 data-testid 를 답니다:
 *       ingest-lawdcd, ingest-dealymd, btn-ingest, ingest-result.
 */
export default function IngestPage() {
  return (
    <div className="mx-auto max-w-2xl space-y-2 py-10">
      <h1 className="text-2xl font-bold tracking-tight">데이터 수집</h1>
      <p className="text-sm text-muted-foreground">
        TODO: 발화로 구현 — 수집 폼과 결과 카드를 만드세요.
      </p>
    </div>
  );
}
