import { CheckCircle2, Circle } from "lucide-react";
import { OrderStatusBadge } from "./OrderStatusBadge";
import { formatDateTime } from "@/lib/utils/format";
import type { OrderStatus, StatusHistoryEntry } from "@/lib/types/domain";

const SEQUENCE: OrderStatus[] = [
  "DRAFT",
  "CONFIRMED",
  "SHIPPED",
  "INVOICED",
  "COMPLETE",
];

export function OrderTimeline({
  history,
  currentStatus,
}: {
  history: StatusHistoryEntry[];
  currentStatus: OrderStatus;
}) {
  const seenStatuses = new Set(history.map((h) => h.status));
  const lastIndex = SEQUENCE.indexOf(currentStatus);
  const cancelled = currentStatus === "CANCELLED" || currentStatus === "VOIDED";

  return (
    <ol className="space-y-4">
      {SEQUENCE.map((step, i) => {
        const reached = seenStatuses.has(step) || i <= lastIndex;
        const entry = history.find((h) => h.status === step);
        return (
          <li key={step} className="flex items-start gap-3">
            <div className="mt-0.5">
              {reached ? (
                <CheckCircle2 className="h-5 w-5 text-emerald-500" aria-hidden />
              ) : (
                <Circle className="h-5 w-5 text-slate-300" aria-hidden />
              )}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <OrderStatusBadge status={step} />
                {entry ? (
                  <span className="text-xs text-slate-500">
                    {formatDateTime(entry.changedAt)} · {entry.changedBy}
                  </span>
                ) : (
                  <span className="text-xs text-slate-400">pending</span>
                )}
              </div>
              {entry?.note ? (
                <p className="mt-1 text-xs text-slate-600">{entry.note}</p>
              ) : null}
            </div>
          </li>
        );
      })}
      {cancelled ? (
        <li className="flex items-start gap-3">
          <CheckCircle2 className="h-5 w-5 text-red-500" aria-hidden />
          <div>
            <OrderStatusBadge status={currentStatus} />
            <p className="mt-1 text-xs text-slate-500">
              Order terminated.
            </p>
          </div>
        </li>
      ) : null}
    </ol>
  );
}
