"use client";

import { useQuery } from "@tanstack/react-query";
import { listNotificationLog } from "@/lib/api/notifications";

/**
 * Polls the notification log every 5 seconds — the demo recording shows
 * entries appearing live as the agent's notification adapter runs.
 */
export function useNotificationLog() {
  return useQuery({
    queryKey: ["notification-log"],
    queryFn: () => listNotificationLog(),
    refetchInterval: 5000,
    refetchIntervalInBackground: false,
  });
}
