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
import { listPriceLists } from "@/lib/api/admin";
import { formatCurrency, formatDate } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function PriceListsPage() {
  let lists: Awaited<ReturnType<typeof listPriceLists>> = [];
  let error: string | null = null;
  try {
    lists = await listPriceLists();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader
        title="Price lists"
        description="Versioned price tables per market and currency."
      />
      {error ? (
        <Card>
          <CardContent>
            <p className="text-sm text-red-600">
              Failed to load price lists: {error}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {lists.map((list) => (
            <Card key={list.id}>
              <CardHeader>
                <div>
                  <CardTitle>{list.name}</CardTitle>
                  <div className="mt-1 flex items-center gap-2 text-xs text-slate-500">
                    <span>{list.currency}</span>
                    <span>·</span>
                    <span>
                      {list.isSalesPriceList ? "Sales" : "Purchase"} price list
                    </span>
                  </div>
                </div>
              </CardHeader>
              {list.versions.map((v) => (
                <div key={v.id} className="border-t border-slate-100">
                  <div className="flex items-center justify-between px-5 py-2.5 text-xs">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-slate-700">
                        Version
                      </span>
                      <span className="font-mono">{v.id}</span>
                      <span className="text-slate-500">
                        Valid from {formatDate(v.validFrom)}
                        {v.validTo ? ` to ${formatDate(v.validTo)}` : ""}
                      </span>
                    </div>
                    {v.active ? (
                      <Badge tone="bg-emerald-50 text-emerald-700 border-emerald-200">
                        Active
                      </Badge>
                    ) : (
                      <Badge tone="bg-slate-100 text-slate-600 border-slate-200">
                        Inactive
                      </Badge>
                    )}
                  </div>
                  <Table>
                    <THead>
                      <TR>
                        <TH>SKU</TH>
                        <TH>Product</TH>
                        <TH className="text-right">List</TH>
                        <TH className="text-right">Standard</TH>
                        <TH className="text-right">Limit</TH>
                      </TR>
                    </THead>
                    <TBody>
                      {v.prices.length === 0 ? (
                        <EmptyRow
                          colSpan={5}
                          message="No prices in this version yet."
                        />
                      ) : (
                        v.prices.map((p) => (
                          <TR key={p.productId}>
                            <TD className="font-mono text-xs">{p.productSku}</TD>
                            <TD>{p.productName}</TD>
                            <TD className="text-right tabular-nums">
                              {formatCurrency(p.listPrice)}
                            </TD>
                            <TD className="text-right tabular-nums">
                              {formatCurrency(p.standardPrice)}
                            </TD>
                            <TD className="text-right tabular-nums text-slate-500">
                              {formatCurrency(p.limitPrice)}
                            </TD>
                          </TR>
                        ))
                      )}
                    </TBody>
                  </Table>
                </div>
              ))}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
