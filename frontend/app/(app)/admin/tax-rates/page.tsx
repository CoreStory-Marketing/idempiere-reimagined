import { PageHeader } from "@/components/layout/PageHeader";
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
import { listTaxCategories } from "@/lib/api/admin";
import { formatDate } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function TaxRatesPage() {
  let cats: Awaited<ReturnType<typeof listTaxCategories>> = [];
  let error: string | null = null;
  try {
    cats = await listTaxCategories();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader title="Tax rates" description="Tax categories and jurisdictional rates." />
      {error ? (
        <Card>
          <CardContent>
            <p className="text-sm text-red-600">
              Failed to load tax rates: {error}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {cats.map((cat) => (
            <Card key={cat.id}>
              <CardHeader>
                <div>
                  <CardTitle>{cat.name}</CardTitle>
                  <div className="mt-0.5 text-xs font-mono text-slate-500">
                    {cat.code}
                  </div>
                </div>
              </CardHeader>
              <Table>
                <THead>
                  <TR>
                    <TH>Name</TH>
                    <TH>Country</TH>
                    <TH>Region</TH>
                    <TH className="text-right">Rate</TH>
                    <TH>Valid from</TH>
                    <TH>Valid to</TH>
                  </TR>
                </THead>
                <TBody>
                  {cat.rates.length === 0 ? (
                    <EmptyRow
                      colSpan={6}
                      message="No rates defined in this category."
                    />
                  ) : (
                    cat.rates.map((r) => (
                      <TR key={r.id}>
                        <TD className="font-medium">{r.name}</TD>
                        <TD>{r.country}</TD>
                        <TD>{r.region ?? "—"}</TD>
                        <TD className="text-right tabular-nums">
                          {(Number(r.rate) * 100).toFixed(3)}%
                        </TD>
                        <TD className="text-slate-600">
                          {formatDate(r.validFrom)}
                        </TD>
                        <TD className="text-slate-600">
                          {r.validTo ? formatDate(r.validTo) : "—"}
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
