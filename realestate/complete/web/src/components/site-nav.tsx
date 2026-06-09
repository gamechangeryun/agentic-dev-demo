"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Building2, Download, Table2, BarChart3 } from "lucide-react";

import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  {
    href: "/ingest",
    label: "수집",
    testId: "nav-ingest",
    icon: Download,
  },
  {
    href: "/transactions",
    label: "거래",
    testId: "nav-transactions",
    icon: Table2,
  },
  {
    href: "/analytics",
    label: "분석",
    testId: "nav-analytics",
    icon: BarChart3,
  },
] as const;

export function SiteNav() {
  const pathname = usePathname();

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border/60 bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center justify-between">
        <Link href="/" className="flex items-center gap-2 font-semibold">
          <span className="flex h-7 w-7 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <Building2 className="h-4 w-4" />
          </span>
          <span className="hidden sm:inline">RealField</span>
          <span className="hidden text-xs font-normal text-muted-foreground md:inline">
            부동산 실거래 분석
          </span>
        </Link>
        <nav className="flex items-center gap-1">
          {NAV_ITEMS.map((item) => {
            const active =
              pathname === item.href || pathname.startsWith(`${item.href}/`);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                data-testid={item.testId}
                aria-current={active ? "page" : undefined}
                className={cn(
                  "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                  active
                    ? "bg-primary/10 text-primary"
                    : "text-muted-foreground hover:bg-accent hover:text-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </div>
    </header>
  );
}
