import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/Badge";
import { Card, CardContent } from "@/components/ui/Card";
import {
  Table,
  THead,
  TBody,
  TR,
  TH,
  TD,
  EmptyRow,
} from "@/components/ui/Table";
import { listPurchaseOrders } from "@/lib/api/purchasing";
import { documentStatusColor } from "@/lib/utils/statusColor";
import { formatCurrency, formatDate } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function PurchaseOrdersPage() {
  let rows: Awaited<ReturnType<typeof listPurchaseOrders>> = [];
  let error: string | null = null;
  try {
    rows = await listPurchaseOrders();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader
        title="Purchase orders"
        description="Inbound procurement orders and receipt linkage."
      />
      <Card>
        <CardContent className="p-0">
          {error ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load purchase orders: {error}
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
                  <TH>Promised</TH>
                  <TH className="text-right">Lines</TH>
                  <TH className="text-right">Total</TH>
                  <TH>Receipt</TH>
                </TR>
              </THead>
              <TBody>
                {rows.length === 0 ? (
                  <EmptyRow colSpan={9} message="No purchase orders found." />
                ) : (
                  rows.map((p) => (
                    <TR key={p.id}>
                      <TD className="font-mono text-sm">{p.documentNo}</TD>
                      <TD>
                        <Badge tone={documentStatusColor(p.status)}>
                          {p.status}
                        </Badge>
                      </TD>
                      <TD className="font-medium text-slate-800">{p.vendorName}</TD>
                      <TD className="text-slate-600">{p.warehouseName}</TD>
                      <TD className="text-slate-600">
                        {formatDate(p.orderDate)}
                      </TD>
                      <TD className="text-slate-600">
                        {formatDate(p.promisedDate)}
                      </TD>
                      <TD className="text-right tabular-nums">
                        {p.lineCount}
                      </TD>
                      <TD className="text-right tabular-nums font-medium">
                        {formatCurrency(p.grandTotal)}
                      </TD>
                      <TD className="font-mono text-xs text-slate-600">
                        {p.receiptDocumentNo ?? "—"}
                      </TD>
                    </TR>
                  ))
                )}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
