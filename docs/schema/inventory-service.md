# Schema digest — `inventory-service`

> Authoritative source: `inventory-service/src/main/resources/db/migration/V1__init.sql`.

## Tables

```
units_of_measure            code, name, std_precision, costing_precision, is_default, *audit
uom_conversions             from_uom_id, to_uom_id, multiply_rate, divide_rate, *audit
product_categories          name, parent_category_id (self-FK), is_self_service, *audit
product_category_paths      category_id → product_categories, path                 [denormalized hierarchy]
attribute_sets              name, mandatory_type, is_lot_mandatory, is_serial_mandatory, *audit
attributes                  attribute_set_id, name, value_type, *audit
attribute_values            attribute_id, value
products                    sku, name, description, product_category_id, uom_id,
                            attribute_set_id, is_stocked, weight, volume, *audit
product_attributes          product_id, attribute_id, value, value_number, value_date
lots                        product_id, lot_number, valid_from, valid_to
serial_numbers              product_id, serial_number, status, current_locator_id
warehouses                  code, name, address_id, *audit
locators                    warehouse_id, code, x, y, z, priority_no, is_default
stock_levels                product_id, warehouse_id, locator_id, qty_on_hand,
                            qty_reserved, qty_ordered, *audit
reservations                product_id, qty, order_id, order_line_id,
                            warehouse_id, locator_id, expires_at, status, *audit
replenishment_rules         product_id, warehouse_id, replenish_type, min_level, max_level
cost_history                product_id, cost_type, cost_value, valid_from
stock_movements             movement_date, movement_type, product_id, qty,
                            from_locator_id, to_locator_id, reference_doc_id,
                            reference_doc_type             [append-only ledger]
inventory_counts            warehouse_id, status, count_date
inventory_count_lines       inventory_count_id, product_id, locator_id, qty_book,
                            qty_count, variance
replenishment_orders        product_id, warehouse_id, qty_required, status      [stub for INV-202]
audit_log_inventory         entity_type, entity_id, action, *audit-light
```

## Reservation lifecycle

```
ACTIVE  ──┬──► FULFILLED  (when order ships)
          ├──► EXPIRED    (TTL hit, scheduler releases)
          └──► CANCELLED  (manual cancel or order cancellation)
```

`@Version` on `stock_levels` and `reservations` for optimistic locking under concurrent reservation.
