# Dual-Store Gap Report — SHIP-101 (staged for the recording)

> Pre-staged so the recording shows the agent populating the target column live (no 90s round-trip dead air for legacy intel that's already verified in conversation 5276).
>
> **Legacy column:** filled from the verified seven-category briefing (CoreStory project 457, conversation 5276, dry-run 2026-05-09). See `Build-Scope-of-Work.md` §11.2 for the full transcript. Cross-referenced against source via `docs/verification-report-2026-05-09.md`.
>
> **Target column:** to be filled in by the agent during the recording, after target ingestion completes (CoreStory project ID will be filled into AGENTS.md).

**Generated:** 2026-05-09 (staged)
**Re-runs during:** the recorded `brownfield-feature-implementation` demo

## Ticket reference

`docs/jira-stories/SHIP-101-shipping-notification-flow.md` — Implement shipping notification flow when orders are shipped.

## Per-category analysis

### 1. Existing capabilities (RO)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| `MInOut.completeIt()` (`MInOut.java:1631–2209`) orchestrates shipment lifecycle including DocStatus transitions and posting (~580 lines of completion logic). Email gating via `setSendEMail()` flag at line 599. ✅ verified | _(filled by agent)_ — `shipping-service` is a STUB. `ShipmentController` exists with `POST /shipments/{id}/ship` returning 501. No `ShipmentService`. `notifications-service` has `ShipmentNotificationConsumer.onShipmentCreated()` declared as `@JmsListener` but body is empty (logs + exits). | RO-001: target lacks `ShipmentService.ship()` business logic (legacy parity: `MInOut.completeIt()`). |

### 2. Data model (DM)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| `M_InOut`, `M_InOutLine`, `M_InOutConfirm` tables. Columns: `M_InOut_ID`, `DocumentNo`, `DocStatus`, `C_BPartner_ID`, `AD_Client_ID`, `AD_Org_ID`, `IsDropShip`, **`SendEMail` boolean** | _(filled by agent)_ — `shipments` table exists with `document_no`, `status`, `order_id`, `customer_id`, `ship_to_address_id`, `carrier_id`, `tracking_number`, **`send_email_flag`**. Direct mirror of legacy `SendEMail`. `shipment_lines` mirrors `M_InOutLine`. | DM-001: `ShipmentCreatedEvent.customerEmail` field exists at `domain-common/.../ShipmentCreatedEvent.java:27` but agent must populate it from order/customer when constructing the event in shipping-service. (Earlier framing implied payload extension or cross-service fetch — neither needed.) |
| `R_MailText` (templates), `R_MailText_Trl` (translations), `AD_User` (with `EMail` field), `X_AD_UserMail` (outbound log), `AD_Note` (in-app system notes). Note: `AD_User.IsNoEMail` was claimed in the dry-run but **does not exist in source** (`docs/verification-report-2026-05-09.md`). | `email_templates`, `email_template_translations`, `email_outbox`, `in_app_notifications`, `notification_log` (channel-based), `notification_subscriptions`. Templates seeded with 3 demo templates for SHIP-101. **`notification_subscriptions.is_subscribed` is a clean modernization, not a parity mirror.** | DM-002: `notification_log.dedup_key` UNIQUE constraint exists; agent must compose key as `<eventId>:<channelCode>` to avoid duplicate sends on retries. |
| (no direct analog — legacy uses `MNote` / `X_AD_UserMail` with ad-hoc free-text error messages; no high-level failure-taxonomy column.) | _(filled by agent)_ — `delivery_attempts` (V1) has `error_code VARCHAR(64)` (transport-level) and `error_message VARCHAR(1024)`. No high-level failure category column for triage. | DM-003: SHIP-101 AC #4 (revised) requires structured `failure_code` enum on `delivery_attempts` for triage. Agent generates `notifications-service/.../V3__add_failure_code_to_delivery_attempts.sql` adding the column with `CHECK` constraint over `{SMTP_DOWN, TEMPLATE_RENDER_FAIL, INVALID_RECIPIENT, RATE_LIMITED, INTERNAL_ERROR}` + partial index on non-null. Updates `DeliveryAttempt` entity. |

### 3. UI (UI)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| `AD_Window`/`AD_Tab` metadata for `M_InOut`, `R_MailText`, `AD_User`, `AD_Note`. `WEMailDialog.java` for email composition. `AbstractADWindowContent.java`/`ADWindow.java` for window framework. | _(filled by agent)_ — Frontend has `/orders/[id]` page with **disabled** "Ship Order" button (tooltip: "Pending shipping-service implementation"). `/notifications` page exists with empty state. `/admin/email-templates` page has list view + edit, but the **preview-render endpoint returns 501**. | UI-001: "Ship Order" button needs to enable when shipping-service is online (tracked via `useFeatureEnabled('shipment.ship')` hook hitting `/shipments/health`). UI-002: `/notifications` needs to populate live during the demo (already polls `/notifications/log` every 5s). |

### 4. Business logic (BL)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| Email failure does **NOT** roll back shipment (matches AC #4). Sync send within doc-action transaction. Batch via `AlertProcessor` and `Scheduler`. ✅ `AlertProcessor` verified at `org.adempiere.server/src/main/server/org/compiere/server/AlertProcessor.java`. | _(filled by agent)_ — `notifications-service` has 5 `NotificationSender` adapters (Email, InApp, Warehouse, Accounting, Sms), all currently throwing `UnsupportedOperationException`. `ShipmentNotificationConsumer.onShipmentCreated()` is wired via `@JmsListener` but empty. | BL-001: implement `ShipmentService.ship()` per the existing `OrderService.confirm()` pattern in orders-service. State transition + JmsTemplate publish. BL-002: implement `ShipmentNotificationConsumer.onShipmentCreated()` body — three-channel fan-out using the port instances. **Failure of any channel must NOT roll back the shipment** (AC #4) — wrap each `send()` call in a try/catch, mark `notification_log.status=FAILED`, write `delivery_attempts` row. BL-003: implement `EmailNotificationAdapter.send()`, `WarehouseLogAdapter.send()`, `AccountingLogAdapter.send()`. |

### 5. Rendering (RE)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| `MMailText.getMailText(boolean all, boolean parsed)` — `@variable@` token substitution. Method actually called `parse()` (verified `MMailText.java:128, 139, 158`). HTML and plaintext both supported. `R_MailText_Trl` for translations. | _(filled by agent)_ — `TemplateRenderer` port declared in `domain-common`. `MustacheTemplateRenderer` adapter declared but `render()` throws `UnsupportedOperationException`. | RE-001: implement `MustacheTemplateRenderer.render()`. Simple regex on `@variable@` (mirroring iDempiere `parse()` semantics). Tests cover the 3 demo templates' substitution: `@customerName@`, `@orderDocumentNo@`, `@shipmentDocumentNo@`, `@carrierName@`, `@trackingNumber@`, `@freightAmount@`. |

### 6. Integration (IG)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| ModelValidator hooks, DocAction callbacks, `AlertProcessor` and `Scheduler` for batch, `MClient.sendEMail()` and `MClient.SMTPHost` for SMTP config. ✅ `MClient.RequestEMail` and `MClient.SMTPHost` verified at `MClient.java:570/603/1226`. | _(filled by agent)_ — Spring `JavaMailSender` configured for MailHog SMTP (`spring.mail.host=mailhog`, port 1025) in `notifications-service/application.yml`. Artemis JMS for cross-service eventing. `domain-common` defines events (`ShipmentCreatedEvent`, etc.). | IG-001: `EmailNotificationAdapter.send()` must use the auto-wired `JavaMailSender`. Build `SimpleMailMessage` from rendered template. Confirm via MailHog UI at `http://localhost:8025`. IG-002: `WarehouseLogAdapter.send()` writes a `notification_log` row with `channel=WAREHOUSE`. IG-003: `AccountingLogAdapter.send()` writes a `notification_log` row with `channel=ACCOUNTING`. |

### 7. Constraints (CO)

| Legacy iDempiere | Target idempiere-reimagined | Gap |
|---|---|---|
| `AD_Client_ID` / `AD_Org_ID` always required, multi-tenant tagging. `R_MailText_Trl` for multi-language. **Note:** `AD_User.IsNoEMail` opt-in was claimed but does not exist (verification-report). Multi-language template rendering exists. | _(filled by agent)_ — All entities carry `tenant_id` + `org_id` (schema-level multi-tenancy, no Hibernate `@Filter`). Templates have `language` column + `email_template_translations` table — but renderer only reads default English template for SHIP-101 (multi-language deferred). | CO-001: SHIP-101 explicitly out-of-scope: customer email opt-in (no `customers.is_no_email` column exists). Out-of-scope: multi-language rendering. Out-of-scope: SMS (`SmsNotificationAdapter` stays stubbed). |

## Gap inventory (numbered)

| ID | Category | Description | Resolution sketch | Effort |
|---|---|---|---|---|
| RO-001 | Capabilities | `shipping-service` lacks `ShipmentService.ship()` | Mirror `OrderService.confirm()` pattern | M |
| DM-001 | Data model | `ShipmentCreatedEvent.customerEmail` field exists; agent populates from order/customer | Read at event construction time in shipping-service | S |
| DM-002 | Data model | Compose `notification_log.dedup_key` correctly | `<eventId>:<channelCode>` | S |
| DM-003 | Data model | `delivery_attempts.failure_code` enum column missing | Flyway V3 migration: ADD COLUMN + `CHECK` constraint + partial index | S |
| UI-001 | UI | "Ship Order" button needs enable-when-ready | `useFeatureEnabled('shipment.ship')` hook | S |
| UI-002 | UI | `/notifications` page needs to repopulate live | Already polls every 5s — verify | S |
| BL-001 | Business logic | Implement `ShipmentService.ship()` | Pattern: `OrderService.confirm()` | M |
| BL-002 | Business logic | Implement `ShipmentNotificationConsumer.onShipmentCreated()` body | Three-channel fan-out via `NotificationSender` ports | M |
| BL-003 | Business logic | Implement 3 channel adapters: email, warehouse, accounting | Each adapter ~30 LoC | M |
| RE-001 | Rendering | Implement `MustacheTemplateRenderer.render()` | Regex `@variable@` substitution | S |
| IG-001 | Integration | `EmailNotificationAdapter` → `JavaMailSender` → MailHog | Spring auto-wired | S |
| IG-002 | Integration | `WarehouseLogAdapter` → DB row in `notification_log` | Direct insert | S |
| IG-003 | Integration | `AccountingLogAdapter` → DB row in `notification_log` | Direct insert | S |
| CO-001 | Constraints | Out-of-scope items called out: opt-in, multi-lang, SMS | Document only | S |

**Total gaps:** 14. Demo-criticals: RO-001, BL-001, BL-002, BL-003, IG-001, IG-002, IG-003, RE-001, UI-001, DM-003 (10). Background: DM-001, DM-002, UI-002, CO-001 (4).

## Implementation plan (sequenced, dependency-aware)

1. **shipping-service** — implement `ShipmentService` and lift `POST /shipments/{id}/ship` from 501.  Publishes `ShipmentCreatedEvent` to `shipments.events`. Add `/shipments/health` endpoint.
2. **domain-common** — verify `ShipmentCreatedEvent` payload supports the customer email field (already does — see `ShipmentCreatedEvent.customerEmail`).
3. **notifications-service**: implement `MustacheTemplateRenderer.render()`.
4. **notifications-service**: generate `V3__add_failure_code_to_delivery_attempts.sql` Flyway migration adding the `failure_code` enum column on `delivery_attempts` (per DM-003). Update `DeliveryAttempt` entity. Precedes adapter implementation since adapters populate `failure_code` in catch-blocks on send failure.
5. **notifications-service**: implement `EmailNotificationAdapter.send()` via `JavaMailSender`, `WarehouseLogAdapter.send()` via repo write, `AccountingLogAdapter.send()` via repo write.
6. **notifications-service**: implement `ShipmentNotificationConsumer.onShipmentCreated()` — render + send for each of three channels, write `notification_log` row, populate `failure_code` from the catch-block taxonomy, handle adapter exceptions without rolling back.
7. **frontend**: enable "Ship Order" button when `useFeatureEnabled('shipment.ship')` returns true.
8. **Tests**: integration test (Testcontainers) for the full happy-path flow; failure-mode test (mock `JavaMailSender` to throw) confirms shipment stays SHIPPED + notification_log row marked FAILED + `delivery_attempts` row written with `failure_code='SMTP_DOWN'`.
9. **DESIGN_SPEC**: one-line update to `docs/design-spec.md` § 1 (notifications-service capabilities) — "Added shipment-notification flow handler in notifications-service consuming ShipmentCreatedEvent and dispatching to email/warehouse/accounting channels with structured failure-code triage."

## Validation

- ✅ Every JIRA acceptance criterion has at least one gap addressing it.
- ✅ Every gap is closeable in target only — no legacy modification implied.
- ✅ Out-of-scope items match the JIRA ticket's "Out of scope" section.
- ✅ Failure mode (AC #4) explicitly addressed in BL-002.

## Out of scope (deliberately deferred)

- **Customer email opt-in** — would consult `customers.is_no_email`; column doesn't exist.
- **Multi-language template rendering** — schema supports it via `email_template_translations`; rendering deferred.
- **Carrier-specific tracking-number generation** — placeholder string is fine.
- **SMS dispatch** — `SmsNotificationAdapter` remains stubbed.

---

## Demo timing reference

This staged report runs the agent through the actual fan-out logic in ~3 minutes during the recording. The verbatim re-pull from conversation 5276 against project 457 takes ~10–30s (cached) for confirmation. Target column population takes ~90–120s once per-category for the first round, faster after.
