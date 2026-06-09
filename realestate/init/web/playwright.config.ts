import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright E2E 설정.
 *
 * - baseURL: http://localhost:3000 (Next 앱)
 * - chromium 단일 프로젝트
 * - webServer: 이미 :3000 이 떠 있으면 재사용(reuseExistingServer). 없으면 `npm run start`로 띄운다.
 *   (lab.sh web-e2e 는 이미 빌드·기동한 Next를 재사용하고, 단독 `npm run e2e`는 자동 기동한다.)
 * - HEADED 환경변수가 있으면 headed 모드로 실행.
 *
 * 백엔드 게이트웨이(:8080)는 docker compose 로 별도 기동되어 있어야 한다.
 * (web 프록시 route handler 가 GATEWAY_URL 로 포워딩한다.)
 */
const HEADED = !!process.env.HEADED;

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1,
  reporter: [["list"]],
  timeout: 60_000,
  expect: { timeout: 15_000 },
  use: {
    baseURL: process.env.BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    headless: !HEADED,
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: {
    command: "npm run start",
    url: process.env.BASE_URL ?? "http://localhost:3000",
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
