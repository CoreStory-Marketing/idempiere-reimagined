# API contract — `notifications-service`

> Build state: **STUB.** Port + listener wired but empty. **The recorded `brownfield-feature-implementation` demo implements the email/warehouse/accounting fan-out and template rendering.**

Base URL: `http://localhost:8080` (via gateway), or `http://localhost:8085` direct.

## Endpoints

| Endpoint | Pre-SHIP-101 behavior | Post-SHIP-101 behavior |
|---|---|---|
| `GET /notifications` | 501 | Returns recent notifications across all channels |
| `GET /notifications/log` | Returns paginated `notification_log` view (real, but empty pre-demo) | Same — populated live during demo |
| `GET /notifications/log/{id}` | Real | Real |
| `GET /email-templates`, `GET /email-templates/{id}` | Real (read-only) | Real |
| `POST /email-templates/{id}/preview` | 501 (TemplateRenderer not implemented) | Renders with given context |

## Channel adapters (`NotificationSender` port)

| Adapter | `channelCode()` | Pre-SHIP-101 state | Post-SHIP-101 state |
|---|---|---|---|
| `EmailNotificationAdapter` | `EMAIL` | Throws `UnsupportedOperationException` | SMTP via `JavaMailSender` → MailHog |
| `InAppNotificationAdapter` | `IN_APP` | Throws | Inserts into `in_app_notifications` |
| `WarehouseLogAdapter` | `WAREHOUSE` | Throws | Inserts into `notification_log` with channel=WAREHOUSE |
| `AccountingLogAdapter` | `ACCOUNTING` | Throws | Inserts into `notification_log` with channel=ACCOUNTING |
| `SmsNotificationAdapter` | `SMS` | Throws | **Stays stubbed.** Out of scope for SHIP-101. |

## Template rendering

`TemplateRenderer` port (in `domain-common`):

```java
String render(String template, Map<String, Object> context);
```

Default impl `MustacheTemplateRenderer`:
- Pre-SHIP-101: throws `UnsupportedOperationException`
- Post-SHIP-101: simple `@variable@` substitution mirroring iDempiere `MMailText.parseVariables()`

## Event consumption

### `ShipmentCreatedEvent` (topic: `shipments.events`)

Consumer: `ShipmentNotificationConsumer.onShipmentCreated()`.

Pre-SHIP-101: handler logs receipt and exits.
Post-SHIP-101: handler:
1. Renders `shipment.confirmation.customer` template + sends via `EmailNotificationAdapter`
2. Renders `shipment.warehouse.acknowledgment` template + sends via `WarehouseLogAdapter`
3. Renders `shipment.accounting.record` template + sends via `AccountingLogAdapter`
4. Records each in `notification_log` with `dedup_key = "<eventId>:<channel>"` for idempotency

## Seed data

V99 migration seeds 8 templates:

- `shipment.confirmation.customer` (3-channel demo)
- `shipment.warehouse.acknowledgment`
- `shipment.accounting.record`
- `order.confirmation.customer`
- `order.cancellation.customer`
- `password.reset`
- `welcome.new-user`
- `low-stock.alert`

Plus 4 notification groups: `customer-events`, `warehouse-events`, `accounting-events`, `system-alerts`.

## Failure mode (per SHIP-101 AC #4)

If a channel adapter throws (e.g., MailHog down), the implementation must:
- Mark the `notification_log` row `status=FAILED` with `error_message`
- Record a `delivery_attempts` row
- **NOT roll back the upstream shipment.** The shipment remains `SHIPPED`.

This mirrors iDempiere's behavior (verified): email failure does not rollback the shipment doc-action.
