"use client";

import { Search } from "lucide-react";
import { useMemo, useState } from "react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { SkeletonTable } from "@/components/ui/Skeleton";
import { StockTable } from "@/components/inventory/StockTable";
import { useStock } from "@/lib/hooks/useInventory";

export default function InventoryPage() {
  const [warehouseId, setWarehouseId] = useState<string>("");
  const [lowOnly, setLowOnly] = useState(false);
  const [query, setQuery] = useState("");
  const [locator, setLocator] = useState<string>("");

  const stock = useStock({
    warehouseId: warehouseId || undefined,
    lowStockOnly: lowOnly || undefined,
    query: query || undefined,
  });

  const warehouses = useMemo(() => {
    const set = new Map<string, string>();
    for (const r of stock.data ?? []) {
      set.set(r.warehouseId, r.warehouseName);
    }
    return Array.from(set.entries());
  }, [stock.data]);

  const locators = useMemo(() => {
    const set = new Set<string>();
    for (const r of stock.data ?? []) {
      if (r.locatorCode) set.add(r.locatorCode);
    }
    return Array.from(set).sort();
  }, [stock.data]);

  const filtered = useMemo(() => {
    if (!stock.data) return [];
    if (!locator) return stock.data;
    return stock.data.filter((r) => r.locatorCode === locator);
  }, [stock.data, locator]);

  const lowCount = (stock.data ?? []).filter((r) => r.isLowStock).length;

  return (
    <div>
      <PageHeader
        title="Inventory"
        description="Stock by product × warehouse × locator."
        actions={
          <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-800">
            {lowCount} low-stock SKU{lowCount === 1 ? "" : "s"}
          </span>
        }
      />

      <Card className="mb-4">
        <CardContent className="flex flex-wrap items-end gap-3">
          <div className="flex-1 min-w-[240px]">
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Search
            </label>
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
              <Input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="SKU or product name"
                className="pl-8"
              />
            </div>
          </div>
          <div className="w-48">
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Warehouse
            </label>
            <Select
              value={warehouseId}
              onChange={(e) => {
                setWarehouseId(e.target.value);
                setLocator("");
              }}
            >
              <option value="">All</option>
              {warehouses.map(([id, name]) => (
                <option key={id} value={id}>
                  {name}
                </option>
              ))}
            </Select>
          </div>
          <div className="w-44">
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Locator
            </label>
            <Select
              value={locator}
              onChange={(e) => setLocator(e.target.value)}
            >
              <option value="">All</option>
              {locators.map((l) => (
                <option key={l} value={l}>
                  {l}
                </option>
              ))}
            </Select>
          </div>
          <Button
            variant={lowOnly ? "primary" : "outline"}
            onClick={() => setLowOnly((v) => !v)}
          >
            Low stock only
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          {stock.isLoading ? (
            <div className="p-4">
              <SkeletonTable rows={10} cols={8} />
            </div>
          ) : stock.isError ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load stock: {(stock.error as Error).message}
            </div>
          ) : (
            <StockTable rows={filtered} />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
