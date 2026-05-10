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
import { listCustomers } from "@/lib/api/customers";
import { formatCurrency } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function CustomersPage() {
  let rows: Awaited<ReturnType<typeof listCustomers>> = [];
  let error: string | null = null;
  try {
    rows = await listCustomers();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader
        title="Customers"
        description="Business partners with sales activity."
      />
      <Card>
        <CardContent className="p-0">
          {error ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load customers: {error}
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
                  <TH className="text-right">Credit limit</TH>
                  <TH>Status</TH>
                </TR>
              </THead>
              <TBody>
                {rows.length === 0 ? (
                  <EmptyRow colSpan={7} message="No customers found." />
                ) : (
                  rows.map((c) => (
                    <TR key={c.id}>
                      <TD className="font-mono text-xs">{c.code}</TD>
                      <TD>
                        <Link
                          href={`/customers/${c.id}`}
                          className="font-medium text-blue-700 hover:underline"
                        >
                          {c.name}
                        </Link>
                      </TD>
                      <TD className="text-slate-600">{c.email ?? "—"}</TD>
                      <TD className="text-slate-600">{c.phone ?? "—"}</TD>
                      <TD className="text-slate-600">
                        {c.paymentTermsCode ?? "—"}
                      </TD>
                      <TD className="text-right tabular-nums">
                        {formatCurrency(c.creditLimit)}
                      </TD>
                      <TD>
                        {c.active ? (
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
