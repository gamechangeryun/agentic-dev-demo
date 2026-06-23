"use client";

/**
 * 데이터 수집 화면. lawdCd(5자리)·dealYmd(YYYYMM) → ingestAptTrade → upserted 건수 카드.
 * 같은 구간 재수집 시 멱등(0건)임을 보여준다.
 * E2E data-testid: ingest-lawdcd, ingest-dealymd, btn-ingest, ingest-result.
 */
import { useState } from "react";
import { toast } from "sonner";

import { ingestAptTrade } from "@/lib/api";
import type { IngestResult } from "@/lib/types";
import { IngestForm } from "@/components/ingest-form";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function IngestPage() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<IngestResult | null>(null);

  async function handleIngest(lawdCd: string, dealYmd: string) {
    if (!lawdCd || !dealYmd) {
      toast.error("지역코드(5자리)와 계약년월(YYYYMM)을 입력하세요.");
      return;
    }
    setLoading(true);
    try {
      const r = await ingestAptTrade(lawdCd, dealYmd);
      setResult(r);
      toast.success(`수집 완료 — ${r.upserted.toLocaleString("ko-KR")}건 적재`);
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "수집에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">데이터 수집</h1>
        <p className="text-sm text-muted-foreground">
          국토부 실거래가를 수집해 거래원장에 적재합니다. 같은 구간을 다시 수집해도
          멱등(중복 0)입니다.
        </p>
      </header>

      <Card>
        <CardHeader>
          <CardTitle>수집 트리거</CardTitle>
          <CardDescription>
            지역코드(LAWD_CD, 5자리)와 계약년월(DEAL_YMD, YYYYMM)을 입력하세요.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <IngestForm
            loading={loading}
            onIngest={handleIngest}
            defaultLawdCd="11110"
            defaultDealYmd="202405"
          />
        </CardContent>
      </Card>

      {result ? (
        <Card data-testid="ingest-result">
          <CardHeader>
            <CardTitle>수집 결과</CardTitle>
            <CardDescription>
              지역 {result.lawdCd} · 계약월 {result.dealYmd}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold tracking-tight">
              {result.upserted.toLocaleString("ko-KR")}건
            </p>
            <p className="mt-1 text-sm text-muted-foreground">
              새로 적재된 거래 수입니다. 재수집 시 멱등으로 0건이 됩니다.
            </p>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}
