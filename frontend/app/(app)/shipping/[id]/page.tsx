"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { PendingCard } from "@/components/ui/PendingCard";
import { Skeleton } from "@/components/ui/Skeleton";
import { useFeatureEnabled } from "@/lib/hooks/useFeatureEnabled";
import { getShipment } from "@/lib/api/shipping";
import { documentStatusColor } from "@/lib/utils/statusColor";
import { formatDateTime } from "@/lib/utils/format";

export default function ShipmentDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";
  const feature = useFeatureEnabled("shipment.ship");

  const shipmentQuery = useQuery({
    queryKey: ["shipment", id],
    queryFn: () => getShipment(id),
    enabled: Boolean(id),
  });

  if (!feature.loading && !feature.enabled) {
    return (
      <div>
        <PageHeader
          title="Shipment detail"
          description="Pending shipping-service implementation."
        />
        <PendingCard
          title="Implementation pending"
          message="Shipment records are unavailable until the agent's SHIP-101 implementation lands during the recorded demo."
          jiraTicket="SHIP-101"
        />
      </div>
    );
  }

  return (
    <div>
      <Link
        href="/shipping"
        className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to shipments
      </Link>
      <PageHeader title={`Shipment ${id}`} />
      <Card>
        <CardContent>
          {shipmentQuery.isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-4 w-48" />
              <Skeleton className="h-4 w-64" />
              <Skeleton className="h-4 w-40" />
            </div>
          ) : !shipmentQuery.data ? (
            <p className="text-sm text-slate-500">
              Shipment not found, or shipping-service has not been implemented yet.
            </p>
          ) : (
            <div className="space-y-2 text-sm">
              <div className="flex gap-3">
                <span className="text-slate-500">Document #</span>
                <span className="font-mono">
                  {shipmentQuery.data.documentNo}
                </span>
              </div>
              <div className="flex gap-3">
                <span className="text-slate-500">Status</span>
                <Badge tone={documentStatusColor(shipmentQuery.data.status)}>
                  {shipmentQuery.data.status}
                </Badge>
              </div>
              <div className="flex gap-3">
                <span className="text-slate-500">Order</span>
                <span className="font-mono">
                  {shipmentQuery.data.orderDocumentNo}
                </span>
              </div>
              <div className="flex gap-3">
                <span className="text-slate-500">Customer</span>
                <span>{shipmentQuery.data.customerName}</span>
              </div>
              <div className="flex gap-3">
                <span className="text-slate-500">Carrier</span>
                <span>
                  {shipmentQuery.data.carrierName ?? "—"}{" "}
                  {shipmentQuery.data.serviceCode
                    ? `· ${shipmentQuery.data.serviceCode}`
                    : ""}
                </span>
              </div>
              <div className="flex gap-3">
                <span className="text-slate-500">Tracking #</span>
                <span className="font-mono">
                  {shipmentQuery.data.trackingNumber ?? "—"}
                </span>
              </div>
              <div className="flex gap-3">
                <span className="text-slate-500">Shipped</span>
                <span>{formatDateTime(shipmentQuery.data.shippedAt)}</span>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
