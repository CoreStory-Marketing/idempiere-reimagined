"use client";

import { useQuery } from "@tanstack/react-query";
import { listOrders, type OrderListParams } from "@/lib/api/orders";

export function useOrders(params: OrderListParams) {
  return useQuery({
    queryKey: ["orders", params],
    queryFn: () => listOrders(params),
  });
}
