# Schema digest — `orders-service`

> Lightweight ER digest. Authoritative source: `orders-service/src/main/resources/db/migration/V1__init.sql`.

## Tables

```
currencies                  iso_code, symbol, precision_digits, is_active, *audit
countries                   iso_code, name, default_currency_id → currencies, *audit
regions                     country_id → countries, code, name, *audit
customers                   document_no, name, name2, tax_id, is_customer, is_vendor,
                            default_address_id → addresses (deferred FK), *audit
addresses                   customer_id → customers, address1, address2, city, region_id,
                            country_id → countries, postal_code, address_type, *audit
contacts                    customer_id → customers, name, email, phone, is_primary, *audit
payment_terms               code, name, net_days, discount_pct, discount_days, *audit
price_lists                 name, currency, valid_from, valid_to, is_default, *audit
price_list_versions         price_list_id → price_lists, valid_from, valid_to, *audit
product_prices              price_list_version_id → price_list_versions, product_id,
                            list_price, std_price, limit_price, *audit
tax_categories              code, name, *audit
tax_rates                   tax_category_id → tax_categories, rate_pct, valid_from, valid_to,
                            country_id, region_id, *audit
incoterms                   code, name, description, *audit
orders                      document_no, status, customer_id → customers,
                            bill_to_address_id, ship_to_address_id, payment_term_id,
                            price_list_id, incoterm_id, currency, total_amount, tax_amount,
                            grand_total, order_date, promised_date, notes, *audit
order_lines                 order_id → orders, line_no, product_id, qty_ordered,
                            qty_delivered, qty_invoiced, unit_price, line_discount_pct,
                            line_amount, tax_rate_id, *audit
order_status_history        order_id → orders, from_status, to_status, changed_at,
                            changed_by, reason
order_documents             order_id → orders, doc_type, file_path, content_type,
                            uploaded_at, uploaded_by
audit_log_orders            entity_type, entity_id, action, changed_by, changed_at,
                            before_json, after_json
```

`*audit = is_active, tenant_id, org_id, created_at, updated_at, created_by, updated_by, version`

## State machine

```
DRAFT ──► CONFIRMED ──► SHIPPED ──► INVOICED ──► COMPLETE
  │           │
  └───────────┴──► CANCELLED   (also from PENDING)
```

## Constraints

- `orders.status` CHECK in (`DRAFT`, `PENDING`, `CONFIRMED`, `SHIPPED`, `INVOICED`, `COMPLETE`, `CANCELLED`, `VOIDED`).
- `addresses.address_type` CHECK in (`BILLING`, `SHIPPING`, `BOTH`).
- Unique on `orders.document_no`, `(order_lines.order_id, line_no)`, `(price_list_versions.price_list_id, valid_from)`.
- Indexes on `orders(customer_id)`, `orders(status)`, `orders(order_date)`, `order_lines(product_id)`.
