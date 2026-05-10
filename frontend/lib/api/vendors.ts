import { api } from "./client";
import { mockVendors } from "./mocks";
import type { Vendor } from "@/lib/types/domain";

export function listVendors(): Promise<Vendor[]> {
  return api.get<Vendor[]>("/vendors", { mock: () => mockVendors() });
}
