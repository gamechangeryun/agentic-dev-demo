/**
 * 홈(랜딩). 3개 화면(수집·거래·분석)으로 안내한다. 네비게이션은 상단 SiteNav가 담당한다.
 */
import Link from "next/link";
import { ArrowRight, BarChart3, Download, Table2 } from "lucide-react";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const FEATURES = [
  {
    href: "/ingest",
    title: "데이터 수집",
    description:
      "국토부 실거래가를 시군구·계약월 단위로 수집해 거래원장에 적재합니다. 재수집해도 멱등(중복 0)입니다.",
    icon: Download,
  },
  {
    href: "/transactions",
    title: "거래 조회",
    description:
      "조건으로 실거래를 조회하고, 해제거래는 배지로 표시·토글로 제외합니다.",
    icon: Table2,
  },
  {
    href: "/analytics",
    title: "시세 분석",
    description:
      "해제거래를 제외한 중위 시세와 전용면적 대비 거래금액 분포를 봅니다.",
    icon: BarChart3,
  },
] as const;

export default function HomePage() {
  return (
    <div className="space-y-8">
      <section className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">
          RealField · 부동산 실거래 분석
        </h1>
        <p className="max-w-2xl text-muted-foreground">
          MSA 게이트웨이(8080) 위에서 수집 → 거래조회 → 시세분석을 한 화면 흐름으로
          연결한 데모입니다. 모든 호출은 게이트웨이 프록시를 통해 단일 진입점으로 나갑니다.
        </p>
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        {FEATURES.map((f) => {
          const Icon = f.icon;
          return (
            <Link key={f.href} href={f.href} className="group">
              <Card className="h-full transition-colors group-hover:border-primary/50">
                <CardHeader>
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                    <Icon className="h-5 w-5" />
                  </div>
                  <CardTitle className="flex items-center justify-between">
                    {f.title}
                    <ArrowRight className="h-4 w-4 text-muted-foreground transition-transform group-hover:translate-x-0.5" />
                  </CardTitle>
                  <CardDescription>{f.description}</CardDescription>
                </CardHeader>
                <CardContent />
              </Card>
            </Link>
          );
        })}
      </section>
    </div>
  );
}
