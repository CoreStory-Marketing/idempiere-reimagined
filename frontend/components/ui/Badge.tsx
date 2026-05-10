import type { HTMLAttributes } from "react";
import { cn } from "@/lib/utils/cn";

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  /** Pre-built color classes (bg/text/border). */
  tone?: string;
}

export function Badge({ className, tone, children, ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-medium",
        tone ?? "bg-slate-100 text-slate-700 border-slate-200",
        className,
      )}
      {...props}
    >
      {children}
    </span>
  );
}
