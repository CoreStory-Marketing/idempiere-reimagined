# API contract — `orders-service`

> Build state: **FULL.** All endpoints implemented and tested.

Base URL (via gateway): `http://localhost:8080`. Direct: `http://localhost:8081`.

## Authentication

All endpoints except `/actuator/health` require `Authorization: Bearer <jwt>`. Obtain a JWT from `POST /auth/login` on the gateway.

## Endpoints

### Orders

#### `POST /orders` — create order

Request:
```json
{
  "customerId": 42,
  "billToAddressId": 101,
  "shipToAddressId": 102,
  "paymentTermId": 1,
  "priceListId": 1,
  "currency": "USD",
  "lines": [
    {"productId": 7001, "qtyOrdered": 2, "unitPrice": 49.99, "lineDiscountPct": 0, "taxRateId": 1}
  ],
  "promisedDate": "2026-05-20",
  "notes": "Rush"
}
```

Response 201:
```json
{
  "id": 1,
  "documentNo": "ORD-20260509-00001",
  "status": "DRAFT",
  "customerId": 42,
  "currency": "USD",
  "totalAmount": 99.98,
  "taxAmount": 8.00,
  "grandTotal": 107.98,
  "lines": [{"id": 1, "lineNo": 1, "productId": 7001, "qtyOrdered": 2, "unitPrice": 49.99, "lineAmount": 99.98}],
  "createdAt": "2026-05-09T15:00:00Z"
}
```

#### `GET /orders/{id}` — get order

Response 200: same shape as above.

#### `GET /orders` — list orders (paginated, filterable)

Query params: `page`, `pageSize`, `status` (multi-value), `customerId`, `documentNoLike`.

Response 200: `{"items": [...], "page": 0, "pageSize": 20, "total": 47, "totalPages": 3}`

#### `POST /orders/{id}/confirm` — transition DRAFT → CONFIRMED

Request body optional:
```json
{"reason": "Stock available, terms approved"}
```

Response 200: order with `status=CONFIRMED`. Side effects:
- `OrderStatusHistory` row written
- `OrderConfirmedEvent` published to `orders.events`

Errors:
- 409 `ILLEGAL_STATE_TRANSITION` if not DRAFT
- 422 `VALIDATION_FAILED` if required fields missing

#### `POST /orders/{id}/cancel` — transition * → CANCELLED (pre-SHIPPED only)

Request:
```json
{"reason": "Customer changed mind"}
```

Response 200. Side effects: history row + `OrderCancelledEvent`.

Errors:
- 409 `ILLEGAL_STATE_TRANSITION` if SHIPPED, INVOICED, or COMPLETE.

### Reference data (list + get only)

| Endpoint | Returns |
|---|---|
| `GET /customers`, `GET /customers/{id}` | Customer + addresses + contacts |
| `GET /addresses`, `GET /addresses/{id}` | Address |
| `GET /contacts`, `GET /contacts/{id}` | Contact |
| `GET /payment-terms`, `GET /payment-terms/{id}` | PaymentTerm |
| `GET /price-lists`, `GET /price-lists/{id}` | PriceList + versions |
| `GET /currencies`, `GET /currencies/{id}` | Currency |
| `GET /countries`, `GET /countries/{id}` | Country + regions |
| `GET /regions`, `GET /regions/{id}` | Region |
| `GET /tax-categories`, `GET /tax-categories/{id}` | TaxCategory |
| `GET /tax-rates`, `GET /tax-rates/{id}` | TaxRate |
| `GET /incoterms`, `GET /incoterms/{id}` | Incoterm |

## Idempotency

POST endpoints accept `Idempotency-Key` header. Replays return the original response.

## Errors — canonical `ApiError` shape

```json
{
  "code": "ILLEGAL_STATE_TRANSITION",
  "message": "Cannot transition order ORD-20260509-00001 from SHIPPED to DRAFT",
  "timestamp": "2026-05-09T15:00:00Z",
  "path": "/orders/1/confirm",
  "details": []
}
```

## Events emitted

| Event | Trigger | Topic |
|---|---|---|
| `OrderConfirmedEvent` | POST /orders/{id}/confirm succeeds | `orders.events` |
| `OrderCancelledEvent` | POST /orders/{id}/cancel succeeds | `orders.events` |
| `OrderShippedEvent` | service-internal transition (after shipping-service implementation) | `orders.events` |
| `OrderInvoicedEvent` | service-internal transition | `orders.events` |
| `OrderCompletedEvent` | service-internal transition | `orders.events` |

Payload shapes are defined in `domain-common/.../events/`.
