"use client";

import { Truck } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Tooltip } from "@/components/ui/Tooltip";
import { useFeatureEnabled } from "@/lib/hooks/useFeatureEnabled";
import type { OrderStatus } from "@/lib/types/domain";

const PENDING_TOOLTIP = "Pending shipping-service implementation";

/**
 * Per EPIC-09:
 *   - Always disabled with tooltip "Pending shipping-service implementation"
 *     when status !== CONFIRMED.
 *   - Even when status === CONFIRMED, the button stays disabled until the
 *     `shipment.ship` feature flag flips on (i.e. shipping-service /health
 *     returns 200). The demo recording shows the button becoming enabled
 *     after the agent's SHIP-101 code lands.
 */
export function ShipOrderButton({
  status,
  onShip,
}: {
  status: OrderStatus;
  onShip?: () => void;
}) {
  const feature = useFeatureEnabled("shipment.ship");
  const isConfirmed = status === "CONFIRMED";
  const enabled = isConfirmed && feature.enabled;

  const tooltip = !isConfirmed
    ? `Order must be CONFIRMED to ship (currently ${status})`
    : PENDING_TOOLTIP;

  const button = (
    <Button
      variant="primary"
      disabled={!enabled}
      onClick={enabled ? onShip : undefined}
      aria-disabled={!enabled}
    >
      <Truck className="h-4 w-4" aria-hidden />
      Ship Order
    </Button>
  );

  if (enabled) return button;

  return (
    <Tooltip content={tooltip} side="bottom">
      {button}
    </Tooltip>
  );
}
