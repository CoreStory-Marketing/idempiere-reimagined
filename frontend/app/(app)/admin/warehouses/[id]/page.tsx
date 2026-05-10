import Link from "next/link";
import { ArrowLeft } from "lucide-react";
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
import { getWarehouse } from "@/lib/api/warehouse";

export const dynamic = "force-dynamic";

export default async function WarehouseDetailPage({
  params,
}: {
  params: { id: string };
}) {
  let wh: Awaited<ReturnType<typeof getWarehouse>> | null = null;
  let error: string | null = null;
  try {
    wh = await getWarehouse(params.id);
  } catch (err) {
    error = (err as Error).message;
  }

  if (error || !wh) {
    return (
      <div>
        <PageHeader title="Warehouse not found" />
        <p className="text-sm text-red-600">{error ?? "Unknown error"}</p>
        <Link
          href="/admin/warehouses"
          className="mt-4 inline-block text-sm text-blue-600"
        >
          ← Back to warehouses
        </Link>
      </div>
    );
  }

  return (
    <div>
      <Link
        href="/admin/warehouses"
        className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to warehouses
      </Link>
      <PageHeader
        title={wh.name}
        description={<span className="font-mono">{wh.code}</span>}
        actions={
          wh.active ? (
            <Badge tone="bg-emerald-50 text-emerald-700 border-emerald-200">
              Active
            </Badge>
          ) : (
            <Badge tone="bg-slate-100 text-slate-600 border-slate-200">
              Inactive
            </Badge>
          )
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle>Address</CardTitle>
          </CardHeader>
          <CardContent>
            {wh.address ? (
              <p className="text-sm text-slate-700 leading-snug">
                {wh.address.line1}
                {wh.address.line2 ? (
                  <>
                    <br />
                    {wh.address.line2}
                  </>
                ) : null}
                <br />
                {wh.address.city}, {wh.address.region} {wh.address.postalCode}
                <br />
                {wh.address.country}
              </p>
            ) : (
              <p className="text-sm text-slate-400">—</p>
            )}
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Locators</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <THead>
                <TR>
                  <TH>Code</TH>
                  <TH>Aisle</TH>
                  <TH>Bin</TH>
                  <TH>Level</TH>
                  <TH>Status</TH>
                </TR>
              </THead>
              <TBody>
                {wh.locators.length === 0 ? (
                  <EmptyRow colSpan={5} message="No locators configured." />
                ) : (
                  wh.locators.map((l) => (
                    <TR key={l.id}>
                      <TD className="font-mono text-xs">{l.code}</TD>
                      <TD>{l.aisle ?? "—"}</TD>
                      <TD>{l.bin ?? "—"}</TD>
                      <TD>{l.level ?? "—"}</TD>
                      <TD>
                        {l.active ? (
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
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
