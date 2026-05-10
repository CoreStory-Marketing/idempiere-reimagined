import { BarChart3 } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardContent } from "@/components/ui/Card";

export default function ReportsPage() {
  return (
    <div>
      <PageHeader title="Reports" description="Analytics and operational reports." />
      <Card>
        <CardContent className="flex flex-col items-center justify-center py-16 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-400">
            <BarChart3 className="h-6 w-6" aria-hidden />
          </div>
          <h2 className="mt-4 text-lg font-semibold text-slate-900">
            Coming soon
          </h2>
          <p className="mt-1 max-w-md text-sm text-slate-500">
            Operational reports (orders backlog, inventory turn, fill rate,
            on-time delivery) will land after the demo arc is recorded.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
