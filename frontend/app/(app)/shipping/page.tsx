"use client";

import { Truck } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { PendingCard } from "@/components/ui/PendingCard";
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
import { Badge } from "@/components/ui/Badge";
import { listShipments } from "@/lib/api/shipping";
import { useFeatureEnabled } from "@/lib/hooks/useFeatureEnabled";
import { documentStatusColor } from "@/lib/utils/statusColor";
import { formatDateTime } from "@/lib/utils/format";
import Link from "next/link";

export default function ShippingPage() {
  const feature = useFeatureEnabled("shipment.ship");
  const shipments = useQuery({
    queryKey: ["shipments"],
    queryFn: () => listShipments(),
  });

  if (!feature.loading && !feature.enabled) {
    return (
      <div>
        <PageHeader
          title="Shipments"
          description="Outbound shipments — pending shipping-service implementation."
        />
        <PendingCard
          title="Implementation pending"
          message="The shipping-service /ship endpoint is currently a stub. Shipment records will populate during the recorded demo, after the agent's SHIP-101 implementation lands."
          jiraTicket="SHIP-101"
        />
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="Shipments"
        description="Outbound shipments by carrier."
      />
      <Card>
        <CardContent className="p-0">
          {shipments.isLoading ? (
            <div className="p-4">
              <SkeletonTable rows={6} cols={6} />
            </div>
          ) : shipments.isError ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load shipments: {(shipments.error as Error).message}
            </div>
          ) : (shipments.data ?? []).length === 0 ? (
            <div className="p-6">
              <EmptyState
                icon={<Truck className="h-5 w-5" />}
                title="No shipments yet."
                description="Once an order is shipped, the resulting shipment will land here."
              />
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>Document #</TH>
                  <TH>Status</TH>
                  <TH>Order</TH>
                  <TH>Customer</TH>
                  <TH>Carrier</TH>
                  <TH>Shipped</TH>
                </TR>
              </THead>
              <TBody>
                {(shipments.data ?? []).length === 0 ? (
                  <EmptyRow colSpan={6} message="No shipments yet." />
                ) : (
                  shipments.data!.map((s) => (
                    <TR key={s.id}>
                      <TD className="font-mono text-sm">
                        <Link
                          href={`/shipping/${s.id}`}
                          className="text-blue-700 hover:underline"
                        >
                          {s.documentNo}
                        </Link>
                      </TD>
                      <TD>
                        <Badge tone={documentStatusColor(s.status)}>
                          {s.status}
                        </Badge>
                      </TD>
                      <TD className="font-mono text-xs">
                        {s.orderDocumentNo}
                      </TD>
                      <TD>{s.customerName}</TD>
                      <TD className="text-slate-600">
                        {s.carrierName ?? "—"}
                        {s.serviceCode ? (
                          <span className="text-xs text-slate-400">
                            {" "}
                            · {s.serviceCode}
                          </span>
                        ) : null}
                      </TD>
                      <TD className="text-slate-600">
                        {formatDateTime(s.shippedAt)}
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
