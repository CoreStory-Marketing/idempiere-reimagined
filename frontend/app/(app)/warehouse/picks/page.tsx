import { PageHeader } from "@/components/layout/PageHeader";
import { PendingCard } from "@/components/ui/PendingCard";

export default function PicksPage() {
  return (
    <div>
      <PageHeader
        title="Warehouse picks"
        description="Outbound pick lists and wave management."
      />
      <PendingCard
        title="Implementation pending"
        message="Pick list management is on the warehouse-service backlog. The data model for picks exists, but the UI / API are not yet implemented. Reserved as a backup demo story."
        jiraTicket="WH-220"
      />
    </div>
  );
}
