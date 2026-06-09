/**
 * 홈 — 시작 상태(placeholder).
 *
 * 정답(complete/web)에서는 3화면(수집·거래·분석) 진입 카드와 사용 순서를 안내합니다.
 * TODO: 발화로 구현하세요. (홈은 정적 화면이라 비워두거나 그대로 둬도 됩니다.)
 */
export default function HomePage() {
  return (
    <div className="mx-auto max-w-2xl space-y-3 py-10">
      <h1 className="text-2xl font-bold tracking-tight">
        RealField — 부동산 실거래 분석
      </h1>
      <p className="text-sm text-muted-foreground">
        시작 상태입니다. 상단 내비게이션(수집 · 거래 · 분석)의 각 화면을 발화로
        구현하세요. 구현 전에는 화면 동작이 없어 브라우저 E2E가 실패하는 것이
        정상입니다.
      </p>
    </div>
  );
}
