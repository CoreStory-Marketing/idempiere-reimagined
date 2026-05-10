import Link from "next/link";
import { ArrowLeft, Mail, Phone, User } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { getCustomer } from "@/lib/api/customers";
import { formatCurrency, formatDate } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function CustomerDetailPage({
  params,
}: {
  params: { id: string };
}) {
  let customer: Awaited<ReturnType<typeof getCustomer>> | null = null;
  let error: string | null = null;
  try {
    customer = await getCustomer(params.id);
  } catch (err) {
    error = (err as Error).message;
  }

  if (error || !customer) {
    return (
      <div>
        <PageHeader title="Customer not found" />
        <p className="text-sm text-red-600">{error ?? "Unknown error"}</p>
        <Link
          href="/customers"
          className="mt-4 inline-block text-sm text-blue-600"
        >
          ← Back to customers
        </Link>
      </div>
    );
  }

  return (
    <div>
      <Link
        href="/customers"
        className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to customers
      </Link>
      <PageHeader
        title={customer.name}
        description={
          <span className="font-mono">
            {customer.code} · joined {formatDate(customer.createdAt)}
          </span>
        }
        actions={
          customer.active ? (
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
        <Card>
          <CardHeader>
            <CardTitle>Contact</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <div className="flex items-center gap-2">
              <Mail className="h-4 w-4 text-slate-400" />
              {customer.email ?? "—"}
            </div>
            <div className="flex items-center gap-2">
              <Phone className="h-4 w-4 text-slate-400" />
              {customer.phone ?? "—"}
            </div>
            <div className="flex justify-between pt-2 border-t border-slate-100">
              <span className="text-slate-500">Tax ID</span>
              <span className="font-mono">{customer.taxId ?? "—"}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Payment terms</span>
              <span>{customer.paymentTermsCode ?? "—"}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Credit limit</span>
              <span className="tabular-nums font-medium">
                {formatCurrency(customer.creditLimit)}
              </span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Addresses</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            {customer.addresses.length === 0 ? (
              <p className="text-slate-400">None</p>
            ) : (
              customer.addresses.map((a, i) => (
                <p key={i} className="leading-snug">
                  {a.line1}
                  {a.line2 ? (
                    <>
                      <br />
                      {a.line2}
                    </>
                  ) : null}
                  <br />
                  {a.city}, {a.region} {a.postalCode}
                  <br />
                  {a.country}
                </p>
              ))
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Contacts</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            {customer.contacts.length === 0 ? (
              <p className="text-slate-400">None</p>
            ) : (
              customer.contacts.map((c) => (
                <div key={c.id} className="flex items-center gap-2">
                  <User className="h-4 w-4 text-slate-400" />
                  <div>
                    <div className="font-medium">
                      {c.firstName} {c.lastName}
                    </div>
                    <div className="text-xs text-slate-500">
                      {c.title ?? "—"} · {c.email ?? c.phone ?? ""}
                    </div>
                  </div>
                </div>
              ))
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
