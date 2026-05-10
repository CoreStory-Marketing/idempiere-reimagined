import { api } from "./client";
import {
  mockDashboardCounts,
  mockLowStock,
  mockRecentOrders,
} from "./mocks";
import type {
  DashboardCounts,
  LowStockBucket,
  RecentOrdersBucket,
} from "@/lib/types/domain";
import { listNotificationLog } from "./notifications";
import { listOrders } from "./orders";
import { listReceipts } from "./warehouse";
import { listShipments } from "./shipping";
import { listProducts } from "./inventory";

/**
 * Dashboard counts are computed by hitting the per-resource list endpoints
 * and reading their counts. The backend doesn't yet expose dedicated
 * `/dashboard/counts` aggregation, so we aggregate client-side.
 */
export async function getDashboardCounts(): Promise<DashboardCounts> {
  const [orders, products, receipts, shipments, notifications] =
    await Promise.allSettled([
      listOrders({ page: 0, size: 1 }),
      listProducts({}),
      listReceipts(),
      listShipments(),
      listNotificationLog(),
    ]);

  return {
    orderCount:
      orders.status === "fulfilled" ? orders.value.totalElements : 0,
    productCount:
      products.status === "fulfilled" ? products.value.length : 0,
    receiptCount:
      receipts.status === "fulfilled" ? receipts.value.length : 0,
    shipmentCount:
      shipments.status === "fulfilled" ? shipments.value.length : 0,
    notificationCount:
      notifications.status === "fulfilled"
        ? notifications.value.length
        : 0,
  };
}

export function getRecentOrdersBuckets(): Promise<RecentOrdersBucket[]> {
  return api.get<RecentOrdersBucket[]>("/dashboard/recent-orders", {
    mock: () => mockRecentOrders(),
  });
}

export function getLowStockBuckets(): Promise<LowStockBucket[]> {
  return api.get<LowStockBucket[]>("/dashboard/low-stock", {
    mock: () => mockLowStock(),
  });
}

export function fallbackDashboardCounts(): DashboardCounts {
  return mockDashboardCounts();
}
