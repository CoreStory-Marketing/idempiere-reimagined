# DESIGN_SPEC — `idempiere-reimagined`

> Application-level design spec. Structured to anticipate the seven-category sweep used by the `dual-store-gap-analysis` skill.

**Last updated:** 2026-05-09
**Repo:** `github.com/CoreStory-Marketing/idempiere-reimagined`
**License:** Apache 2.0
**Stack:** Spring Boot 3.4 + Apache Artemis (JMS) + Postgres-per-service + Spring Cloud Gateway + Next.js 14 + Docker Compose

## 1. Capabilities

### orders-service — FULL
- Order CRUD with validation (lines, addresses, terms)
- Order state machine: `DRAFT → CONFIRMED → SHIPPED → INVOICED → COMPLETE`, with `* → CANCELLED` allowed pre-`SHIPPED`
- Pricing: line amount = qty × unit_price × (1 - line_discount_pct/100), tax via `tax_rates` lookup, grand_total = subtotal + tax
- Reference data: customers, addresses, contacts, payment_terms, price_lists/versions/product_prices, currencies, countries, regions, incoterms, tax_categories, tax_rates
- Event emission: `OrderConfirmedEvent`, `OrderCancelledEvent`, `OrderShippedEvent`, `OrderInvoicedEvent`, `OrderCompletedEvent` on Artemis topic `orders.events`
- Audit log table + status history table
- ~18 entities

### inventory-service — FULL
- Stock-level view per product/warehouse/locator
- Reservations with TTL (24h default, configurable)
- Background scheduler releases expired reservations (default cron: every 15 min)
- Locator-priority allocation strategy
- Attribute sets, lots, serial numbers
- UoM + UoM conversions
- Stock-movements ledger (append-only)
- Inventory counts (cycle count workflow)
- Replenishment rules
- Cost history (STD/AVG/FIFO/LIFO)
- **Listens for** `OrderConfirmedEvent` on `orders.events`
- **Emits** `InventoryReservedEvent` on `inventory.events`
- ~22 entities

### warehouse-service — HALF
- **Receiving works end-to-end:** create receipt, post to inventory, inspect lines
  - `POST /receipts`, `GET /receipts`, `POST /receipts/{id}/post`, `POST /receipts/{lineId}/inspect`
  - Emits `ReceiptPostedEvent` on `warehouse.events`
- **Picking is stubbed:** controller declared, `POST /picks` returns 501. No service-layer impl.
- **No event consumer for `OrderConfirmedEvent` yet** (this is a deliberate gap surfaced by the `INV-202` / `ORD-303` backup stories)
- ~12 entities (8 receiving-real, 4 picking-stubbed)

### shipping-service — STUB
- Schema, controllers, DTOs, repository interfaces only
- All endpoints return 501 Not Implemented
- `ShipmentCreatedEvent` payload is **defined in `domain-common`** (this is what the agent emits during the recorded demo)
- Carrier integration adapter interfaces (`CarrierClient`, `LabelGenerator`, `RateQuoter`) — declared, no implementations
- ~8 entities (populated with seed data only)
- **The recorded `brownfield-feature-implementation` demo fills in the `POST /shipments/{id}/ship` flow + `ShipmentCreatedEvent` emission**

### notifications-service — STUB
- Multi-channel notification dispatch from event bus
- `NotificationController` — `GET /notifications` returns 501; `GET /notifications/log` returns paged log entries
- `NotificationSender` port + 5 adapter stubs (Email/InApp/Warehouse/Accounting/Sms — all throw `UnsupportedOperationException`)
- `TemplateRenderer` port + `MustacheTemplateRenderer` stub
- `@JmsListener` on `ShipmentNotificationConsumer.onShipmentCreated()` is wired but the body just logs and exits
- `email_templates` table seeded with 3 demo templates + 5 generic templates
- ~9 entities (schema + seed only)
- **The recorded demo implements the email/warehouse/accounting fan-out and template rendering**

### api-gateway — FULL
- Spring Cloud Gateway routes per `application.yml`
- JWT validation filter (HS256, static signing key for demo)
- `POST /auth/login` issues JWT for hardcoded admin user
- `GET /auth/me` returns the current user
- CORS permits the frontend

## 2. Modules / services

| Service | Port | Database | Build state | Owner |
|---|---|---|---|---|
| `api-gateway` | 8080 | (none) | Full | platform |
| `orders-service` | 8081 | `orders` (5441) | Full | order team |
| `inventory-service` | 8082 | `inventory` (5442) | Full | warehouse team |
| `warehouse-service` | 8083 | `warehouse` (5443) | Half | warehouse team |
| `shipping-service` | 8084 | `shipping` (5444) | Stub | logistics team |
| `notifications-service` | 8085 | `notifications` (5445) | Stub | platform |

Apache Artemis (single broker) on `tcp://localhost:61616`, console at `:8161`. MailHog on SMTP `:1025`, web `:8025`.

## 3. APIs and contracts

Per-service summary in `docs/api-contracts/<service>.md`. Highlights:

- All endpoints accept JSON, return JSON.
- Errors return the canonical `ApiError` shape (code, message, timestamp, path, details[]).
- Idempotency keys honored on POST endpoints via `Idempotency-Key` header (recorded; replay returns same response).
- Pagination via `?page=N&pageSize=M` (defaults 0, 20).

## 4. Database tables / entities

Per-service ER digest in `docs/schema/<service>.md` (when present). Entity inventory by service:

| Service | Entity count | Notes |
|---|---|---|
| orders-service | 18 | Includes 5 reference-data entities (currency/country/region/tax/incoterm) |
| inventory-service | 22 | Rich attribute-set / lot / serial-number model |
| warehouse-service | 12 | 8 receiving + 4 picking-stubbed |
| shipping-service | 8 | All seeded; no service code |
| notifications-service | 9 | Templates seeded; adapters stubbed |

Every entity carries audit columns: `created_at`, `updated_at`, `created_by`, `updated_by`, `version` (`@Version` for optimistic locking) plus `tenant_id` and `org_id` for multi-tenancy at the schema level (no row-level isolation enforced for the demo).

## 5. Events and topics

| Topic | Producer | Payload | Consumer |
|---|---|---|---|
| `orders.events` | orders-service | `OrderConfirmedEvent`, `OrderCancelledEvent`, `OrderShippedEvent`, `OrderInvoicedEvent`, `OrderCompletedEvent` | inventory-service (confirmed/cancelled), notifications-service (post-SHIP-101) |
| `inventory.events` | inventory-service | `InventoryReservedEvent` | (none yet — placeholder for ORD-303) |
| `warehouse.events` | warehouse-service | `ReceiptPostedEvent` | (none yet) |
| `shipments.events` | shipping-service (after SHIP-101) | `ShipmentCreatedEvent` | notifications-service |
| `notifications.events` | notifications-service | (none defined yet) | (none) |

All payloads are JSON-serialized (Jackson with `jackson-datatype-jsr310`), include `eventId` (UUID, idempotency key), `occurredAt` (ISO-8601 instant), `tenantId`, `orgId`.

## 6. Integrations

- **SMTP** — Spring Mail Sender via MailHog at `mailhog:1025` for the demo. `notifications-service` reads `spring.mail.*`. `idempiere.email.from` is `no-reply@idempiere-reimagined.local`.
- **No others.** External carrier APIs, payment gateways, e-invoicing — out of scope for the demo.

## 7. Security roles

Single hardcoded admin user (`admin/admin`). JWT issued at `/auth/login`. The frontend stores the JWT in an HTTP-only cookie via a Next.js route handler, and proxies all backend calls through `/api/proxy/[...path]`.

Out of scope: real user store, RBAC, OAuth/OIDC, multi-tenant tenant resolution at the gateway.

## 8. Error handling

- Canonical `ApiError` shape (see `domain-common`).
- Each service has a `RestExceptionHandler` (`@RestControllerAdvice`).
- Validation errors → 400 with `details[]` populated per field.
- Unknown entity → 404 with `code=NOT_FOUND`.
- State-transition violations → 409 with `code=ILLEGAL_STATE_TRANSITION`.
- Insufficient stock → 409 with `code=INSUFFICIENT_STOCK`.
- Stub endpoints → 501 with `code=NOT_IMPLEMENTED`.

## 9. Deployment / runtime model

`docker compose up` brings up:

- 5 Postgres 16 instances, one per service, healthcheck-gated
- Apache Artemis 2.39 (single broker, console on :8161)
- MailHog 1.0 (SMTP :1025, web :8025)
- All 5 Spring Boot services
- API gateway
- Next.js frontend on :3000

Each Spring service uses Flyway-managed schema (`spring.jpa.hibernate.ddl-auto=validate` — Flyway migrations are the source of truth).

## 10. iDempiere parity table — the centerpiece

This table is what the `dual-store-gap-analysis` skill leans on most. Each row maps a target construct to its iDempiere parity reference, with `[unverified]` flags where the dry-run on 2026-05-09 left questions.

### orders-service parity

| Target | Legacy iDempiere | Notes |
|---|---|---|
| `OrderService.confirm()` | `MOrder.completeIt()` | Status-transition orchestration; emit event vs ModelValidator hook |
| `orders.status` | `C_Order.DocStatus` | Two-letter codes in legacy; full enum names in target |
| `customers` (with is_customer/is_vendor flags) | `C_BPartner` (universal partner table) | Same multi-role pattern |
| `currencies` / `countries` / `regions` | `C_Currency` / `C_Country` / `C_Region` | Clean-room mirror |
| `price_lists` / `price_list_versions` / `product_prices` | `M_PriceList` / `M_PriceList_Version` / `M_ProductPrice` | Same temporal versioning pattern |
| `tax_rates` / `tax_categories` | `C_Tax` / `C_TaxCategory` | |
| `incoterms` | `C_Incoterms` | |

### inventory-service parity

| Target | Legacy iDempiere | Notes |
|---|---|---|
| `stock_levels` (qty_on_hand half) | `MStorageOnHand` | Legacy splits storage into separate qty/reserved tables; we mirror |
| `reservations` | `MStorageReservation` | Same split |
| `products` / `product_categories` | `MProduct` / `MProductCategory` | |
| `attribute_sets` / `attributes` / `attribute_values` / `product_attributes` | `MAttributeSet` / `MAttribute` / `MAttributeValue` / `MAttributeInstance` | Rich attribute-instance pattern |
| `units_of_measure` / `uom_conversions` | `MUOM` / `MUOMConversion` | |
| `lots` / `serial_numbers` | `M_Lot` / `M_SerNoCtl` | |
| `warehouses` / `locators` | `M_Warehouse` / `M_Locator` | |
| `stock_movements` | `M_Transaction` | Append-only ledger pattern preserved |
| `inventory_counts` / `inventory_count_lines` | `M_Inventory` / `M_InventoryLine` | |

### warehouse-service parity

| Target | Legacy iDempiere | Notes |
|---|---|---|
| `receipts` | `MInOut` (receipts side, MovementType=`V_`) | **Decomposition decision:** legacy has a single `M_InOut` table with a MovementType discriminator. We split: receipts here, shipments in shipping-service. **Surface this in gap analysis as a clean modernization narrative.** |
| `transfer_orders` | `M_MovementType` | |
| `inspection_records` | `M_QualityTest` / `M_QualityTestResult` | Loose mapping |

### shipping-service parity

| Target | Legacy iDempiere | Notes |
|---|---|---|
| `shipments` | `MInOut` (shipments side) | Same decomposition note as above |
| `shipments.send_email_flag` | `M_InOut.SendEMail` | **Verified: `MInOut.java:599` calls `setSendEMail(false)`. Boolean flag preserved verbatim.** |
| `packages` | `M_Package` | |
| `tracking_events` | `M_Tracking` | |
| `freight_rates` | `C_Freight` | |
| `(future) shipment_schedules` | `M_ShipmentSchedule` | Out of scope for demo |

### notifications-service parity (rich — this is the demo's focus)

| Target | Legacy iDempiere | Notes |
|---|---|---|
| `EmailTemplateService` + `email_templates` table | `MMailText` / `R_MailText` | |
| `email_template_translations` | `R_MailText_Trl` | i18n parity |
| `TemplateRenderer.render()` | `MMailText.getMailText(boolean all, boolean parsed)` | Same `@variable@` substitution pattern |
| `email_outbox` (outbound log) | `X_AD_UserMail` | |
| `in_app_notifications` | `MNote` (`AD_Note`) | |
| `EmailNotificationAdapter.send()` | `MClient.sendEMail()` | |
| `application.yml` SMTP config | `MClient.SMTPHost` / `MClient.RequestEMail` | **`[unverified]` — confirm `MClient.RequestEMail` field exists** |
| `notification_subscriptions.is_subscribed` | `MUser.IsNoEMail` opt-in | **`[unverified]` — `IsNoEMail` not directly grep-visible in `MUser.java`; likely on the generated `X_AD_User` parent. Verify during build.** |
| `@JmsListener` event consumer | ModelValidator / DocAction post-completion hooks | Different paradigm; clean modernization narrative |
| (out of scope) | `AlertProcessor` / `Scheduler` (batch send) | `@Scheduled` retry job is a placeholder schema slot |

## 11. Items to verify on-the-fly during the recorded demo

If the dry-run on 2026-05-09 left `[unverified]` flags, the recorded demo can either:
- Pre-verify them via `grep` against `/Users/johnives/Downloads/Claude Context/idempiere/` (read-only)
- Surface them in the gap report as `[unverified]` and let the agent's CoreStory query confirm/deny live

The list (matches §11.3 of `Build-Scope-of-Work.md`):

```bash
# In /Users/johnives/Downloads/Claude Context/idempiere/
grep -rn "IsNoEMail" --include="*.java" org.adempiere.base/src/org/compiere/model/X_AD_User.java
grep -rn "RequestEMail" --include="*.java" org.adempiere.base/src/org/compiere/model/MClient.java
grep -rn "SMTPHost" --include="*.java" org.adempiere.base/src/org/compiere/model/MClient.java
grep -rn "sendEMailAttachments" --include="*.java" org.adempiere.base/src/org/compiere/model/MMailText.java
grep -rn "parseVariables" --include="*.java" org.adempiere.base/src/org/compiere/model/MMailText.java
grep -rn "AlertProcessor" --include="*.java" org.adempiere.server/src/main/server/
```
