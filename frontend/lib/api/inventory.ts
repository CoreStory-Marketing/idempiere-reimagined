import { api } from "./client";
import {
  mockMovements,
  mockProducts,
  mockStock,
} from "./mocks";
import type {
  Product,
  StockMovement,
  StockRow,
} from "@/lib/types/domain";

export interface StockListParams {
  warehouseId?: string;
  lowStockOnly?: boolean;
  query?: string;
}

export function listStock(params: StockListParams = {}): Promise<StockRow[]> {
  return api.get<StockRow[]>("/inventory/stock", {
    query: {
      warehouseId: params.warehouseId,
      lowStockOnly: params.lowStockOnly ? true : undefined,
      q: params.query || undefined,
    },
    mock: () => {
      let rows = mockStock();
      if (params.warehouseId) {
        rows = rows.filter((r) => r.warehouseId === params.warehouseId);
      }
      if (params.lowStockOnly) {
        rows = rows.filter((r) => r.isLowStock);
      }
      if (params.query) {
        const q = params.query.toLowerCase();
        rows = rows.filter(
          (r) =>
            r.sku.toLowerCase().includes(q) ||
            r.productName.toLowerCase().includes(q),
        );
      }
      return rows;
    },
  });
}

export function listMovements(params: { warehouseId?: string; type?: string } = {}): Promise<StockMovement[]> {
  return api.get<StockMovement[]>("/inventory/movements", {
    query: {
      warehouseId: params.warehouseId,
      type: params.type,
    },
    mock: () => mockMovements(),
  });
}

export function listProducts(params: { query?: string } = {}): Promise<Product[]> {
  return api.get<Product[]>("/products", {
    query: { q: params.query || undefined },
    mock: () => {
      const rows = mockProducts();
      if (!params.query) return rows;
      const q = params.query.toLowerCase();
      return rows.filter(
        (p) =>
          p.sku.toLowerCase().includes(q) ||
          p.name.toLowerCase().includes(q),
      );
    },
  });
}

export function getProduct(id: string): Promise<Product> {
  return api.get<Product>(`/products/${id}`, {
    mock: () => {
      const all = mockProducts();
      return all.find((p) => p.id === id) ?? all[0]!;
    },
  });
}
