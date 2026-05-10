import type { ReactNode } from "react";
import { cn } from "@/lib/utils/cn";

/**
 * Pure-CSS tooltip — no Radix dependency. Triggers on hover/focus.
 * Wrap a single inline element (button, span, etc).
 */
export function Tooltip({
  content,
  children,
  side = "top",
  className,
}: {
  content: ReactNode;
  children: ReactNode;
  side?: "top" | "bottom" | "left" | "right";
  className?: string;
}) {
  if (!content) return <>{children}</>;
  const positions: Record<typeof side, string> = {
    top: "bottom-full left-1/2 -translate-x-1/2 mb-1.5",
    bottom: "top-full left-1/2 -translate-x-1/2 mt-1.5",
    left: "right-full top-1/2 -translate-y-1/2 mr-1.5",
    right: "left-full top-1/2 -translate-y-1/2 ml-1.5",
  };
  return (
    <span className={cn("relative inline-flex group", className)}>
      {children}
      <span
        role="tooltip"
        className={cn(
          "pointer-events-none absolute z-40 whitespace-nowrap rounded-md bg-slate-900 px-2 py-1 text-xs font-medium text-white shadow-md",
          "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 transition-opacity",
          positions[side],
        )}
      >
        {content}
      </span>
    </span>
  );
}
