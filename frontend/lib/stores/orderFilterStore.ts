"use client";

import { create } from "zustand";
import type { OrderStatus } from "@/lib/types/domain";

export type OrderStatusFilter = OrderStatus | "ALL";

interface OrderFilterState {
  status: OrderStatusFilter;
  query: string;
  page: number;
  setStatus: (s: OrderStatusFilter) => void;
  setQuery: (q: string) => void;
  setPage: (p: number) => void;
  reset: () => void;
}

export const useOrderFilterStore = create<OrderFilterState>((set) => ({
  status: "ALL",
  query: "",
  page: 0,
  setStatus: (status) => set({ status, page: 0 }),
  setQuery: (query) => set({ query, page: 0 }),
  setPage: (page) => set({ page }),
  reset: () => set({ status: "ALL", query: "", page: 0 }),
}));
