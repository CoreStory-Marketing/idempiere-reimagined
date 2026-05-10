"use client";

import Link from "next/link";
import { Table, THead, TBody, TR, TH, TD, EmptyRow } from "@/components/ui/Table";
import { OrderStatusBadge } from "./OrderStatusBadge";
import { formatCurrency, formatDate } from "@/lib/utils/format";
import type { OrderListItem } from "@/lib/types/domain";

export function OrderTable({ orders }: { orders: OrderListItem[] }) {
  return (
    <Table>
      <THead>
        <TR>
          <TH>Document #</TH>
          <TH>Status</TH>
          <TH>Customer</TH>
          <TH>Warehouse</TH>
          <TH>Date</TH>
          <TH className="text-right">Lines</TH>
          <TH className="text-right">Total</TH>
        </TR>
      </THead>
      <TBody>
        {orders.length === 0 ? (
          <EmptyRow colSpan={7} message="No orders match the current filters." />
        ) : (
          orders.map((o) => (
            <TR key={o.id}>
              <TD>
                <Link
                  href={`/orders/${o.id}`}
                  className="font-mono text-sm text-blue-700 hover:underline"
                >
                  {o.documentNo}
                </Link>
              </TD>
              <TD>
                <OrderStatusBadge status={o.status} />
              </TD>
              <TD className="font-medium text-slate-800">{o.customerName}</TD>
              <TD className="text-slate-600">{o.warehouseName}</TD>
              <TD className="text-slate-600">{formatDate(o.orderDate)}</TD>
              <TD className="text-right tabular-nums">{o.lineCount}</TD>
              <TD className="text-right tabular-nums font-medium">
                {formatCurrency(o.grandTotal)}
              </TD>
            </TR>
          ))
        )}
      </TBody>
    </Table>
  );
}
