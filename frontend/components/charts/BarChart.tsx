import { cn } from "@/lib/utils/cn";

export interface BarDatum {
  label: string;
  value: number;
  /** Optional override; defaults to brand blue */
  color?: string;
}

export interface BarChartProps {
  data: BarDatum[];
  height?: number;
  className?: string;
  emptyMessage?: string;
  /** Format function for the bar's hover label and y-axis */
  format?: (value: number) => string;
  ariaLabel?: string;
}

const DEFAULT_COLOR = "#2563eb";

/**
 * Pure-SVG bar chart — no chart-lib dependency.
 * Vertical bars, evenly spaced, with x-axis labels under each bar.
 */
export function BarChart({
  data,
  height = 180,
  className,
  emptyMessage = "No data",
  format = (v) => String(v),
  ariaLabel,
}: BarChartProps) {
  if (!data.length) {
    return (
      <div
        className={cn(
          "flex items-center justify-center rounded-md border border-dashed border-slate-200 text-sm text-slate-500",
          className,
        )}
        style={{ height }}
      >
        {emptyMessage}
      </div>
    );
  }

  const max = Math.max(...data.map((d) => d.value), 1);
  const padTop = 8;
  const padBottom = 28;
  const padLeft = 28;
  const padRight = 8;
  const width = Math.max(data.length * 48, 280);
  const innerH = height - padTop - padBottom;
  const innerW = width - padLeft - padRight;
  const slot = innerW / data.length;
  const barW = Math.max(8, slot * 0.6);

  // Y axis ticks: 0, max/2, max
  const ticks = [0, max / 2, max];

  return (
    <div className={cn("w-full overflow-x-auto", className)}>
      <svg
        role="img"
        aria-label={ariaLabel ?? "Bar chart"}
        viewBox={`0 0 ${width} ${height}`}
        preserveAspectRatio="none"
        className="w-full"
        style={{ minHeight: height }}
      >
        {ticks.map((t, i) => {
          const y = padTop + innerH - (t / max) * innerH;
          return (
            <g key={i}>
              <line
                x1={padLeft}
                x2={width - padRight}
                y1={y}
                y2={y}
                stroke="#e2e8f0"
                strokeDasharray={i === 0 ? "0" : "3 3"}
              />
              <text
                x={padLeft - 4}
                y={y + 3}
                textAnchor="end"
                fontSize="9"
                fill="#94a3b8"
              >
                {format(Math.round(t))}
              </text>
            </g>
          );
        })}
        {data.map((d, i) => {
          const h = (d.value / max) * innerH;
          const x = padLeft + i * slot + (slot - barW) / 2;
          const y = padTop + innerH - h;
          return (
            <g key={`${d.label}-${i}`}>
              <rect
                x={x}
                y={y}
                width={barW}
                height={Math.max(0, h)}
                rx={3}
                fill={d.color ?? DEFAULT_COLOR}
              >
                <title>{`${d.label}: ${format(d.value)}`}</title>
              </rect>
              <text
                x={x + barW / 2}
                y={height - padBottom + 14}
                textAnchor="middle"
                fontSize="10"
                fill="#475569"
              >
                {d.label}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}
