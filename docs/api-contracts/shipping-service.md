# API contract — `shipping-service`

> Build state: **STUB.** Schema seeded, controllers return 501. **The recorded `brownfield-feature-implementation` demo fills in the `POST /shipments/{id}/ship` flow.**

Base URL: `http://localhost:8080` (via gateway), or `http://localhost:8084` direct.

## Endpoints — ALL STUBBED (return 501)

| Endpoint | Behavior pre-SHIP-101 | Behavior post-SHIP-101 |
|---|---|---|
| `POST /shipments` | 501 | Create shipment record |
| `GET /shipments` | 501 | List paginated/filterable |
| `GET /shipments/{id}` | 501 | Get with lines + packages |
| `POST /shipments/{id}/ship` | 501 | Mark IN_TRANSIT, generate tracking, **emit `ShipmentCreatedEvent`** |
| `DELETE /shipments/{id}` | 501 | Cancel pre-IN_TRANSIT shipments |

### Carrier reference data — IMPLEMENTED (read-only)

| Endpoint | Notes |
|---|---|
| `GET /carriers`, `GET /carriers/{id}` | UPS, FedEx, DHL, USPS, generic — seeded |
| `GET /carriers/{id}/services` | Ground/Express/Overnight per carrier |

### Tracking & packages — STUBBED

| Endpoint | Behavior |
|---|---|
| `POST /packages` | 501 |
| `GET /packages` | 501 |
| `POST /tracking-events` | 501 — placeholder for carrier webhook ingest |

## What the recorded demo implements

After running `brownfield-feature-implementation` against `SHIP-101`, the agent will:

1. **Create `ShipmentService`** with `create()`, `ship()`, list/get methods
2. **Wire `ShipmentEventPublisher`** to emit `ShipmentCreatedEvent` on topic `shipments.events`
3. **Lift `POST /shipments/{id}/ship` from 501 to 200** with the actual logic
4. **Add tests:** happy path, notification-failure-doesn't-roll-back, idempotency

The `ShipmentCreatedEvent` payload is **already defined** in `domain-common/.../events/ShipmentCreatedEvent.java`. The agent reuses it.

## Events emitted (post-SHIP-101)

| Event | Topic | When |
|---|---|---|
| `ShipmentCreatedEvent` | `shipments.events` | After `POST /shipments/{id}/ship` succeeds. Consumed by notifications-service. |

## Stubbed adapters (in `carrier/` package)

- `CarrierClient` — interface declared, no implementations
- `LabelGenerator` — interface declared, no implementations
- `RateQuoter` — interface declared, no implementations

These are deliberately out of scope for SHIP-101 (the recorded demo doesn't generate real labels — placeholder tracking string is fine).
