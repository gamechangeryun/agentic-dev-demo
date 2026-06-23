"use client";

/**
 * 수집 폼. 지역코드(LAWD_CD, 5자리)·계약년월(DEAL_YMD, YYYYMM)을 받아 onIngest를 호출한다.
 * E2E data-testid: ingest-lawdcd, ingest-dealymd, btn-ingest.
 */
import { useState } from "react";
import { Download } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface IngestFormProps {
  loading: boolean;
  onIngest: (lawdCd: string, dealYmd: string) => void;
  defaultLawdCd?: string;
  defaultDealYmd?: string;
}

export function IngestForm({
  loading,
  onIngest,
  defaultLawdCd = "",
  defaultDealYmd = "",
}: IngestFormProps) {
  const [lawdCd, setLawdCd] = useState(defaultLawdCd);
  const [dealYmd, setDealYmd] = useState(defaultDealYmd);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onIngest(lawdCd.trim(), dealYmd.trim());
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="grid grid-cols-1 gap-3 sm:grid-cols-3 sm:items-end"
    >
      <div className="space-y-1.5">
        <Label htmlFor="ingest-lawdcd">지역코드 (5자리)</Label>
        <Input
          id="ingest-lawdcd"
          data-testid="ingest-lawdcd"
          inputMode="numeric"
          placeholder="11110"
          maxLength={5}
          value={lawdCd}
          onChange={(e) => setLawdCd(e.target.value)}
        />
      </div>
      <div className="space-y-1.5">
        <Label htmlFor="ingest-dealymd">계약년월 (YYYYMM)</Label>
        <Input
          id="ingest-dealymd"
          data-testid="ingest-dealymd"
          inputMode="numeric"
          placeholder="202405"
          maxLength={6}
          value={dealYmd}
          onChange={(e) => setDealYmd(e.target.value)}
        />
      </div>
      <Button
        type="submit"
        data-testid="btn-ingest"
        disabled={loading}
        className="w-full"
      >
        <Download className="h-4 w-4" />
        {loading ? "수집 중…" : "수집 실행"}
      </Button>
    </form>
  );
}
