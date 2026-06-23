"use client";

/**
 * 시세 분석 화면. getMarketStats(카드) + getTransactions(산점도) 동시 조회.
 * 해제거래는 산점도에서 제외(read model과 동일 기준).
 * E2E data-testid: an-sggcd, an-year, an-month, btn-analyze, stat-tradecount, stat-median, price-chart.
 */
import { useState } from "react";
import { toast } from "sonner";
import { Coins, ListChecks, Ruler } from "lucide-react";
import {
  CartesianGrid,
  ResponsiveContainer,
  Scatter,
  ScatterChart,
  Tooltip,
  XAxis,
  YAxis,
  ZAxis,
} from "recharts";

import { getMarketStats, getTransactions } from "@/lib/api";
import type { AptTransaction, MarketStat, QueryParams } from "@/lib/types";
import { QueryForm } from "@/components/query-form";
import { MarketCard } from "@/components/market-card";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { formatPerM2, formatWon, formatWonShort } from "@/lib/format";

interface ChartPoint {
  area: number;
  price: number;
  name: string;
}

export default function AnalyticsPage() {
  const [loading, setLoading] = useState(false);
  const [stat, setStat] = useState<MarketStat | null>(null);
  const [points, setPoints] = useState<ChartPoint[]>([]);

  async function handleAnalyze(params: QueryParams) {
    setLoading(true);
    try {
      const [s, txs] = await Promise.all([
        getMarketStats(params),
        getTransactions(params).catch(() => [] as AptTransaction[]),
      ]);
      setStat(s);
      setPoints(
        txs
          .filter((t) => !t.canceled)
          .map((t) => ({
            area: t.exclusiveArea,
            price: t.dealAmountWon,
            name: t.aptNm,
          })),
      );
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "분석에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">시세 분석</h1>
        <p className="text-sm text-muted-foreground">
          해제거래를 제외한 중위 시세와 전용면적 대비 거래금액 분포를 봅니다.
        </p>
      </header>

      <QueryForm
        testPrefix="an"
        searchTestId="btn-analyze"
        submitLabel="분석"
        loading={loading}
        onSubmit={handleAnalyze}
        defaultValues={{ sggCd: "11110", dealYear: 2024, dealMonth: 5 }}
      />

      {stat ? (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <MarketCard
              label="유효 거래건수"
              icon={<ListChecks className="h-4 w-4" />}
              value={`${stat.tradeCount.toLocaleString("ko-KR")}건`}
              valueTestId="stat-tradecount"
              hint="해제거래 제외"
            />
            <MarketCard
              label="중위 거래금액"
              icon={<Coins className="h-4 w-4" />}
              value={formatWon(stat.medianPriceWon)}
              valueTestId="stat-median"
              hint="중앙값(원)"
            />
            <MarketCard
              label="중위 ㎡당 단가"
              icon={<Ruler className="h-4 w-4" />}
              value={formatPerM2(stat.medianPricePerM2Won)}
              valueTestId="stat-perm2"
              hint="㎡당 중앙값"
            />
          </div>

          <Card>
            <CardHeader>
              <CardTitle>거래 분포</CardTitle>
              <CardDescription>
                전용면적(㎡) 대비 거래금액(원) 산점도 · 해제거래 제외
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div data-testid="price-chart" className="h-80 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <ScatterChart
                    margin={{ top: 8, right: 16, bottom: 24, left: 8 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                    <XAxis
                      type="number"
                      dataKey="area"
                      name="전용면적"
                      unit="㎡"
                      tick={{ fontSize: 12 }}
                    />
                    <YAxis
                      type="number"
                      dataKey="price"
                      name="거래금액"
                      width={56}
                      tick={{ fontSize: 12 }}
                      tickFormatter={(v: number) => formatWonShort(v)}
                    />
                    <ZAxis range={[64, 64]} />
                    <Tooltip cursor={{ strokeDasharray: "3 3" }} />
                    <Scatter
                      data={points}
                      fill="hsl(var(--primary))"
                      name="거래"
                    />
                  </ScatterChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        </>
      ) : (
        <p className="text-sm text-muted-foreground">
          조건을 입력하고 분석을 누르세요.
        </p>
      )}
    </div>
  );
}
