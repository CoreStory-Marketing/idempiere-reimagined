import type {
  DocumentStatus,
  NotificationStatus,
  OrderStatus,
} from "@/lib/types/domain";

/**
 * Tailwind utility classes for the order status chip palette spec'd in EPIC-09.
 * DRAFT gray / CONFIRMED blue / SHIPPED green / INVOICED purple /
 * COMPLETE emerald / CANCELLED red. PENDING and VOIDED get sensible neighbors.
 */
export function orderStatusColor(status: OrderStatus): string {
  switch (status) {
    case "DRAFT":
      return "bg-slate-100 text-slate-700 border-slate-200";
    case "PENDING":
      return "bg-amber-100 text-amber-800 border-amber-200";
    case "CONFIRMED":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "SHIPPED":
      return "bg-green-100 text-green-800 border-green-200";
    case "INVOICED":
      return "bg-purple-100 text-purple-800 border-purple-200";
    case "COMPLETE":
      return "bg-emerald-100 text-emerald-800 border-emerald-200";
    case "CANCELLED":
      return "bg-red-100 text-red-800 border-red-200";
    case "VOIDED":
      return "bg-zinc-200 text-zinc-700 border-zinc-300";
    default:
      return "bg-slate-100 text-slate-700 border-slate-200";
  }
}

export function documentStatusColor(status: DocumentStatus): string {
  switch (status) {
    case "DRAFT":
      return "bg-slate-100 text-slate-700 border-slate-200";
    case "IN_PROGRESS":
      return "bg-amber-100 text-amber-800 border-amber-200";
    case "COMPLETED":
      return "bg-emerald-100 text-emerald-800 border-emerald-200";
    case "CLOSED":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "VOIDED":
      return "bg-zinc-200 text-zinc-700 border-zinc-300";
    case "REVERSED":
      return "bg-red-100 text-red-800 border-red-200";
    default:
      return "bg-slate-100 text-slate-700 border-slate-200";
  }
}

export function notificationStatusColor(status: NotificationStatus): string {
  switch (status) {
    case "PENDING":
      return "bg-slate-100 text-slate-700 border-slate-200";
    case "SENT":
      return "bg-emerald-100 text-emerald-800 border-emerald-200";
    case "FAILED":
      return "bg-red-100 text-red-800 border-red-200";
    case "RETRYING":
      return "bg-amber-100 text-amber-800 border-amber-200";
    case "SUPPRESSED":
      return "bg-zinc-200 text-zinc-700 border-zinc-300";
    default:
      return "bg-slate-100 text-slate-700 border-slate-200";
  }
}
