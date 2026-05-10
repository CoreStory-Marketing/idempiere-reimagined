"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { ArrowLeft, Mail, Phone, User } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/Card";
import { Skeleton, SkeletonTable } from "@/components/ui/Skeleton";
import {
  EmptyRow,
  TBody,
  TD,
  TH,
  THead,
  TR,
  Table,
} from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { OrderStatusBadge } from "@/components/orders/OrderStatusBadge";
import { OrderTimeline } from "@/components/orders/OrderTimeline";
import { ShipOrderButton } from "@/components/orders/ShipOrderButton";
import { useOrder } from "@/lib/hooks/useOrder";
import {
  formatCurrency,
  formatDate,
  formatQty,
  formatDateTime,
} from "@/lib/utils/format";

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";
  const orderQuery = useOrder(id);

  if (orderQuery.isLoading) {
    return (
      <div>
        <Skeleton className="h-8 w-64" />
        <Skeleton className="mt-2 h-4 w-48" />
        <div className="mt-6 grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2 space-y-4">
            <Skeleton className="h-24" />
            <SkeletonTable rows={6} cols={5} />
          </div>
          <Skeleton className="h-64" />
        </div>
      </div>
    );
  }

  if (orderQuery.isError || !orderQuery.data) {
    return (
      <div>
        <PageHeader title="Order not found" />
        <p className="text-sm text-red-600">
          {(orderQuery.error as Error)?.message ?? "Could not load order."}
        </p>
        <Link href="/orders" className="mt-4 inline-block text-sm text-blue-600">
          ← Back to orders
        </Link>
      </div>
    );
  }

  const order = orderQuery.data;

  return (
    <div>
      <Link
        href="/orders"
        className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to orders
      </Link>
      <PageHeader
        title={`Order ${order.documentNo}`}
        description={
          <span className="inline-flex items-center gap-2">
            <OrderStatusBadge status={order.status} />
            <span>·</span>
            <span>{formatDate(order.orderDate)}</span>
            {order.promisedDate ? (
              <>
                <span>·</span>
                <span>Promised {formatDate(order.promisedDate)}</span>
              </>
            ) : null}
          </span>
        }
        actions={
          <div className="flex items-center gap-2">
            <Button variant="outline">Print</Button>
            <ShipOrderButton status={order.status} />
          </div>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Lines</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <Table>
                <THead>
                  <TR>
                    <TH className="w-12">#</TH>
                    <TH>SKU</TH>
                    <TH>Product</TH>
                    <TH className="text-right">Qty</TH>
                    <TH className="text-right">Unit price</TH>
                    <TH className="text-right">Line total</TH>
                  </TR>
                </THead>
                <TBody>
                  {order.lines.length === 0 ? (
                    <EmptyRow colSpan={6} message="No lines on this order." />
                  ) : (
                    order.lines.map((line) => (
                      <TR key={line.id}>
                        <TD className="text-slate-500">{line.lineNo}</TD>
                        <TD className="font-mono text-xs">{line.productSku}</TD>
                        <TD className="font-medium text-slate-800">
                          {line.productName}
                          {line.description ? (
                            <div className="text-xs text-slate-500">
                              {line.description}
                            </div>
                          ) : null}
                        </TD>
                        <TD className="text-right tabular-nums">
                          {formatQty(line.quantity)} {line.uom}
                        </TD>
                        <TD className="text-right tabular-nums">
                          {formatCurrency(line.unitPrice)}
                        </TD>
                        <TD className="text-right tabular-nums font-medium">
                          {formatCurrency(line.lineTotal)}
                        </TD>
                      </TR>
                    ))
                  )}
                </TBody>
              </Table>
            </CardContent>
            <div className="border-t border-slate-100 px-5 py-3 text-sm">
              <div className="flex justify-end gap-8">
                <span className="text-slate-500">Subtotal</span>
                <span className="tabular-nums font-medium">
                  {formatCurrency(order.totalLines)}
                </span>
              </div>
              <div className="mt-1 flex justify-end gap-8">
                <span className="font-semibold">Grand total</span>
                <span className="tabular-nums font-semibold text-slate-900">
                  {formatCurrency(order.grandTotal)}
                </span>
              </div>
            </div>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Status timeline</CardTitle>
            </CardHeader>
            <CardContent>
              <OrderTimeline
                history={order.statusHistory}
                currentStatus={order.status}
              />
            </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Customer</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex items-center gap-2 text-slate-800 font-medium">
                <User className="h-4 w-4 text-slate-500" />
                {order.customer.name}
              </div>
              <div className="text-xs text-slate-500 font-mono">
                {order.customer.code}
              </div>
              {order.customer.email ? (
                <div className="flex items-center gap-2 text-slate-600">
                  <Mail className="h-4 w-4 text-slate-400" />
                  <a
                    href={`mailto:${order.customer.email}`}
                    className="hover:underline"
                  >
                    {order.customer.email}
                  </a>
                </div>
              ) : null}
              {order.customer.phone ? (
                <div className="flex items-center gap-2 text-slate-600">
                  <Phone className="h-4 w-4 text-slate-400" />
                  {order.customer.phone}
                </div>
              ) : null}
              <Link
                href={`/customers/${order.customer.id}`}
                className="mt-2 inline-block text-xs text-blue-600 hover:underline"
              >
                View customer →
              </Link>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Addresses</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">
                  Shipping
                </div>
                {order.shippingAddress ? (
                  <p className="mt-1 text-slate-700 leading-snug">
                    {order.shippingAddress.line1}
                    {order.shippingAddress.line2 ? (
                      <>
                        <br />
                        {order.shippingAddress.line2}
                      </>
                    ) : null}
                    <br />
                    {order.shippingAddress.city},{" "}
                    {order.shippingAddress.region}{" "}
                    {order.shippingAddress.postalCode}
                    <br />
                    {order.shippingAddress.country}
                  </p>
                ) : (
                  <p className="text-slate-400">—</p>
                )}
              </div>
              <div>
                <div className="text-xs uppercase tracking-wide text-slate-500">
                  Billing
                </div>
                {order.billingAddress ? (
                  <p className="mt-1 text-slate-700 leading-snug">
                    {order.billingAddress.line1}
                    {order.billingAddress.line2 ? (
                      <>
                        <br />
                        {order.billingAddress.line2}
                      </>
                    ) : null}
                    <br />
                    {order.billingAddress.city},{" "}
                    {order.billingAddress.region}{" "}
                    {order.billingAddress.postalCode}
                    <br />
                    {order.billingAddress.country}
                  </p>
                ) : (
                  <p className="text-slate-400">—</p>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Meta</CardTitle>
            </CardHeader>
            <CardContent className="space-y-1 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-500">Warehouse</span>
                <span>{order.warehouseName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Currency</span>
                <span>{order.currency}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Created</span>
                <span>{formatDateTime(order.createdAt)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Updated</span>
                <span>{formatDateTime(order.updatedAt)}</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
