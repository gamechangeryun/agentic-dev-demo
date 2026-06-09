"use client";

/**
 * 조회 폼 (거래·분석 공용) — 시작 상태(placeholder).
 *
 * 정답(complete/web)에서는 sggCd(5자리)·계약년·계약월 입력과 검증, 제출 버튼을 렌더하고
 * onSubmit({ sggCd, dealYear, dealMonth }) 를 호출합니다.
 *
 * 시그니처(props)는 그대로 둡니다. 구현부는 발화로 채웁니다.
 * E2E가 기대하는 data-testid: `${testPrefix}-sggcd`, `${testPrefix}-year`,
 * `${testPrefix}-month`, 그리고 제출 버튼의 `searchTestId`.
 */
import type { QueryParams } from "@/lib/types";

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

export function QueryForm(props: QueryFormProps) {
  // TODO: 발화로 구현 — 입력 폼과 검증, onSubmit 호출.
  return (
    <div className="rounded-xl border bg-card p-4 text-sm text-muted-foreground">
      TODO: 발화로 구현 — 조회 폼({props.submitLabel}).
    </div>
  );
}
