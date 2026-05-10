import Link from "next/link";
import { PageHeader } from "@/components/layout/PageHeader";
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
import { listEmailTemplates } from "@/lib/api/admin";
import { formatDateTime } from "@/lib/utils/format";

export const dynamic = "force-dynamic";

export default async function EmailTemplatesPage() {
  let rows: Awaited<ReturnType<typeof listEmailTemplates>> = [];
  let error: string | null = null;
  try {
    rows = await listEmailTemplates();
  } catch (err) {
    error = (err as Error).message;
  }

  return (
    <div>
      <PageHeader
        title="Email templates"
        description="Outbound notification templates with variable substitution."
      />
      <Card>
        <CardContent className="p-0">
          {error ? (
            <div className="p-12 text-center text-sm text-red-600">
              Failed to load templates: {error}
            </div>
          ) : (
            <Table>
              <THead>
                <TR>
                  <TH>Code</TH>
                  <TH>Name</TH>
                  <TH>Subject</TH>
                  <TH>Variables</TH>
                  <TH>Updated</TH>
                </TR>
              </THead>
              <TBody>
                {rows.length === 0 ? (
                  <EmptyRow colSpan={5} message="No templates found." />
                ) : (
                  rows.map((t) => (
                    <TR key={t.id}>
                      <TD className="font-mono text-xs">{t.code}</TD>
                      <TD>
                        <Link
                          href={`/admin/email-templates/${t.id}`}
                          className="font-medium text-blue-700 hover:underline"
                        >
                          {t.name}
                        </Link>
                      </TD>
                      <TD className="max-w-[40ch] truncate text-slate-700">
                        {t.subject}
                      </TD>
                      <TD className="text-xs text-slate-500">
                        {t.variables.length}
                      </TD>
                      <TD className="text-slate-600">
                        {formatDateTime(t.updatedAt)}
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
