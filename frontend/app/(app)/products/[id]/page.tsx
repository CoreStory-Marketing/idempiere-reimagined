import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import { getProduct } from "@/lib/api/inventory";
import { formatCurrency } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function ProductDetailPage({
  params,
}: {
  params: { id: string };
}) {
  let product: Awaited<ReturnType<typeof getProduct>> | null = null;
  let error: string | null = null;
  try {
    product = await getProduct(params.id);
  } catch (err) {
    error = (err as Error).message;
  }

  if (error || !product) {
    return (
      <div>
        <PageHeader title="Product not found" />
        <p className="text-sm text-red-600">{error ?? "Unknown error"}</p>
        <Link
          href="/products"
          className="mt-4 inline-block text-sm text-blue-600"
        >
          ← Back to products
        </Link>
      </div>
    );
  }

  return (
    <div>
      <Link
        href="/products"
        className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to products
      </Link>
      <PageHeader
        title={product.name}
        description={<span className="font-mono">{product.sku}</span>}
        actions={
          product.active ? (
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
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-slate-500">Category</span>
              <span>{product.categoryName ?? "—"}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">UOM</span>
              <span>{product.uom}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">List price</span>
              <span className="tabular-nums">
                {formatCurrency(product.listPrice)}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Weight</span>
              <span>{product.weight ?? "—"}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Volume</span>
              <span>{product.volume ?? "—"}</span>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Description</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-slate-700">
              {product.description ?? "No description provided."}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
