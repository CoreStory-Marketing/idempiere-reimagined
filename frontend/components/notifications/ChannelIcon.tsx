import { Bell, Mail, MessageSquare, Webhook } from "lucide-react";
import type { NotificationChannel } from "@/lib/types/domain";

export function ChannelIcon({ channel }: { channel: NotificationChannel }) {
  const className = "h-3.5 w-3.5";
  switch (channel) {
    case "EMAIL":
      return <Mail className={className} aria-label="Email" />;
    case "SMS":
      return <MessageSquare className={className} aria-label="SMS" />;
    case "WEBHOOK":
      return <Webhook className={className} aria-label="Webhook" />;
    case "INTERNAL":
    default:
      return <Bell className={className} aria-label="Internal" />;
  }
}
