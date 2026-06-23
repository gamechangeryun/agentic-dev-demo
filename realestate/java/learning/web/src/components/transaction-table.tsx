"use client";

/**
 * 거래 테이블. 조회 결과를 표로 렌더하고, 해제거래 포함/제외 토글을 제공한다.
 * E2E data-testid: transactions-table, tx-row(행), cancel-badge(해제), filter-canceled(토글).
 */
import { useState } from "react";

import type { AptTransaction } from "@/lib/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { formatWon, formatAreaShort, formatDealDate } from "@/lib/format";

interface TransactionTableProps {
  transactions: AptTransaction[];
}

export function TransactionTable({ transactions }: TransactionTableProps) {
  const [includeCanceled, setIncludeCanceled] = useState(true);

  const rows = includeCanceled
    ? transactions
    : transactions.filter((t) => !t.canceled);

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <p className="text-sm text-muted-foreground">
          {rows.length.toLocaleString("ko-KR")}건
        </p>
        <div className="flex items-center gap-2">
          <Switch
            id="filter-canceled"
            data-testid="filter-canceled"
            checked={includeCanceled}
            onCheckedChange={setIncludeCanceled}
          />
          <Label htmlFor="filter-canceled" className="text-sm font-normal">
            해제거래 포함
          </Label>
        </div>
      </div>

      <div className="rounded-xl border">
        <Table data-testid="transactions-table">
          <TableHeader>
            <TableRow>
              <TableHead>단지명</TableHead>
              <TableHead>법정동</TableHead>
              <TableHead className="text-right">전용면적</TableHead>
              <TableHead className="text-right">층</TableHead>
              <TableHead>계약일</TableHead>
              <TableHead className="text-right">거래금액</TableHead>
              <TableHead>상태</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={7}
                  className="h-24 text-center text-muted-foreground"
                >
                  조회 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              rows.map((t, i) => (
                <TableRow
                  key={`${t.aptNm}-${t.floor}-${t.dealDay}-${t.dealAmountWon}-${i}`}
                  data-testid="tx-row"
                >
                  <TableCell className="font-medium">{t.aptNm}</TableCell>
                  <TableCell>{t.umdNm}</TableCell>
                  <TableCell className="text-right">
                    {formatAreaShort(t.exclusiveArea)}
                  </TableCell>
                  <TableCell className="text-right">{t.floor}층</TableCell>
                  <TableCell>
                    {formatDealDate(t.dealYear, t.dealMonth, t.dealDay)}
                  </TableCell>
                  <TableCell className="text-right font-semibold">
                    {formatWon(t.dealAmountWon)}
                  </TableCell>
                  <TableCell>
                    {t.canceled ? (
                      <Badge variant="destructive" data-testid="cancel-badge">
                        해제
                      </Badge>
                    ) : (
                      <Badge variant="secondary">정상</Badge>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
