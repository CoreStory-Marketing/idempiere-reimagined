import { Badge } from "@/components/ui/Badge";
import { Table, THead, TBody, TR, TH, TD, EmptyRow } from "@/components/ui/Table";
import { ChannelIcon } from "./ChannelIcon";
import { formatDateTime, formatRelative } from "@/lib/utils/format";
import { notificationStatusColor } from "@/lib/utils/statusColor";
import type { NotificationLogEntry } from "@/lib/types/domain";

export function NotificationLogTable({ rows }: { rows: NotificationLogEntry[] }) {
  return (
    <Table>
      <THead>
        <TR>
          <TH>When</TH>
          <TH>Channel</TH>
          <TH>Template</TH>
          <TH>Recipient</TH>
          <TH>Subject</TH>
          <TH className="text-right">Attempts</TH>
          <TH>Status</TH>
        </TR>
      </THead>
      <TBody>
        {rows.length === 0 ? (
          <EmptyRow colSpan={7} message="No notifications yet." />
        ) : (
          rows.map((row) => (
            <TR key={row.id}>
              <TD className="whitespace-nowrap">
                <div className="text-sm">{formatRelative(row.occurredAt)}</div>
                <div className="text-xs text-slate-400">
                  {formatDateTime(row.occurredAt)}
                </div>
              </TD>
              <TD>
                <span className="inline-flex items-center gap-1.5 rounded-md bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">
                  <ChannelIcon channel={row.channel} />
                  {row.channel}
                </span>
              </TD>
              <TD className="font-mono text-xs">{row.templateCode}</TD>
              <TD className="text-slate-700">{row.recipient}</TD>
              <TD className="max-w-[24ch] truncate text-slate-600">
                {row.subject ?? "—"}
              </TD>
              <TD className="text-right tabular-nums">{row.attempts}</TD>
              <TD>
                <Badge tone={notificationStatusColor(row.status)}>
                  {row.status}
                </Badge>
              </TD>
            </TR>
          ))
        )}
      </TBody>
    </Table>
  );
}
