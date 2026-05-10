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
  EmptyRow,
} from "@/components/ui/Table";
import { listCarriers } from "@/lib/api/admin";

export const dynamic = "force-dynamic";

export default async function CarriersPage() {
  let rows: Awaited<ReturnType<typeof listCarriers>> = [];
  let error: string | null = null;
  try {
    rows = await listCarriers();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader title="Carriers" description="Shipping carriers and supported services." />
      {error ? (
        <Card>
          <CardContent>
            <p className="text-sm text-red-600">
              Failed to load carriers: {error}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {rows.map((c) => (
            <Card key={c.id}>
              <CardHeader>
                <div className="flex items-center gap-3">
                  <CardTitle>{c.name}</CardTitle>
                  <span className="text-xs font-mono text-slate-500">
                    {c.code}
                  </span>
                  {c.active ? (
                    <Badge tone="bg-emerald-50 text-emerald-700 border-emerald-200">
                      Active
                    </Badge>
                  ) : (
                    <Badge tone="bg-slate-100 text-slate-600 border-slate-200">
                      Inactive
                    </Badge>
                  )}
                </div>
              </CardHeader>
              <Table>
                <THead>
                  <TR>
                    <TH>Service code</TH>
                    <TH>Name</TH>
                    <TH className="text-right">Transit days</TH>
                  </TR>
                </THead>
                <TBody>
                  {c.services.length === 0 ? (
                    <EmptyRow colSpan={3} message="No services configured." />
                  ) : (
                    c.services.map((s) => (
                      <TR key={s.id}>
                        <TD className="font-mono text-xs">{s.code}</TD>
                        <TD className="font-medium">{s.name}</TD>
                        <TD className="text-right tabular-nums">
                          {s.transitDays ?? "—"}
                        </TD>
                      </TR>
                    ))
                  )}
                </TBody>
              </Table>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
