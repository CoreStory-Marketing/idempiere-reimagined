"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardContent } from "@/components/ui/Card";
import { Dialog } from "@/components/ui/Dialog";
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
import { useReceipts } from "@/lib/hooks/useReceipts";
import { documentStatusColor } from "@/lib/utils/statusColor";
import { formatDate, formatQty } from "@/lib/utils/format";
import type { Receipt } from "@/lib/types/domain";

export default function ReceiptsPage() {
  const [createOpen, setCreateOpen] = useState(false);
  const [inspecting, setInspecting] = useState<Receipt | null>(null);
  const receipts = useReceipts();

  return (
    <div>
      <PageHeader
        title="Material receipts"
        description="Inbound vendor receipts → posted to inventory."
        actions={
          <Button onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4" /> New receipt
          </Button>
        }
      />

      <Card>
        <CardContent className="p-0">
          {receipts.isLoading ? (
            <div className="p-4">
              <SkeletonTable rows={6} cols={7} />
            </div>
          ) : receipts.isError ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load receipts: {(receipts.error as Error).message}
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>Document #</TH>
                  <TH>Status</TH>
                  <TH>Vendor</TH>
                  <TH>Warehouse</TH>
                  <TH>Date</TH>
                  <TH className="text-right">Qty</TH>
                  <TH className="text-right">Actions</TH>
                </TR>
              </THead>
              <TBody>
                {(receipts.data ?? []).length === 0 ? (
                  <EmptyRow colSpan={7} message="No receipts found." />
                ) : (
                  receipts.data!.map((r) => (
                    <TR key={r.id}>
                      <TD className="font-mono text-sm">{r.documentNo}</TD>
                      <TD>
                        <Badge tone={documentStatusColor(r.status)}>
                          {r.status}
                        </Badge>
                      </TD>
                      <TD className="font-medium text-slate-800">
                        {r.vendorName}
                      </TD>
                      <TD className="text-slate-600">{r.warehouseName}</TD>
                      <TD className="text-slate-600">
                        {formatDate(r.receiptDate)}
                      </TD>
                      <TD className="text-right tabular-nums">
                        {formatQty(r.totalQty)}
                      </TD>
                      <TD className="text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setInspecting(r)}
                        >
                          Inspect
                        </Button>
                      </TD>
                    </TR>
                  ))
                )}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        title="New material receipt"
        footer={
          <>
            <Button variant="outline" onClick={() => setCreateOpen(false)}>
              Cancel
            </Button>
            <Button disabled>Create draft</Button>
          </>
        }
      >
        <p className="text-sm text-slate-600">
          The full receipt entry form is wired to{" "}
          <span className="font-mono">POST /receipts</span>. The demo path
          uses pre-seeded receipts; this dialog is a stub for now.
        </p>
        <p className="mt-2 text-xs text-slate-500">
          Backend endpoint:{" "}
          <span className="font-mono">warehouse-service /receipts</span>.
        </p>
      </Dialog>

      <Dialog
        open={!!inspecting}
        onClose={() => setInspecting(null)}
        title={inspecting ? `Receipt ${inspecting.documentNo}` : "Receipt"}
        className="max-w-2xl"
      >
        {inspecting ? (
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <div className="text-xs text-slate-500">Vendor</div>
                <div className="font-medium">{inspecting.vendorName}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Warehouse</div>
                <div className="font-medium">{inspecting.warehouseName}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Date</div>
                <div className="font-medium">
                  {formatDate(inspecting.receiptDate)}
                </div>
              </div>
              <div>
                <div className="text-xs text-slate-500">Status</div>
                <div>
                  <Badge tone={documentStatusColor(inspecting.status)}>
                    {inspecting.status}
                  </Badge>
                </div>
              </div>
            </div>
            <div className="rounded-md border border-slate-200">
              <Table>
                <THead>
                  <TR>
                    <TH>#</TH>
                    <TH>SKU</TH>
                    <TH>Product</TH>
                    <TH className="text-right">Qty</TH>
                    <TH>Locator</TH>
                  </TR>
                </THead>
                <TBody>
                  {inspecting.lines.map((l) => (
                    <TR key={l.id}>
                      <TD className="text-slate-500">{l.lineNo}</TD>
                      <TD className="font-mono text-xs">{l.productSku}</TD>
                      <TD>{l.productName}</TD>
                      <TD className="text-right tabular-nums">
                        {formatQty(l.quantity)} {l.uom}
                      </TD>
                      <TD className="font-mono text-xs">{l.locatorCode ?? "—"}</TD>
                    </TR>
                  ))}
                </TBody>
              </Table>
            </div>
            {inspecting.status === "DRAFT" ? (
              <div className="flex justify-end">
                <Button>Post receipt</Button>
              </div>
            ) : null}
          </div>
        ) : null}
      </Dialog>
    </div>
  );
}
