"use client";

import * as React from "react";
import * as RechartsPrimitive from "recharts";

import { cn } from "@/lib/utils";

// shadcn/ui chart wrapper (간소화 버전). recharts ResponsiveContainer를 감싸고
// CSS 변수 기반 색상 토큰을 주입합니다.

export type ChartConfig = {
  [k in string]: {
    label?: React.ReactNode;
    icon?: React.ComponentType;
    color?: string;
  };
};

type ChartContextProps = {
  config: ChartConfig;
};

const ChartContext = React.createContext<ChartContextProps | null>(null);

function useChart() {
  const context = React.useContext(ChartContext);
  if (!context) {
    throw new Error("useChart must be used within a <ChartContainer />");
  }
  return context;
}

const ChartContainer = React.forwardRef<
  HTMLDivElement,
  React.ComponentProps<"div"> & {
    config: ChartConfig;
    children: React.ComponentProps<
      typeof RechartsPrimitive.ResponsiveContainer
    >["children"];
  }
>(({ id, className, children, config, ...props }, ref) => {
  const uniqueId = React.useId();
  const chartId = `chart-${id || uniqueId.replace(/:/g, "")}`;

  return (
    <ChartContext.Provider value={{ config }}>
      <div
        data-chart={chartId}
        ref={ref}
        className={cn(
          "flex aspect-video justify-center text-xs [&_.recharts-cartesian-axis-tick_text]:fill-muted-foreground [&_.recharts-cartesian-grid_line[stroke='#ccc']]:stroke-border/50 [&_.recharts-curve.recharts-tooltip-cursor]:stroke-border [&_.recharts-dot[stroke='#fff']]:stroke-transparent [&_.recharts-layer]:outline-none [&_.recharts-sector[stroke='#fff']]:stroke-transparent [&_.recharts-sector]:outline-none [&_.recharts-surface]:outline-none",
          className,
        )}
        {...props}
      >
        <ChartStyle id={chartId} config={config} />
        <RechartsPrimitive.ResponsiveContainer>
          {children}
        </RechartsPrimitive.ResponsiveContainer>
      </div>
    </ChartContext.Provider>
  );
});
ChartContainer.displayName = "Chart";

const ChartStyle = ({ id, config }: { id: string; config: ChartConfig }) => {
  const colorConfig = Object.entries(config).filter(
    ([, cfg]) => cfg.color,
  );

  if (!colorConfig.length) {
    return null;
  }

  return (
    <style
      dangerouslySetInnerHTML={{
        __html: `[data-chart=${id}] {\n${colorConfig
          .map(([key, cfg]) => (cfg.color ? `  --color-${key}: ${cfg.color};` : null))
          .filter(Boolean)
          .join("\n")}\n}`,
      }}
    />
  );
};

const ChartTooltip = RechartsPrimitive.Tooltip;

const ChartTooltipContent = React.forwardRef<
  HTMLDivElement,
  React.ComponentProps<"div"> & {
    active?: boolean;
    payload?: Array<{
      name?: string;
      value?: number | string;
      dataKey?: string | number;
      color?: string;
      payload?: Record<string, unknown>;
    }>;
    label?: React.ReactNode;
    labelFormatter?: (label: unknown) => React.ReactNode;
    formatter?: (value: number | string, name: string) => React.ReactNode;
    hideLabel?: boolean;
  }
>(
  (
    {
      active,
      payload,
      label,
      labelFormatter,
      formatter,
      hideLabel,
      className,
    },
    ref,
  ) => {
    const { config } = useChart();

    if (!active || !payload?.length) {
      return null;
    }

    return (
      <div
        ref={ref}
        className={cn(
          "grid min-w-[8rem] items-start gap-1.5 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl",
          className,
        )}
      >
        {!hideLabel && label != null && (
          <div className="font-medium">
            {labelFormatter ? labelFormatter(label) : label}
          </div>
        )}
        <div className="grid gap-1.5">
          {payload.map((item, index) => {
            const key = String(item.dataKey ?? item.name ?? index);
            const itemConfig = config[key];
            const displayName = itemConfig?.label ?? item.name ?? key;
            const color = item.color ?? itemConfig?.color;
            return (
              <div
                key={index}
                className="flex w-full items-center gap-2 [&>svg]:h-2.5 [&>svg]:w-2.5"
              >
                {color && (
                  <span
                    className="h-2.5 w-2.5 shrink-0 rounded-[2px]"
                    style={{ backgroundColor: color }}
                  />
                )}
                <div className="flex flex-1 justify-between leading-none">
                  <span className="text-muted-foreground">{displayName}</span>
                  <span className="font-mono font-medium tabular-nums text-foreground">
                    {formatter && item.value != null
                      ? formatter(item.value, String(displayName))
                      : item.value}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  },
);
ChartTooltipContent.displayName = "ChartTooltipContent";

export {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartStyle,
  useChart,
};
