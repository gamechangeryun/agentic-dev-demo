/**
 * 클라이언트 API 래퍼 — 시작 상태(placeholder).
 *
 * 시그니처(계약)는 그대로 둡니다. 구현부는 발화로 채웁니다.
 * 게이트웨이(8080)를 직접 부르지 않고 Next route handler 프록시(/api/gateway/...)를
 * 통해 호출해야 합니다(CORS 회피). 엔드포인트·파라미터명은 아래 TODO 참고.
 *
 * 비어 있는 상태에서도 타입은 맞으므로 `next build`(타입체크)는 통과합니다.
 * 다만 실제 동작이 없어 브라우저 E2E(./lab.sh web-e2e)는 실패하는 것이 정상입니다.
 */
import type {
  AptTransaction,
  IngestResult,
  MarketStat,
  QueryParams,
} from "./types";

/**
 * 실거래 수집 트리거. 수집은 lawdCd 파라미터명을 씁니다.
 * TODO: POST /api/gateway/ingest/apt-trade?lawdCd=...&dealYmd=... 를 호출하고
 *       IngestResult(upserted 포함)를 반환하도록 구현하세요. (재수집 시 멱등으로 0)
 */
export async function ingestAptTrade(
  lawdCd: string,
  dealYmd: string,
): Promise<IngestResult> {
  // TODO: 발화로 구현
  throw new Error("ingestAptTrade 미구현 — 발화로 구현하세요.");
}

/**
 * 거래원장 조회. 조회는 sggCd 파라미터명을 씁니다.
 * TODO: GET /api/gateway/transactions?sggCd=...&dealYear=...&dealMonth=...
 */
export async function getTransactions(
  params: QueryParams,
): Promise<AptTransaction[]> {
  // TODO: 발화로 구현
  throw new Error("getTransactions 미구현 — 발화로 구현하세요.");
}

/**
 * 시세 통계 조회. 조회는 sggCd 파라미터명을 씁니다.
 * TODO: GET /api/gateway/market-stats?sggCd=...&dealYear=...&dealMonth=...
 */
export async function getMarketStats(
  params: QueryParams,
): Promise<MarketStat> {
  // TODO: 발화로 구현
  throw new Error("getMarketStats 미구현 — 발화로 구현하세요.");
}
