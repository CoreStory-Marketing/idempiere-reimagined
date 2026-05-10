# Schema digest — `warehouse-service`

> Authoritative source: `warehouse-service/src/main/resources/db/migration/V1__init.sql`.

## Tables — receiving (FULL)

```
vendors                     document_no, name, default_address_id, payment_term_id, *audit
purchase_orders             document_no, vendor_id → vendors, status, expected_date, *audit
purchase_order_lines        purchase_order_id → purchase_orders, product_id,
                            qty_ordered, qty_delivered
put_away_rules              product_id, warehouse_id, locator_id, priority
receipts                    document_no, status, vendor_id → vendors, vendor_invoice_no,
                            purchase_order_id, warehouse_id, receipt_date, notes, *audit
receipt_lines               receipt_id → receipts, line_no, product_id, qty_received,
                            qty_inspected, qty_accepted, locator_id
inspection_records          receipt_line_id → receipt_lines, inspector_id, status, notes,
                            inspected_at
transfer_orders             document_no, status, from_warehouse_id, to_warehouse_id, *audit
transfer_order_lines        transfer_order_id → transfer_orders, product_id, qty
```

## Tables — picking (STUBBED — no service logic)

```
picks                       document_no, status, order_id, warehouse_id, *audit
pick_lines                  pick_id → picks, order_line_id, product_id,
                            qty_required, qty_picked, locator_id
wave_picks                  status, scheduled_at
```

## Receipt lifecycle

```
DRAFT ──► IN_PROGRESS ──► POSTED  (publishes ReceiptPostedEvent)
   │            │
   └────────────┴──► CANCELLED
```

## iDempiere parity note

`MInOut` in legacy iDempiere is bi-modal — receipts and shipments share one table with a `MovementType` discriminator. **This repo splits them:** receipts here, shipments in `shipping-service`. This is a deliberate modernization decision to eliminate the discriminator-table anti-pattern. The `dual-store-gap-analysis` skill should surface this as a clean modernization narrative when comparing data models.
