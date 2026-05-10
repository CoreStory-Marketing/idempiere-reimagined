import { api, ApiError } from "./client";
import { mockShipments } from "./mocks";
import type { Shipment } from "@/lib/types/domain";

/**
 * Shipping API — currently mostly stubbed.
 *
 * Many endpoints return 501 Not Implemented from the backend until
 * SHIP-101 lands during the demo. We surface that gracefully.
 */

export function listShipments(): Promise<Shipment[]> {
  return api
    .get<Shipment[]>("/shipments", { mock: () => mockShipments() })
    .catch((err: unknown) => {
      if (err instanceof ApiError && err.status === 501) {
        return [];
      }
      throw err;
    });
}

export function getShipment(id: string): Promise<Shipment | null> {
  return api
    .get<Shipment>(`/shipments/${id}`, { mock: () => null as unknown as Shipment })
    .catch((err: unknown) => {
      if (err instanceof ApiError && (err.status === 501 || err.status === 404)) {
        return null;
      }
      throw err;
    });
}

export interface ShipmentHealth {
  ok: boolean;
  status: number;
}

/**
 * Hits the shipping-service health endpoint via the gateway.
 * Returns a 200 OK once SHIP-101 is implemented; 501 / network error otherwise.
 *
 * Note: we DON'T pass `mock` here — feature gating must reflect reality.
 */
export async function fetchShipmentHealth(): Promise<ShipmentHealth> {
  try {
    await api.get<unknown>("/shipments/health");
    return { ok: true, status: 200 };
  } catch (err) {
    if (err instanceof ApiError) {
      return { ok: false, status: err.status };
    }
    return { ok: false, status: 0 };
  }
}

export interface CreateShipmentInput {
  orderId: string;
  carrierId: string;
  serviceCode: string;
  trackingNumber?: string;
}

export function createShipment(input: CreateShipmentInput): Promise<Shipment> {
  return api.post<Shipment>("/shipments", input);
}
