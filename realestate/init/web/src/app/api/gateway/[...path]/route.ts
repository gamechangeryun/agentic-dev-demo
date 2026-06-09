/**
 * 게이트웨이 프록시 route handler.
 *
 * 클라이언트는 8080을 직접 호출하지 않고 이 핸들러(/api/gateway/...)를 부릅니다.
 * 서버측에서 GATEWAY_URL(기본 http://localhost:8080)로 포워딩하므로 브라우저 CORS가 발생하지 않습니다.
 *
 * 예) /api/gateway/ingest/apt-trade?lawdCd=11110&dealYmd=202405
 *   → ${GATEWAY_URL}/api/v1/ingest/apt-trade?lawdCd=11110&dealYmd=202405
 */
import { NextRequest, NextResponse } from "next/server";

export const dynamic = "force-dynamic";

const GATEWAY_URL = process.env.GATEWAY_URL ?? "http://localhost:8080";

async function forward(req: NextRequest, path: string[]): Promise<NextResponse> {
  const subPath = path.join("/");
  const search = req.nextUrl.search; // ?a=b&c=d (그대로 전달)
  const target = `${GATEWAY_URL}/api/v1/${subPath}${search}`;

  const init: RequestInit = {
    method: req.method,
    headers: {
      Accept: "application/json",
    },
    cache: "no-store",
  };

  // 본문이 있는 메서드는 그대로 전달합니다.
  if (req.method !== "GET" && req.method !== "HEAD") {
    const body = await req.text();
    if (body) {
      init.body = body;
      (init.headers as Record<string, string>)["Content-Type"] =
        req.headers.get("content-type") ?? "application/json";
    }
  }

  try {
    const upstream = await fetch(target, init);
    const text = await upstream.text();
    return new NextResponse(text, {
      status: upstream.status,
      headers: {
        "Content-Type":
          upstream.headers.get("content-type") ?? "application/json",
      },
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : String(err);
    return NextResponse.json(
      {
        error: "게이트웨이에 연결할 수 없습니다.",
        target,
        detail: message,
      },
      { status: 502 },
    );
  }
}

type RouteContext = { params: Promise<{ path: string[] }> };

export async function GET(req: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(req, path);
}

export async function POST(req: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(req, path);
}

export async function PUT(req: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(req, path);
}

export async function DELETE(req: NextRequest, ctx: RouteContext) {
  const { path } = await ctx.params;
  return forward(req, path);
}
