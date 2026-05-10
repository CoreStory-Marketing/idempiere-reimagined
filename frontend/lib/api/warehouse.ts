import { api } from "./client";
import { mockReceipts, mockWarehouses } from "./mocks";
import type { Receipt, Warehouse } from "@/lib/types/domain";

export function listReceipts(): Promise<Receipt[]> {
  return api.get<Receipt[]>("/receipts", { mock: () => mockReceipts() });
}

export function getReceipt(id: string): Promise<Receipt> {
  return api.get<Receipt>(`/receipts/${id}`, {
    mock: () => {
      const all = mockReceipts();
      return all.find((r) => r.id === id) ?? all[0]!;
    },
  });
}

export interface CreateReceiptInput {
  vendorId: string;
  warehouseId: string;
  receiptDate: string;
  lines: Array<{
    productId: string;
    quantity: string;
    locatorId?: string;
    purchaseOrderRef?: string;
  }>;
}

export function createReceipt(input: CreateReceiptInput): Promise<Receipt> {
  return api.post<Receipt>("/receipts", input);
}

export function postReceipt(id: string): Promise<Receipt> {
  return api.post<Receipt>(`/receipts/${id}/post`);
}

export function listWarehouses(): Promise<Warehouse[]> {
  return api.get<Warehouse[]>("/warehouses", { mock: () => mockWarehouses() });
}

export function getWarehouse(id: string): Promise<Warehouse> {
  return api.get<Warehouse>(`/warehouses/${id}`, {
    mock: () => {
      const all = mockWarehouses();
      return all.find((w) => w.id === id) ?? all[0]!;
    },
  });
}
