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
import { listProducts } from "@/lib/api/inventory";
import { formatCurrency } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function ProductsPage() {
  let rows: Awaited<ReturnType<typeof listProducts>> = [];
  let error: string | null = null;
  try {
    rows = await listProducts({});
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader title="Products" description="Item master." />
      <Card>
        <CardContent className="p-0">
          {error ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load products: {error}
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>SKU</TH>
                  <TH>Name</TH>
                  <TH>Category</TH>
                  <TH>UOM</TH>
                  <TH className="text-right">List price</TH>
                  <TH>Status</TH>
                </TR>
              </THead>
              <TBody>
                {rows.length === 0 ? (
                  <EmptyRow colSpan={6} message="No products found." />
                ) : (
                  rows.map((p) => (
                    <TR key={p.id}>
                      <TD className="font-mono text-xs">{p.sku}</TD>
                      <TD>
                        <Link
                          href={`/products/${p.id}`}
                          className="font-medium text-blue-700 hover:underline"
                        >
                          {p.name}
                        </Link>
                        {p.description ? (
                          <div className="text-xs text-slate-500">
                            {p.description}
                          </div>
                        ) : null}
                      </TD>
                      <TD className="text-slate-600">
                        {p.categoryName ?? "—"}
                      </TD>
                      <TD className="text-slate-600">{p.uom}</TD>
                      <TD className="text-right tabular-nums">
                        {formatCurrency(p.listPrice)}
                      </TD>
                      <TD>
                        {p.active ? (
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
