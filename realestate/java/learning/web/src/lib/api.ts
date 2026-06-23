/**
 * 클라이언트 API 래퍼.
 *
 * 게이트웨이(8080)를 브라우저에서 직접 부르지 않고, Next route handler 프록시
 * (`/api/gateway/...`)를 통해 호출한다(CORS 회피). 프록시가 `${GATEWAY}/api/v1/...`로 포워딩한다.
 * 수집은 `lawdCd`, 조회는 `sggCd` 파라미터명을 쓴다(백엔드 계약).
 */
import type {
  AptTransaction,
  IngestResult,
  MarketStat,
  QueryParams,
} from "./types";

const GATEWAY = "/api/gateway";

/** 응답을 검증해 JSON으로 푼다. 실패 시 프록시가 준 에러 메시지를 그대로 노출한다. */
async function unwrap<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let detail = "";
    try {
      const body = (await res.json()) as { error?: string; detail?: string };
      detail = body.error ?? body.detail ?? "";
    } catch {
      // JSON 본문이 아니면 상태코드만으로 보고
    }
    throw new Error(
      `요청 실패 (HTTP ${res.status})${detail ? `: ${detail}` : ""}`,
    );
  }
  return (await res.json()) as T;
}

function queryString(params: QueryParams): string {
  return new URLSearchParams({
    sggCd: params.sggCd,
    dealYear: String(params.dealYear),
    dealMonth: String(params.dealMonth),
  }).toString();
}

/**
 * 실거래 수집 트리거. 수집은 `lawdCd` 파라미터명을 쓴다.
 * `POST /api/gateway/ingest/apt-trade?lawdCd=...&dealYmd=...` → IngestResult(재수집 시 멱등 upserted=0).
 */
export async function ingestAptTrade(
  lawdCd: string,
  dealYmd: string,
): Promise<IngestResult> {
  const qs = new URLSearchParams({ lawdCd, dealYmd }).toString();
  const res = await fetch(`${GATEWAY}/ingest/apt-trade?${qs}`, {
    method: "POST",
    headers: { Accept: "application/json" },
  });
  return unwrap<IngestResult>(res);
}

/**
 * 거래원장 조회. 조회는 `sggCd` 파라미터명을 쓴다.
 * `GET /api/gateway/transactions?sggCd=...&dealYear=...&dealMonth=...` → AptTransaction[].
 */
export async function getTransactions(
  params: QueryParams,
): Promise<AptTransaction[]> {
  const res = await fetch(`${GATEWAY}/transactions?${queryString(params)}`, {
    headers: { Accept: "application/json" },
  });
  return unwrap<AptTransaction[]>(res);
}

/**
 * 시세 통계 조회(해제 제외 read model).
 * `GET /api/gateway/market-stats?sggCd=...&dealYear=...&dealMonth=...` → MarketStat.
 */
export async function getMarketStats(
  params: QueryParams,
): Promise<MarketStat> {
  const res = await fetch(`${GATEWAY}/market-stats?${queryString(params)}`, {
    headers: { Accept: "application/json" },
  });
  return unwrap<MarketStat>(res);
}
