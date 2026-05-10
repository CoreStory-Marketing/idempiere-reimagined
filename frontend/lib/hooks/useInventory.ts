"use client";

import { useQuery } from "@tanstack/react-query";
import { listMovements, listStock, type StockListParams } from "@/lib/api/inventory";

export function useStock(params: StockListParams) {
  return useQuery({
    queryKey: ["stock", params],
    queryFn: () => listStock(params),
  });
}

export function useMovements(params: { warehouseId?: string; type?: string }) {
  return useQuery({
    queryKey: ["movements", params],
    queryFn: () => listMovements(params),
  });
}
