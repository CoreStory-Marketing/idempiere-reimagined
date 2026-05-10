import { Table, THead, TBody, TR, TH, TD, EmptyRow } from "@/components/ui/Table";
import { LowStockBadge } from "./LowStockBadge";
import { formatQty } from "@/lib/utils/format";
import type { StockRow } from "@/lib/types/domain";

export function StockTable({ rows }: { rows: StockRow[] }) {
  return (
    <Table>
      <THead>
        <TR>
          <TH>SKU</TH>
          <TH>Product</TH>
          <TH>Warehouse</TH>
          <TH>Locator</TH>
          <TH className="text-right">On hand</TH>
          <TH className="text-right">Reserved</TH>
          <TH className="text-right">Available</TH>
          <TH>Status</TH>
        </TR>
      </THead>
      <TBody>
        {rows.length === 0 ? (
          <EmptyRow colSpan={8} message="No stock records found." />
        ) : (
          rows.map((row) => (
            <TR key={`${row.productId}-${row.warehouseId}-${row.locatorCode ?? ""}`}>
              <TD className="font-mono text-xs">{row.sku}</TD>
              <TD className="font-medium text-slate-800">{row.productName}</TD>
              <TD className="text-slate-600">{row.warehouseName}</TD>
              <TD className="text-slate-600 font-mono text-xs">
                {row.locatorCode ?? "—"}
              </TD>
              <TD className="text-right tabular-nums">{formatQty(row.qtyOnHand)}</TD>
              <TD className="text-right tabular-nums text-slate-500">
                {formatQty(row.qtyReserved)}
              </TD>
              <TD className="text-right tabular-nums font-medium">
                {formatQty(row.qtyAvailable)}
              </TD>
              <TD>
                <LowStockBadge low={row.isLowStock} />
              </TD>
            </TR>
          ))
        )}
      </TBody>
    </Table>
  );
}
