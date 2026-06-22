import { test, expect } from "@playwright/test";

/**
 * RealField 브라우저 E2E (결정적).
 *
 * 백엔드는 stub 프로필로 기동되어 있으므로 모든 값이 고정이다:
 *   - 종로구 11110 / 2024년 5월(202405)
 *   - transactions GET: 5행 (정상 4 + 해제 1). cancel-badge 1개.
 *   - market-stats: tradeCount=4 (해제 제외), medianPriceWon=850,000,000 → "8억 5,000만원"
 *
 * data-testid 만으로 조작하며 절대시각·랜덤 단언은 쓰지 않는다.
 */

const SGG = "11110";
const YMD = "202405";
const YEAR = "2024";
const MONTH = "5";

test.describe("RealField 실거래 분석 3화면", () => {
  test("수집 → 거래조회 → 시세분석 전 흐름이 결정적으로 통과한다", async ({
    page,
  }) => {
    // ── 1) 홈 → 수집 ──────────────────────────────────────────────
    await page.goto("/");
    await page.getByTestId("nav-ingest").click();
    await expect(page).toHaveURL(/\/ingest$/);

    const lawdInput = page.getByTestId("ingest-lawdcd");
    await lawdInput.fill("");
    await lawdInput.fill(SGG);
    const ymdInput = page.getByTestId("ingest-dealymd");
    await ymdInput.fill("");
    await ymdInput.fill(YMD);

    await page.getByTestId("btn-ingest").click();

    // 수집 결과 카드에 upserted 숫자가 노출될 때까지 대기
    const ingestResult = page.getByTestId("ingest-result");
    await expect(ingestResult).toBeVisible();
    await expect(ingestResult).toContainText(/\d+건/);

    // ── 2) 거래 조회 ──────────────────────────────────────────────
    await page.getByTestId("nav-transactions").click();
    await expect(page).toHaveURL(/\/transactions$/);

    await page.getByTestId("tx-sggcd").fill(SGG);
    await page.getByTestId("tx-year").fill(YEAR);
    await page.getByTestId("tx-month").fill(MONTH);
    await page.getByTestId("btn-search").click();

    // 테이블 렌더 + 행 확인. stub: 정상 4 + 해제 1 = 5행.
    const table = page.getByTestId("transactions-table");
    await expect(table).toBeVisible();
    const rows = page.getByTestId("tx-row");
    await expect(rows).toHaveCount(5);

    // 해제 배지 존재 (1건)
    const cancelBadges = page.getByTestId("cancel-badge");
    await expect(cancelBadges).toHaveCount(1);

    // filter-canceled 토글 해제 → 해제거래 제외 → 행 수 4로 감소
    await page.getByTestId("filter-canceled").click();
    await expect(rows).toHaveCount(4);
    await expect(page.getByTestId("cancel-badge")).toHaveCount(0);

    // ── 3) 시세 분석 ──────────────────────────────────────────────
    await page.getByTestId("nav-analytics").click();
    await expect(page).toHaveURL(/\/analytics$/);

    await page.getByTestId("an-sggcd").fill(SGG);
    await page.getByTestId("an-year").fill(YEAR);
    await page.getByTestId("an-month").fill(MONTH);
    await page.getByTestId("btn-analyze").click();

    // 유효 거래건수 = 4 (해제 제외)
    const tradeCount = page.getByTestId("stat-tradecount");
    await expect(tradeCount).toBeVisible();
    await expect(tradeCount).toContainText("4");

    // 중위 거래금액 = 850,000,000원 → formatWon: "8억 5,000만원"
    const median = page.getByTestId("stat-median");
    await expect(median).toBeVisible();
    await expect(median).toContainText("8억");

    // 산점도 렌더
    await expect(page.getByTestId("price-chart")).toBeVisible();
  });
});
