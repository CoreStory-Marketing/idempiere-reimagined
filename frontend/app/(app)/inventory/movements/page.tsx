"use client";

import { useState } from "react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent } from "@/components/ui/Card";
import { Select } from "@/components/ui/Select";
import { SkeletonTable } from "@/components/ui/Skeleton";
import {
  Table,
  THead,
  TBody,
  TR,
  TH,
  TD,
  EmptyRow,
} from "@/components/ui/Table";
import { useMovements } from "@/lib/hooks/useInventory";
import { formatDateTime, formatQty } from "@/lib/utils/format";

const TYPES = ["", "RECEIPT", "ISSUE", "TRANSFER", "ADJUSTMENT"];

export default function MovementsPage() {
  const [type, setType] = useState<string>("");
  const movements = useMovements({ type: type || undefined });

  return (
    <div>
      <PageHeader
        title="Stock movements"
        description="Append-only inventory ledger across all warehouses."
      />

      <Card className="mb-4">
        <CardContent className="flex items-end gap-3">
          <div className="w-56">
            <label className="block text-xs font-medium text-slate-600 mb-1">
              Movement type
            </label>
            <Select value={type} onChange={(e) => setType(e.target.value)}>
              {TYPES.map((t) => (
                <option key={t} value={t}>
                  {t || "All types"}
                </option>
              ))}
            </Select>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          {movements.isLoading ? (
            <div className="p-4">
              <SkeletonTable rows={8} cols={7} />
            </div>
          ) : movements.isError ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load movements: {(movements.error as Error).message}
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>Date</TH>
                  <TH>Type</TH>
                  <TH>SKU</TH>
                  <TH>Product</TH>
                  <TH>Warehouse / Locator</TH>
                  <TH className="text-right">Qty</TH>
                  <TH>Reference</TH>
                </TR>
              </THead>
              <TBody>
                {(movements.data ?? []).length === 0 ? (
                  <EmptyRow colSpan={7} message="No movements found." />
                ) : (
                  movements.data!.map((m) => {
                    const qty = Number(m.quantity);
                    const isPositive = qty >= 0;
                    return (
                      <TR key={m.id}>
                        <TD className="whitespace-nowrap text-slate-600">
                          {formatDateTime(m.movementDate)}
                        </TD>
                        <TD>
                          <span className="rounded-md bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">
                            {m.movementType}
                          </span>
                        </TD>
                        <TD className="font-mono text-xs">{m.productSku}</TD>
                        <TD className="font-medium text-slate-800">
                          {m.productName}
                        </TD>
                        <TD className="text-slate-600">
                          {m.warehouseName}
                          {m.locatorCode ? (
                            <span className="text-slate-400 font-mono text-xs">
                              {" "}
                              · {m.locatorCode}
                            </span>
                          ) : null}
                        </TD>
                        <TD
                          className={
                            "text-right tabular-nums font-medium " +
                            (isPositive ? "text-emerald-700" : "text-red-700")
                          }
                        >
                          {isPositive ? "+" : ""}
                          {formatQty(m.quantity)} {m.uom}
                        </TD>
                        <TD className="text-xs text-slate-500">
                          {m.referenceType}{" "}
                          <span className="font-mono">{m.referenceNo}</span>
                        </TD>
                      </TR>
                    );
                  })
                )}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
