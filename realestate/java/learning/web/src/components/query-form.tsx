"use client";

/**
 * 조회 폼 (거래·분석 공용).
 *
 * sggCd(5자리)·계약년·계약월 입력을 받아 onSubmit({ sggCd, dealYear, dealMonth })을 호출한다.
 * E2E data-testid: `${testPrefix}-sggcd`, `${testPrefix}-year`, `${testPrefix}-month`, 제출 버튼 `searchTestId`.
 */
import { useState } from "react";
import { Search } from "lucide-react";

import type { QueryParams } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface QueryFormProps {
  /** data-testid 접두어 ("tx" 또는 "an") */
  testPrefix: "tx" | "an";
  /** 조회 버튼 testid */
  searchTestId: string;
  /** 조회 버튼 라벨 */
  submitLabel: string;
  loading: boolean;
  onSubmit: (params: QueryParams) => void;
  defaultValues?: Partial<QueryParams>;
}

export function QueryForm({
  testPrefix,
  searchTestId,
  submitLabel,
  loading,
  onSubmit,
  defaultValues,
}: QueryFormProps) {
  const [sggCd, setSggCd] = useState(defaultValues?.sggCd ?? "");
  const [dealYear, setDealYear] = useState(
    defaultValues?.dealYear ? String(defaultValues.dealYear) : "",
  );
  const [dealMonth, setDealMonth] = useState(
    defaultValues?.dealMonth ? String(defaultValues.dealMonth) : "",
  );

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit({
      sggCd: sggCd.trim(),
      dealYear: Number(dealYear),
      dealMonth: Number(dealMonth),
    });
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="grid grid-cols-1 gap-3 rounded-xl border bg-card p-4 sm:grid-cols-4 sm:items-end"
    >
      <div className="space-y-1.5">
        <Label htmlFor={`${testPrefix}-sggcd`}>시군구코드</Label>
        <Input
          id={`${testPrefix}-sggcd`}
          data-testid={`${testPrefix}-sggcd`}
          inputMode="numeric"
          placeholder="11110"
          maxLength={5}
          value={sggCd}
          onChange={(e) => setSggCd(e.target.value)}
        />
      </div>
      <div className="space-y-1.5">
        <Label htmlFor={`${testPrefix}-year`}>계약년</Label>
        <Input
          id={`${testPrefix}-year`}
          data-testid={`${testPrefix}-year`}
          inputMode="numeric"
          placeholder="2024"
          maxLength={4}
          value={dealYear}
          onChange={(e) => setDealYear(e.target.value)}
        />
      </div>
      <div className="space-y-1.5">
        <Label htmlFor={`${testPrefix}-month`}>계약월</Label>
        <Input
          id={`${testPrefix}-month`}
          data-testid={`${testPrefix}-month`}
          inputMode="numeric"
          placeholder="5"
          maxLength={2}
          value={dealMonth}
          onChange={(e) => setDealMonth(e.target.value)}
        />
      </div>
      <Button
        type="submit"
        data-testid={searchTestId}
        disabled={loading}
        className="w-full"
      >
        <Search className="h-4 w-4" />
        {loading ? "조회 중…" : submitLabel}
      </Button>
    </form>
  );
}
