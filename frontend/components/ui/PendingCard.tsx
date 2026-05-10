import { Construction } from "lucide-react";
import type { ReactNode } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./Card";

export function PendingCard({
  title,
  message,
  jiraTicket,
}: {
  title: string;
  message: ReactNode;
  jiraTicket?: string;
}) {
  return (
    <Card className="max-w-2xl">
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-md bg-amber-50 text-amber-600">
            <Construction className="h-5 w-5" aria-hidden />
          </div>
          <CardTitle>{title}</CardTitle>
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-slate-600">{message}</p>
        {jiraTicket ? (
          <p className="mt-3 text-xs text-slate-500">
            Tracked in JIRA:{" "}
            <span className="font-mono font-medium text-slate-700">
              {jiraTicket}
            </span>
          </p>
        ) : null}
      </CardContent>
    </Card>
  );
}
