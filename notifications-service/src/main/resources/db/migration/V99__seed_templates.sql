-- Seed templates for the recorded brownfield-feature-implementation demo (SHIP-101)
-- and a few generic ones for richness. iDempiere parity: rows in R_MailText.

INSERT INTO notification_groups (code, name, description) VALUES
    ('customer-events',    'Customer event notifications',   'Order confirmations, shipment notices, password resets'),
    ('warehouse-events',   'Warehouse team notifications',   'Shipment dock-out alerts and put-away tasks'),
    ('accounting-events',  'Accounting team notifications',  'Freight cost and invoice posting alerts'),
    ('system-alerts',      'System alerts',                  'Health and integration failures');

-- The three demo templates that SHIP-101 exercises end-to-end:
INSERT INTO email_templates (code, subject_template, body_template, body_format, language, is_active) VALUES
    ('shipment.confirmation.customer',
     'Your shipment @shipmentDocumentNo@ has shipped',
     'Hi @customerName@,

Your order @orderDocumentNo@ has shipped via @carrierName@. Tracking: @trackingNumber@

Thanks!',
     'TEXT', 'en', TRUE),

    ('shipment.warehouse.acknowledgment',
     '[Warehouse] Shipment @shipmentDocumentNo@ left dock',
     'Shipment @shipmentDocumentNo@ for order @orderDocumentNo@ is now IN_TRANSIT.',
     'TEXT', 'en', TRUE),

    ('shipment.accounting.record',
     '[Accounting] Shipment @shipmentDocumentNo@ — freight @freightAmount@',
     'Customer: @customerName@
Order: @orderDocumentNo@
Freight: @freightAmount@',
     'TEXT', 'en', TRUE);

-- Generic templates for richness (not exercised by SHIP-101 but populate the templates page)
INSERT INTO email_templates (code, subject_template, body_template, body_format, language, is_active) VALUES
    ('order.confirmation',
     'Order @orderDocumentNo@ received',
     'Hi @customerName@,

We have received your order @orderDocumentNo@ totaling @orderTotal@.

We will send another email when it ships.

Thanks for your business!',
     'TEXT', 'en', TRUE),

    ('order.cancelled',
     'Order @orderDocumentNo@ cancelled',
     'Hi @customerName@,

Your order @orderDocumentNo@ has been cancelled. If this was unexpected, please reply to this email.',
     'TEXT', 'en', TRUE),

    ('password.reset',
     'Reset your password',
     'A password reset was requested for your account.

Click here to reset: @resetLink@

This link expires in 24 hours.',
     'TEXT', 'en', TRUE),

    ('account.welcome',
     'Welcome to iDempiere Reimagined',
     'Hi @customerName@,

Welcome aboard! Your account is ready at @loginUrl@.',
     'TEXT', 'en', TRUE),

    ('invoice.posted',
     'Invoice @invoiceDocumentNo@ posted',
     'Hi @customerName@,

Invoice @invoiceDocumentNo@ for order @orderDocumentNo@ has been posted. Amount: @invoiceTotal@.',
     'TEXT', 'en', TRUE);
