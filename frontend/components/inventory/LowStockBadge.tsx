import { AlertTriangle } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

export function LowStockBadge({ low }: { low: boolean }) {
  if (!low) {
    return (
      <Badge tone="bg-emerald-50 text-emerald-700 border-emerald-200">
        OK
      </Badge>
    );
  }
  return (
    <Badge tone="bg-red-50 text-red-700 border-red-200">
      <AlertTriangle className="h-3 w-3" aria-hidden />
      Low stock
    </Badge>
  );
}
