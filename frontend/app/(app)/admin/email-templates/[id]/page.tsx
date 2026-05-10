"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, AlertTriangle } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import {
  getEmailTemplate,
  isPreviewPending,
  renderEmailTemplate,
} from "@/lib/api/admin";
import { formatDateTime } from "@/lib/utils/format";
import { ApiError } from "@/lib/api/client";

export default function EmailTemplateDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";

  const templateQuery = useQuery({
    queryKey: ["email-template", id],
    queryFn: () => getEmailTemplate(id),
    enabled: Boolean(id),
  });

  const [variables, setVariables] = useState<Record<string, string>>({});

  useEffect(() => {
    if (templateQuery.data) {
      const seed: Record<string, string> = {};
      for (const v of templateQuery.data.variables) seed[v] = "";
      setVariables(seed);
    }
  }, [templateQuery.data]);

  const previewQuery = useQuery({
    queryKey: ["email-template-render", id, variables],
    queryFn: () => renderEmailTemplate(id, variables),
    enabled: Boolean(id) && Boolean(templateQuery.data),
    retry: false,
  });

  const previewPending =
    previewQuery.isError &&
    previewQuery.error instanceof ApiError &&
    isPreviewPending(previewQuery.error);

  return (
    <div>
      <Link
        href="/admin/email-templates"
        className="mb-3 inline-flex items-center gap-1 text-xs font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to templates
      </Link>
      {templateQuery.isLoading ? (
        <Skeleton className="h-8 w-72" />
      ) : templateQuery.isError || !templateQuery.data ? (
        <p className="text-sm text-red-600">
          Failed to load template:{" "}
          {(templateQuery.error as Error)?.message ?? "Unknown"}
        </p>
      ) : (
        <>
          <PageHeader
            title={templateQuery.data.name}
            description={
              <span className="font-mono">
                {templateQuery.data.code} · updated{" "}
                {formatDateTime(templateQuery.data.updatedAt)}
              </span>
            }
          />
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Source</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">
                    Subject
                  </label>
                  <div className="rounded-md border border-slate-200 bg-slate-50 px-3 py-2 font-mono text-xs">
                    {templateQuery.data.subject}
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">
                    HTML body
                  </label>
                  <pre className="max-h-72 overflow-auto rounded-md border border-slate-200 bg-slate-50 px-3 py-2 font-mono text-xs whitespace-pre-wrap">
                    {templateQuery.data.bodyHtml}
                  </pre>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">
                    Variables
                  </label>
                  <div className="flex flex-wrap gap-1">
                    {templateQuery.data.variables.map((v) => (
                      <span
                        key={v}
                        className="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-xs font-mono text-blue-700"
                      >
                        {v}
                      </span>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Preview</CardTitle>
              </CardHeader>
              <CardContent>
                {previewQuery.isLoading ? (
                  <Skeleton className="h-32 w-full" />
                ) : previewPending ? (
                  <div className="flex items-start gap-2 rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
                    <AlertTriangle
                      className="h-4 w-4 mt-0.5 shrink-0"
                      aria-hidden
                    />
                    <div>
                      <div className="font-medium">
                        Preview pending implementation
                      </div>
                      <p className="text-xs mt-0.5 text-amber-700">
                        The template render endpoint returns 501 Not Implemented
                        until SHIP-101 lands. Source view is available above.
                      </p>
                    </div>
                  </div>
                ) : previewQuery.isError ? (
                  <p className="text-sm text-red-600">
                    {(previewQuery.error as Error).message}
                  </p>
                ) : previewQuery.data ? (
                  <div className="space-y-3">
                    <div className="rounded-md border border-slate-200 bg-white px-3 py-2 text-sm">
                      <div className="text-xs uppercase tracking-wide text-slate-500">
                        Subject
                      </div>
                      <div className="font-medium">
                        {previewQuery.data.subject}
                      </div>
                    </div>
                    <div
                      className="rounded-md border border-slate-200 bg-white px-3 py-3 text-sm prose prose-sm max-w-none"
                      // eslint-disable-next-line react/no-danger -- Trusted backend-rendered template
                      dangerouslySetInnerHTML={{
                        __html: previewQuery.data.bodyHtml,
                      }}
                    />
                  </div>
                ) : null}
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
