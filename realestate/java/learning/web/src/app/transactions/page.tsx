"use client";

/**
 * 거래 조회 화면. QueryForm(tx)으로 조회 → TransactionTable 렌더(해제 토글 포함).
 * E2E data-testid: tx-sggcd, tx-year, tx-month, btn-search, transactions-table, tx-row, cancel-badge, filter-canceled.
 */
import { useState } from "react";
import { toast } from "sonner";

import { getTransactions } from "@/lib/api";
import type { AptTransaction, QueryParams } from "@/lib/types";
import { QueryForm } from "@/components/query-form";
import { TransactionTable } from "@/components/transaction-table";

export default function TransactionsPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<AptTransaction[] | null>(null);

  async function handleSearch(params: QueryParams) {
    setLoading(true);
    try {
      const rows = await getTransactions(params);
      setData(rows);
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "조회에 실패했습니다.");
      setData([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">거래 조회</h1>
        <p className="text-sm text-muted-foreground">
          시군구·계약년월로 실거래를 조회합니다. 해제거래는 배지로 표시되고 토글로
          제외할 수 있습니다.
        </p>
      </header>

      <QueryForm
        testPrefix="tx"
        searchTestId="btn-search"
        submitLabel="조회"
        loading={loading}
        onSubmit={handleSearch}
        defaultValues={{ sggCd: "11110", dealYear: 2024, dealMonth: 5 }}
      />

      {data ? (
        <TransactionTable transactions={data} />
      ) : (
        <p className="text-sm text-muted-foreground">
          조건을 입력하고 조회를 누르세요.
        </p>
      )}
    </div>
  );
}
