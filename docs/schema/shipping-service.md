# Schema digest — `shipping-service`

> Authoritative source: `shipping-service/src/main/resources/db/migration/V1__init.sql`.

## Tables

```
carriers                    code, name, scac_code, supports_tracking, api_endpoint,
                            requires_label, *audit                       [seeded: UPS, FedEx, DHL, USPS, generic]
carrier_services            carrier_id → carriers, service_code, service_name,
                            transit_days_min, transit_days_max
shipping_zones              code, name, country_id, region_id, postal_pattern
freight_rates               carrier_id, carrier_service_id, origin_country_id, dest_country_id,
                            weight_min, weight_max, rate_amount, currency
shipments                   document_no, status, order_id, customer_id, ship_to_address_id,
                            carrier_id, tracking_number, send_email_flag, weight_total,
                            freight_amount, ship_date, delivery_date, *audit
shipment_lines              shipment_id → shipments, order_line_id, product_id,
                            qty_shipped, package_id, line_no
packages                    shipment_id → shipments, package_no, weight, length, width,
                            height, package_type
tracking_events             shipment_id → shipments, event_code, event_description,
                            event_location, event_at, raw_payload_json
```

## Shipment lifecycle (post-SHIP-101)

```
DRAFT ──► PENDING ──► IN_TRANSIT ──► SHIPPED ──► DELIVERED
   │            │
   └────────────┴──► CANCELLED
```

## iDempiere parity

`shipments` ↔ legacy `MInOut` (shipments side, MovementType=`C_`).

The `send_email_flag` column directly mirrors `M_InOut.SendEMail` — verified at `MInOut.java:599` (legacy calls `setSendEMail(false)` there). This is the canonical iDempiere precedent for gating notification dispatch on a per-document flag.

## State during the recorded demo

Pre-demo: this entire schema is **populated with seed data** (carriers, freight rates) but the `shipments` table is empty. No service-layer logic exists. All endpoints return 501.

Post-demo: the agent has implemented `ShipmentService.create()` and `ship()`. A `shipments` row is inserted, `ShipmentCreatedEvent` is published, and the demo continues into notifications-service.
