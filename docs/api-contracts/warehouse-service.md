# API contract — `warehouse-service`

> Build state: **HALF.** Receiving works; picking is stubbed (501).

Base URL: `http://localhost:8080` (via gateway), or `http://localhost:8083` direct.

## Receiving — IMPLEMENTED

### `POST /receipts` — create receipt
Request:
```json
{
  "vendorId": 1,
  "vendorInvoiceNo": "VINV-2026-0001",
  "purchaseOrderId": 42,
  "warehouseId": 1,
  "lines": [{"productId": 7001, "qtyReceived": 100, "locatorId": 11}]
}
```
Response 201:
```json
{
  "id": 1,
  "documentNo": "RCT-20260509-00001",
  "status": "DRAFT",
  "vendorId": 1,
  "warehouseId": 1,
  "lines": [{"id": 1, "lineNo": 1, "productId": 7001, "qtyReceived": 100, "qtyAccepted": 0, "locatorId": 11}],
  "createdAt": "2026-05-09T15:00:00Z"
}
```

### `GET /receipts/{id}`, `GET /receipts`
Standard list/get. Filterable by status, vendorId.

### `POST /receipts/{id}/post` — commit receipt to inventory
Side effects:
- `receipts.status = POSTED`
- `ReceiptPostedEvent` published to `warehouse.events`
- (Future enhancement: inventory-service consumes `ReceiptPostedEvent` and credits stock; currently this consumption is part of the INV-202 backup story)

### `POST /receipt-lines/{lineId}/inspect` — inspection record
Request:
```json
{"inspectorId": 99, "status": "PASS", "qtyInspected": 100, "qtyAccepted": 100, "notes": ""}
```

### Reference data — IMPLEMENTED

| Endpoint | Notes |
|---|---|
| `GET /vendors`, `GET /vendors/{id}` | |
| `GET /purchase-orders`, `GET /purchase-orders/{id}` | |
| `GET /transfer-orders`, `POST /transfer-orders` | |
| `GET /locators` | (Mirror of inventory-service for cross-warehouse views) |

## Picking — STUBBED (501)

| Endpoint | Status | Behavior |
|---|---|---|
| `POST /picks` | 501 | `PickRequestDto` accepted, returns "Pick implementation pending" |
| `GET /picks` | 501 | |
| `POST /picks/{id}/release` | 501 | |

The schema is in place (`picks`, `pick_lines`, `wave_picks` tables) but no service-layer logic. The `INV-202` backup story exercises this surface.

## Events emitted

| Event | Topic | When |
|---|---|---|
| `ReceiptPostedEvent` | `warehouse.events` | After `POST /receipts/{id}/post` succeeds |
