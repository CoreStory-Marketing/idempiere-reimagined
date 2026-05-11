-- V99 demo seed — populates orders-service with reference data + two CONFIRMED orders
-- so the recording's UI moneymaker beat has data to click on. Internal demo seed only.

-- Reference data
INSERT INTO currencies (id, iso_code, symbol, precision_digits) VALUES (1, 'USD', '$', 2);
SELECT setval('currencies_id_seq', 1, true);

INSERT INTO countries (id, iso_code, name, default_currency_id) VALUES (1, 'US', 'United States', 1);
SELECT setval('countries_id_seq', 1, true);

INSERT INTO regions (id, country_id, code, name) VALUES (1, 1, 'CA', 'California');
SELECT setval('regions_id_seq', 1, true);

INSERT INTO payment_terms (id, code, name, net_days) VALUES (1, 'NET30', 'Net 30 Days', 30);
SELECT setval('payment_terms_id_seq', 1, true);

INSERT INTO price_lists (id, name, currency, valid_from, is_default)
VALUES (1, 'Standard USD', 'USD', '2026-01-01', TRUE);
SELECT setval('price_lists_id_seq', 1, true);

INSERT INTO incoterms (id, code, name, description)
VALUES (1, 'FCA', 'Free Carrier', 'Seller delivers goods to carrier nominated by buyer');
SELECT setval('incoterms_id_seq', 1, true);

-- Customers
INSERT INTO customers (id, document_no, name, name2, tax_id, is_customer) VALUES
  (1, 'BP-DEMO-001', 'Acme Logistics Inc', 'West Coast Hub', '12-3456789', TRUE),
  (2, 'BP-DEMO-002', 'Pacific Freight Co', NULL, '98-7654321', TRUE);
SELECT setval('customers_id_seq', 2, true);

-- Addresses (BOTH-type so they serve as both ship_to and bill_to)
INSERT INTO addresses (id, customer_id, address1, city, region_id, country_id, postal_code, address_type) VALUES
  (1, 1, '123 Industrial Park Way', 'Oakland', 1, 1, '94607', 'BOTH'),
  (2, 2, '500 Harbor Blvd', 'Long Beach', 1, 1, '90802', 'BOTH');
SELECT setval('addresses_id_seq', 2, true);

UPDATE customers SET default_address_id = 1 WHERE id = 1;
UPDATE customers SET default_address_id = 2 WHERE id = 2;

-- Contacts — primary contact with email (critical for SHIP-101 customer-email lookup)
INSERT INTO contacts (id, customer_id, name, email, phone, is_primary) VALUES
  (1, 1, 'Jane Operator', 'jane@acme-logistics.example.com', '+1-510-555-0101', TRUE),
  (2, 2, 'Bill Dispatcher', 'bill@pacific-freight.example.com', '+1-562-555-0202', TRUE);
SELECT setval('contacts_id_seq', 2, true);

-- Two CONFIRMED demo orders, ready for the "Ship Order" button
INSERT INTO orders (
    id, document_no, status, customer_id, bill_to_address_id, ship_to_address_id,
    payment_term_id, price_list_id, incoterm_id, currency,
    total_amount, tax_amount, grand_total, order_date, promised_date, notes
) VALUES
  (1, 'SO-DEMO-001', 'CONFIRMED', 1, 1, 1, 1, 1, 1, 'USD',
   2400.00, 240.00, 2640.00, '2026-05-08', '2026-05-15',
   'Demo order — ready to ship via shipping-service'),
  (2, 'SO-DEMO-002', 'CONFIRMED', 2, 2, 2, 1, 1, 1, 'USD',
   1800.00, 180.00, 1980.00, '2026-05-09', '2026-05-16',
   'Backup demo order');
SELECT setval('orders_id_seq', 2, true);

-- Order lines (product_id is just a BIGINT here; products live in inventory-service)
INSERT INTO order_lines (id, order_id, line_no, product_id, qty_ordered, qty_delivered, unit_price, line_amount) VALUES
  (1, 1, 10, 1001, 4, 0, 600.00, 2400.00),
  (2, 2, 10, 1001, 3, 0, 600.00, 1800.00);
SELECT setval('order_lines_id_seq', 2, true);
