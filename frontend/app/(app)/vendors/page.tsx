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
import { listVendors } from "@/lib/api/vendors";

export const dynamic = "force-dynamic";

export default async function VendorsPage() {
  let rows: Awaited<ReturnType<typeof listVendors>> = [];
  let error: string | null = null;
  try {
    rows = await listVendors();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader title="Vendors" description="Suppliers and procurement partners." />
      <Card>
        <CardContent className="p-0">
          {error ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load vendors: {error}
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>Code</TH>
                  <TH>Name</TH>
                  <TH>Email</TH>
                  <TH>Phone</TH>
                  <TH>Terms</TH>
                  <TH>Status</TH>
                </TR>
              </THead>
              <TBody>
                {rows.length === 0 ? (
                  <EmptyRow colSpan={6} message="No vendors found." />
                ) : (
                  rows.map((v) => (
                    <TR key={v.id}>
                      <TD className="font-mono text-xs">{v.code}</TD>
                      <TD className="font-medium text-slate-800">{v.name}</TD>
                      <TD className="text-slate-600">{v.email ?? "—"}</TD>
                      <TD className="text-slate-600">{v.phone ?? "—"}</TD>
                      <TD className="text-slate-600">
                        {v.paymentTermsCode ?? "—"}
                      </TD>
                      <TD>
                        {v.active ? (
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
