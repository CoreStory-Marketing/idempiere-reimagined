import { api } from "./client";
import { mockNotificationLog } from "./mocks";
import type { NotificationLogEntry } from "@/lib/types/domain";

export function listNotificationLog(): Promise<NotificationLogEntry[]> {
  return api.get<NotificationLogEntry[]>("/notifications/log", {
    mock: () => mockNotificationLog(),
  });
}
