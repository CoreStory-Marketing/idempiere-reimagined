"use client";

import { useQuery } from "@tanstack/react-query";
import { listReceipts } from "@/lib/api/warehouse";

export function useReceipts() {
  return useQuery({
    queryKey: ["receipts"],
    queryFn: () => listReceipts(),
  });
}
