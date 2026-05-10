# SHIP-101 — Implement shipping notification flow when orders are shipped

**Type:** Feature
**Priority:** Medium
**Status:** Open
**Components:** shipping-service, notifications-service, orders-service, frontend
**Reporter:** John Bender
**Created:** 2026-05-09

## User story

As a customer who has placed and confirmed an order, I want to receive an email when my order ships, so that I have a record of the shipment and tracking information.

As an operations team, we want internal warehouse and accounting acknowledgments logged for every shipment so we have an auditable trail.

## Acceptance criteria

1. When `shipping-service` marks an order as shipped (`POST /shipments/{id}/ship`), it emits a `shipment.created` event on the `shipments.events` Artemis topic. The event payload follows `com.corestory.idempiere.common.events.ShipmentCreatedEvent` (already defined in `domain-common`).

2. `notifications-service` consumes the event and sends three notifications:
   - **Customer email** — rendered template `shipment.confirmation.customer` (using values from the shipment + order: `@customerName@`, `@orderDocumentNo@`, `@shipmentDocumentNo@`, `@carrierName@`, `@trackingNumber@`), delivered via SMTP, visible in MailHog (`http://localhost:8025`).
   - **Warehouse acknowledgment** — written to `notification_log` table with `channel=WAREHOUSE`, visible in the `/notifications` UI page, using template `shipment.warehouse.acknowledgment`.
   - **Accounting record** — written to `notification_log` with `channel=ACCOUNTING`, visible in the `/notifications` UI page, using template `shipment.accounting.record`.

3. The frontend's "Ship Order" button on `/orders/[id]` is enabled when status is `CONFIRMED`. Clicking it calls `POST /shipments/{id}/ship` via the gateway. The button is disabled (with a tooltip "Pending shipping-service implementation") for any other status, and disabled (with the same tooltip) before this story lands.

4. **Tests cover:**
   - **Happy path:** confirmed order → ship → all three notifications recorded → email visible in MailHog
   - **Failure mode:** notification send failure (e.g., MailHog down) does **not** roll back the shipment. The shipment row remains in `SHIPPED` status; the `notification_log` row is marked `FAILED` with the error message; `delivery_attempts` row is written with the failure detail.
   - **Idempotency:** retrying with the same `dedup_key` (composed of `eventId` + channel) does not create duplicate `notification_log` rows.

5. **(Optional)** `docs/design-spec.md` is updated with a one-line entry under the "Capabilities → notifications-service" section describing the new shipment-notification flow.

## Out of scope

- Customer email opt-in respect (would consult `customer.is_no_email` if it existed; the customer schema doesn't have that column yet — flag in the gap analysis but defer)
- Multi-language template rendering (single English template only for demo; the schema supports translations via `email_template_translations`, but the wiring is deferred)
- Carrier-specific tracking-number generation (placeholder string from request body is fine)
- SMS dispatch (`SmsNotificationAdapter` stays stubbed; the gap analysis should call this out as deliberately out-of-scope for this ticket)

## iDempiere parity reference (for the agent — do not modify)

iDempiere implements an analogous flow via `MInOut.completeIt()` (`org.adempiere.base/.../MInOut.java:1631`) → `MMailText` for templates → `MClient.sendEMail()` for dispatch → `X_AD_UserMail` and `MNote` for logs. The `M_InOut.SendEMail` boolean flag (verified at line 599) gates whether email is sent.

**Use this for parity reference only — do not import any iDempiere code. The legacy repo at `/Users/johnives/Downloads/Claude Context/idempiere/` is read-only.**

Query CoreStory project 457 (`mcp__corestoryProduct-Marketing-Lab__send_message`, `project_id=457`) during gap analysis for runtime-verified detail. Conversation 5276 has a pre-cached seven-category briefing if the agent prefers to resume rather than re-query from scratch.

## Implementation hints

The following gaps in the target are expected. The agent's gap-analysis skill should surface them with stable IDs:

- **DM-001** — `customers.email` not exposed on the `OrderConfirmedEvent`/`ShipmentCreatedEvent` payload. Resolution: the shipping-service should fetch the customer's primary `contacts.email` via the gateway when constructing `ShipmentCreatedEvent`. Or extend the event payload (acceptable since event versioning is in scope).
- **BL-001** — `shipping-service` has no `ShipmentService`. Build it per the existing `OrderService` pattern in `orders-service`. Use `JmsTemplate.convertAndSend()` to publish to `shipments.events` topic.
- **BL-002** — `notifications-service` has the `@JmsListener` declared on `ShipmentNotificationConsumer.onShipmentCreated()` but the body just logs and exits. Implement the three-channel fan-out using the `NotificationSender` port instances (`EmailNotificationAdapter`, `WarehouseLogAdapter`, `AccountingLogAdapter`).
- **IG-001** — `EmailNotificationAdapter.send()` throws `UnsupportedOperationException`. Implement using Spring `JavaMailSender` (already configured for MailHog SMTP in `application.yml`).
- **IG-002** — `TemplateRenderer.render()` throws `UnsupportedOperationException`. Implement `MustacheTemplateRenderer` (or simple regex-based `@variable@` substitution mirroring iDempiere's `MMailText.parseVariables()`).
- **DM-002** — `notification_log.dedup_key` is unique but the agent must compose the key correctly: recommended `<eventId>:<channel>` since events carry `eventId` (UUID).
- **RO-001** — Frontend "Ship Order" button needs to know when shipping-service is online. The recommended pattern: a `useFeatureEnabled('shipment.ship')` hook hits `/shipments/health` and enables when 200. After this story lands, the button enables.

## Demo recording note

This is the JIRA story used in the recorded `brownfield-feature-implementation` demo. The agent runs `dual-store-gap-analysis` against this story end-to-end, presents the gap report (Stop Sign 2 / HITL gate), and after John approves, lands the implementation across `shipping-service`, `notifications-service`, and the frontend.

The demo's unfakeable moment: the customer email lands in MailHog at `http://localhost:8025`, and `/notifications` page populates with three rows, in real time as the agent's code runs.
