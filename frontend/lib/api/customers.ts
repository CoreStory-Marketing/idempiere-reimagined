import { api } from "./client";
import { mockCustomers } from "./mocks";
import type { Customer } from "@/lib/types/domain";

export function listCustomers(query?: string): Promise<Customer[]> {
  return api.get<Customer[]>("/customers", {
    query: { q: query || undefined },
    mock: () => {
      const rows = mockCustomers();
      if (!query) return rows;
      const q = query.toLowerCase();
      return rows.filter(
        (c) => c.name.toLowerCase().includes(q) || c.code.toLowerCase().includes(q),
      );
    },
  });
}

export function getCustomer(id: string): Promise<Customer> {
  return api.get<Customer>(`/customers/${id}`, {
    mock: () => {
      const all = mockCustomers();
      return all.find((c) => c.id === id) ?? all[0]!;
    },
  });
}
