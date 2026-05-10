import { api } from "./client";
import { mockPurchaseOrders } from "./mocks";
import type { PurchaseOrder } from "@/lib/types/domain";

export function listPurchaseOrders(): Promise<PurchaseOrder[]> {
  return api.get<PurchaseOrder[]>("/purchase-orders", {
    mock: () => mockPurchaseOrders(),
  });
}
