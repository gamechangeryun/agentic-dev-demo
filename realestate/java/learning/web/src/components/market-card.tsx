/**
 * 시세 통계 카드. 라벨 + 큰 값(중요 지표)을 보여준다.
 * value에 data-testid를 달아 E2E가 값을 직접 읽도록 한다(예: stat-tradecount, stat-median).
 */
import type { ReactNode } from "react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface MarketCardProps {
  label: string;
  value: ReactNode;
  valueTestId?: string;
  hint?: string;
  icon?: ReactNode;
}

export function MarketCard({
  label,
  value,
  valueTestId,
  hint,
  icon,
}: MarketCardProps) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
          {icon}
          {label}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div
          data-testid={valueTestId}
          className="text-2xl font-bold tracking-tight"
        >
          {value}
        </div>
        {hint ? (
          <p className="mt-1 text-xs text-muted-foreground">{hint}</p>
        ) : null}
      </CardContent>
    </Card>
  );
}
