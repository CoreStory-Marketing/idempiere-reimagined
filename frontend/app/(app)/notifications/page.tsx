"use client";

import { Bell, Inbox, RefreshCw } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { SkeletonTable } from "@/components/ui/Skeleton";
import { NotificationLogTable } from "@/components/notifications/NotificationLogTable";
import { useNotificationLog } from "@/lib/hooks/useNotificationLog";

export default function NotificationsPage() {
  const log = useNotificationLog();
  const rows = log.data ?? [];

  return (
    <div>
      <PageHeader
        title="Notifications"
        description="Live log of outbound notification dispatches. Refreshes every 5 seconds."
        actions={
          <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700">
            <RefreshCw
              className={
                "h-3 w-3 " + (log.isFetching ? "animate-spin" : "")
              }
              aria-hidden
            />
            {log.isFetching ? "Refreshing…" : "Auto-refresh on"}
          </span>
        }
      />

      <Card>
        <CardContent className="p-0">
          {log.isLoading ? (
            <div className="p-4">
              <SkeletonTable rows={6} cols={7} />
            </div>
          ) : log.isError ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load notifications: {(log.error as Error).message}
            </div>
          ) : rows.length === 0 ? (
            <div className="p-6">
              <EmptyState
                icon={<Inbox className="h-5 w-5" />}
                title="No notifications yet."
                description={
                  <>
                    The notifications log is empty. Once the agent&apos;s email
                    adapter writes an entry, it will appear here within ~5
                    seconds. <br />
                    <span className="text-xs">
                      Tip: shipped orders trigger an{" "}
                      <span className="font-mono">ORDER_SHIPPED</span> email
                      via MailHog.
                    </span>
                  </>
                }
                action={
                  <span className="inline-flex items-center gap-2 text-xs text-slate-500">
                    <Bell className="h-3.5 w-3.5" /> Polling every 5s
                  </span>
                }
              />
            </div>
          ) : (
            <NotificationLogTable rows={rows} />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
