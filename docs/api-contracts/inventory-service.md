# API contract — `inventory-service`

> Build state: **FULL.** Reservation logic + scheduled expiry implemented.

Base URL: `http://localhost:8080` (via gateway), or `http://localhost:8082` direct.

## Endpoints

### Stock

#### `GET /products/{id}/stock` — stock view across warehouses
Response 200:
```json
{
  "productId": 7001,
  "sku": "SKU-7001",
  "totals": {"qtyOnHand": 250, "qtyReserved": 18, "qtyAvailable": 232},
  "byWarehouse": [
    {"warehouseId": 1, "warehouseCode": "WH-MAIN", "qtyOnHand": 200, "qtyReserved": 12,
     "byLocator": [{"locatorId": 11, "code": "A-1-1", "qtyOnHand": 100, "qtyReserved": 5}]}
  ]
}
```

#### `POST /products/{id}/stock-adjustments` — adjustment posting
Request:
```json
{"warehouseId": 1, "locatorId": 11, "qtyDelta": -3, "reason": "Damaged"}
```
Side effect: `stock_movements` ledger entry (`movement_type=ADJUSTMENT`).

### Reservations

#### `POST /reservations` — manual reservation (internal use)
Request:
```json
{"productId": 7001, "qty": 5, "orderId": 100, "orderLineId": 1, "warehouseId": 1, "ttlHours": 24}
```
Response 201:
```json
{"id": 9001, "productId": 7001, "qty": 5, "warehouseId": 1, "locatorId": 11,
 "expiresAt": "2026-05-10T15:00:00Z", "status": "ACTIVE"}
```
Errors: 409 `INSUFFICIENT_STOCK` when qty exceeds available.

**Note:** in normal operation, reservations are created automatically by the `OrderConfirmedEvent` listener — this endpoint is for ops/testing.

#### `DELETE /reservations/{id}` — cancel reservation
Response 200. Decrements `qty_reserved` on the corresponding `stock_levels`.

### Reference data

| Endpoint | Notes |
|---|---|
| `GET /products`, `GET /products/{id}` | Paged, filterable by category, sku |
| `POST /products` | Admin create |
| `GET /warehouses`, `GET /warehouses/{id}` | Includes locators |
| `GET /locators`, `POST /locators` | |
| `GET /uoms` | Units of measure |
| `GET /attribute-sets`, `GET /attribute-sets/{id}` | |
| `GET /product-categories` | Tree-flat list |
| `GET /stock-movements` | Append-only ledger view, filterable by date/product |
| `GET /inventory-counts`, `POST /inventory-counts` | Cycle counts |

## Events consumed

| Event | Topic | Behavior |
|---|---|---|
| `OrderConfirmedEvent` | `orders.events` | For each line: locate stock by locator priority, decrement `qty_on_hand`, create `Reservation`, emit `InventoryReservedEvent`. Throws `InsufficientStockException` (logged + dead-letter) if qty unavailable. |

## Events emitted

| Event | Topic | When |
|---|---|---|
| `InventoryReservedEvent` | `inventory.events` | After all lines of an OrderConfirmedEvent are reserved |

## Scheduled jobs

- **Reservation expiry** — cron `0 */15 * * * *` (every 15 minutes). Finds `ACTIVE` reservations where `expires_at < NOW()`, sets `status=EXPIRED`, decrements `qty_reserved`, writes a release `stock_movements` entry. Configurable via `idempiere.reservation.expiry-job-cron`.

## Constraints

- Optimistic locking on `stock_levels` and `reservations` via `@Version`.
- TTL default 24h, configurable via `idempiere.reservation.ttl-hours`.
