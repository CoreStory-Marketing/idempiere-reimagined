"use client";

import { useQuery } from "@tanstack/react-query";
import { fetchShipmentHealth } from "@/lib/api/shipping";

export type FeatureKey = "shipment.ship";

export interface FeatureState {
  enabled: boolean;
  reason?: string;
  loading: boolean;
}

/**
 * Returns whether a feature is enabled, based on a backend health probe.
 *
 * For `shipment.ship`: true when `/shipments/health` returns 200,
 * false (with reason "Pending shipping-service implementation") when
 * the endpoint returns 501 or the service is unreachable.
 *
 * Polls every 10s so the UI flips on automatically the moment the agent's
 * SHIP-101 code lands during the recorded demo.
 */
export function useFeatureEnabled(feature: FeatureKey): FeatureState {
  const query = useQuery({
    queryKey: ["feature", feature],
    queryFn: async () => {
      if (feature === "shipment.ship") {
        return fetchShipmentHealth();
      }
      return { ok: false, status: 501 };
    },
    refetchInterval: 10000,
    refetchIntervalInBackground: false,
  });

  if (query.isLoading) {
    return { enabled: false, loading: true };
  }
  if (!query.data?.ok) {
    return {
      enabled: false,
      loading: false,
      reason: "Pending shipping-service implementation",
    };
  }
  return { enabled: true, loading: false };
}
