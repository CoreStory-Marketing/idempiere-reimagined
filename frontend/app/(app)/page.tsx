import {
  Bell,
  Box,
  PackageCheck,
  ShoppingCart,
  Truck,
  type LucideIcon,
} from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/Card";
import { PageHeader } from "@/components/layout/PageHeader";
import { BarChart, type BarDatum } from "@/components/charts/BarChart";
import {
  fallbackDashboardCounts,
  getDashboardCounts,
  getLowStockBuckets,
  getRecentOrdersBuckets,
} from "@/lib/api/dashboard";
import type {
  DashboardCounts,
  LowStockBucket,
  RecentOrdersBucket,
} from "@/lib/types/domain";

export const dynamic = "force-dynamic";

interface CountTileProps {
  label: string;
  value: number;
  icon: LucideIcon;
  hint?: string;
}

function CountTile({ label, value, icon: Icon, hint }: CountTileProps) {
  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-3">
        <div>
          <div className="text-xs uppercase tracking-wide text-slate-500">
            {label}
          </div>
          <div className="mt-1 text-2xl font-semibold tabular-nums text-slate-900">
            {value.toLocaleString()}
          </div>
          {hint ? (
            <div className="mt-1 text-xs text-slate-500">{hint}</div>
          ) : null}
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-md bg-blue-50 text-blue-600">
          <Icon className="h-5 w-5" aria-hidden />
        </div>
      </CardContent>
    </Card>
  );
}

export default async function DashboardPage() {
  const [counts, recent, low] = await Promise.allSettled([
    getDashboardCounts(),
    getRecentOrdersBuckets(),
    getLowStockBuckets(),
  ]);

  const countsValue: DashboardCounts =
    counts.status === "fulfilled" ? counts.value : fallbackDashboardCounts();
  const recentValue: RecentOrdersBucket[] =
    recent.status === "fulfilled" ? recent.value : [];
  const lowValue: LowStockBucket[] = low.status === "fulfilled" ? low.value : [];

  const recentBars: BarDatum[] = recentValue.map((b) => ({
    label: b.date.slice(5),
    value: b.count,
  }));
  const lowBars: BarDatum[] = lowValue.map((b) => ({
    label: b.warehouseName.slice(0, 10),
    value: b.count,
    color: "#dc2626",
  }));

  return (
    <div>
      <PageHeader
        title="Dashboard"
        description="Operational summary across all five services."
      />

      <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <CountTile
          label="Orders"
          value={countsValue.orderCount}
          icon={ShoppingCart}
          hint="all statuses"
        />
        <CountTile
          label="Products"
          value={countsValue.productCount}
          icon={Box}
        />
        <CountTile
          label="Receipts"
          value={countsValue.receiptCount}
          icon={PackageCheck}
        />
        <CountTile
          label="Shipments"
          value={countsValue.shipmentCount}
          icon={Truck}
          hint="pending SHIP-101"
        />
        <CountTile
          label="Notifications"
          value={countsValue.notificationCount}
          icon={Bell}
          hint="last 24h"
        />
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Orders — last 7 days</CardTitle>
          </CardHeader>
          <CardContent>
            <BarChart
              data={recentBars}
              ariaLabel="Recent orders, last 7 days"
              emptyMessage="No orders in the last 7 days yet."
            />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Low-stock items by warehouse</CardTitle>
          </CardHeader>
          <CardContent>
            <BarChart
              data={lowBars}
              ariaLabel="Low-stock SKU count by warehouse"
              emptyMessage="No low-stock items right now."
            />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
