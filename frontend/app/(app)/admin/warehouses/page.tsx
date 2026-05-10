import Link from "next/link";
import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/Badge";
import { Card, CardContent } from "@/components/ui/Card";
import {
  Table,
  THead,
  TBody,
  TR,
  TH,
  TD,
  EmptyRow,
} from "@/components/ui/Table";
import { listWarehouses } from "@/lib/api/warehouse";

export const dynamic = "force-dynamic";

export default async function WarehousesPage() {
  let rows: Awaited<ReturnType<typeof listWarehouses>> = [];
  let error: string | null = null;
  try {
    rows = await listWarehouses();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader title="Warehouses" description="Storage facilities and locator hierarchy." />
      <Card>
        <CardContent className="p-0">
          {error ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load warehouses: {error}
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>Code</TH>
                  <TH>Name</TH>
                  <TH>City</TH>
                  <TH className="text-right">Locators</TH>
                  <TH>Status</TH>
                </TR>
              </THead>
              <TBody>
                {rows.length === 0 ? (
                  <EmptyRow colSpan={5} message="No warehouses found." />
                ) : (
                  rows.map((w) => (
                    <TR key={w.id}>
                      <TD className="font-mono text-xs">{w.code}</TD>
                      <TD>
                        <Link
                          href={`/admin/warehouses/${w.id}`}
                          className="font-medium text-blue-700 hover:underline"
                        >
                          {w.name}
                        </Link>
                      </TD>
                      <TD className="text-slate-600">
                        {w.address?.city ?? "—"}
                        {w.address?.region ? `, ${w.address.region}` : ""}
                      </TD>
                      <TD className="text-right tabular-nums">
                        {w.locators.length}
                      </TD>
                      <TD>
                        {w.active ? (
                          <Badge tone="bg-emerald-50 text-emerald-700 border-emerald-200">
                            Active
                          </Badge>
                        ) : (
                          <Badge tone="bg-slate-100 text-slate-600 border-slate-200">
                            Inactive
                          </Badge>
                        )}
                      </TD>
                    </TR>
                  ))
                )}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
