"use client";

import { useQuery } from "@tanstack/react-query";
import { getOrder } from "@/lib/api/orders";

export function useOrder(id: string) {
  return useQuery({
    queryKey: ["order", id],
    queryFn: () => getOrder(id),
    enabled: Boolean(id),
  });
}
