"use client";

import { ChevronLeft, ChevronRight, Search } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { OrderTable } from "@/components/orders/OrderTable";
import { Card, CardContent } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { SkeletonTable } from "@/components/ui/Skeleton";
import { useOrders } from "@/lib/hooks/useOrders";
import {
  useOrderFilterStore,
  type OrderStatusFilter,
} from "@/lib/stores/orderFilterStore";

const STATUS_OPTIONS: Array<{ value: OrderStatusFilter; label: string }> = [
  { value: "ALL", label: "All statuses" },
  { value: "DRAFT", label: "Draft" },
  { value: "PENDING", label: "Pending" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "SHIPPED", label: "Shipped" },
  { value: "INVOICED", label: "Invoiced" },
  { value: "COMPLETE", label: "Complete" },
  { value: "CANCELLED", label: "Cancelled" },
];

const PAGE_SIZE = 20;

export default function OrdersPage() {
  const { status, query, page, setStatus, setQuery, setPage } =
    useOrderFilterStore();

  const ordersQuery = useOrders({
    page,
    size: PAGE_SIZE,
    status,
    query,
  });

  const totalPages = ordersQuery.data?.totalPages ?? 0;
  const totalElements = ordersQuery.data?.total ?? 0;

  return (
    <div>
      <PageHeader
        title="Orders"
        description="Sales orders across all warehouses."
      />

      <Card className="mb-4">
        <CardContent className="flex flex-wrap items-end gap-3">
          <div className="flex-1 min-w-[240px]">
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Search by document #
            </label>
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
              <Input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="SO-001234"
                className="pl-8"
              />
            </div>
          </div>
          <div className="w-48">
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Status
            </label>
            <Select
              value={status}
              onChange={(e) => setStatus(e.target.value as OrderStatusFilter)}
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </Select>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          {ordersQuery.isLoading ? (
            <div className="p-4">
              <SkeletonTable rows={8} cols={7} />
            </div>
          ) : ordersQuery.isError ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load orders: {(ordersQuery.error as Error).message}
            </div>
          ) : (
            <OrderTable orders={ordersQuery.data?.items ?? []} />
          )}
        </CardContent>
        <div className="flex items-center justify-between border-t border-slate-100 px-4 py-3 text-sm">
          <div className="text-slate-500">
            {totalElements > 0
              ? `Showing page ${page + 1} of ${totalPages} · ${totalElements} total`
              : "No results"}
          </div>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              disabled={page === 0 || ordersQuery.isFetching}
              onClick={() => setPage(Math.max(0, page - 1))}
            >
              <ChevronLeft className="h-4 w-4" /> Prev
            </Button>
            <Button
              size="sm"
              variant="outline"
              disabled={
                page + 1 >= totalPages || ordersQuery.isFetching || totalPages === 0
              }
              onClick={() => setPage(page + 1)}
            >
              Next <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
