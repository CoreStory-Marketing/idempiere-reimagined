import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import {
  Table,
  THead,
  TBody,
  TR,
  TH,
  TD,
} from "@/components/ui/Table";
import { getSystemSettings } from "@/lib/api/admin";
import { formatDateTime } from "@/lib/utils/format";
import type { ServiceHealth } from "@/lib/types/domain";

export const dynamic = "force-dynamic";

function statusTone(status: ServiceHealth["status"]): string {
  switch (status) {
    case "UP":
      return "bg-emerald-50 text-emerald-700 border-emerald-200";
    case "DEGRADED":
      return "bg-amber-50 text-amber-800 border-amber-200";
    case "DOWN":
      return "bg-red-50 text-red-700 border-red-200";
    default:
      return "bg-slate-100 text-slate-600 border-slate-200";
  }
}

export default async function SettingsPage() {
  let settings: Awaited<ReturnType<typeof getSystemSettings>> | null = null;
  let error: string | null = null;
  try {
    settings = await getSystemSettings();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader
        title="System settings"
        description="Build info, gateway URL, and service health."
      />
      {error || !settings ? (
        <Card>
          <CardContent>
            <p className="text-sm text-red-600">
              Failed to load settings: {error ?? "Unknown error"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <Card>
            <CardHeader>
              <CardTitle>Build</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-500">App</span>
                <span>{settings.appName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Version</span>
                <span className="font-mono">{settings.version}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Build SHA</span>
                <span className="font-mono">{settings.buildSha}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Built</span>
                <span>{formatDateTime(settings.buildDate)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Environment</span>
                <span className="font-mono">{settings.environment}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Gateway</span>
                <span className="font-mono text-xs">
                  {settings.apiGatewayUrl}
                </span>
              </div>
            </CardContent>
          </Card>

          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Services</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <Table>
                <THead>
                  <TR>
                    <TH>Service</TH>
                    <TH>URL</TH>
                    <TH>Status</TH>
                    <TH>Notes</TH>
                  </TR>
                </THead>
                <TBody>
                  {settings.services.map((s) => (
                    <TR key={s.name}>
                      <TD className="font-medium font-mono text-xs">
                        {s.name}
                      </TD>
                      <TD className="text-xs font-mono text-slate-600">
                        {s.url ?? "—"}
                      </TD>
                      <TD>
                        <Badge tone={statusTone(s.status)}>{s.status}</Badge>
                      </TD>
                      <TD className="text-xs text-slate-500">
                        {s.notes ?? "—"}
                      </TD>
                    </TR>
                  ))}
                </TBody>
              </Table>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
