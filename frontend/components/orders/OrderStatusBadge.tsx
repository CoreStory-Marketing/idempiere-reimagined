import { Badge } from "@/components/ui/Badge";
import { orderStatusColor } from "@/lib/utils/statusColor";
import type { OrderStatus } from "@/lib/types/domain";

const LABELS: Record<OrderStatus, string> = {
  DRAFT: "Draft",
  PENDING: "Pending",
  CONFIRMED: "Confirmed",
  SHIPPED: "Shipped",
  INVOICED: "Invoiced",
  COMPLETE: "Complete",
  CANCELLED: "Cancelled",
  VOIDED: "Voided",
};

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return <Badge tone={orderStatusColor(status)}>{LABELS[status]}</Badge>;
}
