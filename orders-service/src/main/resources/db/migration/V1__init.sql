-- orders-service Flyway baseline migration
-- iDempiere parity references: C_Order, C_OrderLine, C_BPartner, C_BPartner_Location, AD_User
--                              C_PaymentTerm, M_PriceList, M_PriceList_Version, M_ProductPrice
--                              C_Currency, C_Country, C_Region, C_TaxCategory, C_Tax, C_Incoterms

CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    iso_code VARCHAR(3) NOT NULL UNIQUE,
    symbol VARCHAR(8) NOT NULL,
    precision_digits SMALLINT NOT NULL DEFAULT 2,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    iso_code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    default_currency_id BIGINT REFERENCES currencies(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES countries(id),
    code VARCHAR(8) NOT NULL,
    name VARCHAR(128) NOT NULL,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (country_id, code)
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    name2 VARCHAR(255),
    tax_id VARCHAR(64),
    is_customer BOOLEAN NOT NULL DEFAULT TRUE,
    is_vendor BOOLEAN NOT NULL DEFAULT FALSE,
    default_address_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    address1 VARCHAR(255) NOT NULL,
    address2 VARCHAR(255),
    city VARCHAR(128) NOT NULL,
    region_id BIGINT REFERENCES regions(id),
    country_id BIGINT NOT NULL REFERENCES countries(id),
    postal_code VARCHAR(32),
    address_type VARCHAR(16) NOT NULL DEFAULT 'BOTH',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (address_type IN ('BILLING','SHIPPING','BOTH'))
);

ALTER TABLE customers ADD CONSTRAINT fk_customers_default_address
    FOREIGN KEY (default_address_id) REFERENCES addresses(id);

CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(64),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE payment_terms (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    net_days SMALLINT NOT NULL DEFAULT 30,
    discount_pct NUMERIC(5,2) NOT NULL DEFAULT 0,
    discount_days SMALLINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE price_lists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE price_list_versions (
    id BIGSERIAL PRIMARY KEY,
    price_list_id BIGINT NOT NULL REFERENCES price_lists(id),
    valid_from DATE NOT NULL,
    valid_to DATE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE product_prices (
    id BIGSERIAL PRIMARY KEY,
    price_list_version_id BIGINT NOT NULL REFERENCES price_list_versions(id),
    product_id BIGINT NOT NULL,
    list_price NUMERIC(19,4) NOT NULL,
    std_price NUMERIC(19,4) NOT NULL,
    limit_price NUMERIC(19,4),
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (price_list_version_id, product_id)
);

CREATE TABLE tax_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE tax_rates (
    id BIGSERIAL PRIMARY KEY,
    tax_category_id BIGINT NOT NULL REFERENCES tax_categories(id),
    rate_pct NUMERIC(7,4) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    country_id BIGINT REFERENCES countries(id),
    region_id BIGINT REFERENCES regions(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE incoterms (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(8) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    bill_to_address_id BIGINT REFERENCES addresses(id),
    ship_to_address_id BIGINT REFERENCES addresses(id),
    payment_term_id BIGINT REFERENCES payment_terms(id),
    price_list_id BIGINT REFERENCES price_lists(id),
    incoterm_id BIGINT REFERENCES incoterms(id),
    currency VARCHAR(3) NOT NULL,
    total_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    grand_total NUMERIC(19,4) NOT NULL DEFAULT 0,
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    promised_date DATE,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (status IN ('DRAFT','PENDING','CONFIRMED','SHIPPED','INVOICED','COMPLETE','CANCELLED','VOIDED'))
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_date ON orders(order_date);

CREATE TABLE order_lines (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    line_no INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    qty_ordered NUMERIC(19,4) NOT NULL,
    qty_delivered NUMERIC(19,4) NOT NULL DEFAULT 0,
    qty_invoiced NUMERIC(19,4) NOT NULL DEFAULT 0,
    unit_price NUMERIC(19,4) NOT NULL,
    line_discount_pct NUMERIC(5,2) NOT NULL DEFAULT 0,
    line_amount NUMERIC(19,4) NOT NULL,
    tax_rate_id BIGINT REFERENCES tax_rates(id),
    tenant_id BIGINT NOT NULL DEFAULT 1,
    org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (order_id, line_no)
);

CREATE INDEX idx_order_lines_product ON order_lines(product_id);

CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    changed_by VARCHAR(64) NOT NULL DEFAULT 'system',
    reason VARCHAR(512)
);

CREATE INDEX idx_order_status_history_order ON order_status_history(order_id);

CREATE TABLE order_documents (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    doc_type VARCHAR(32) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    content_type VARCHAR(128),
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    uploaded_by VARCHAR(64) NOT NULL DEFAULT 'system'
);

CREATE TABLE audit_log_orders (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(16) NOT NULL,
    changed_by VARCHAR(64) NOT NULL DEFAULT 'system',
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    before_json JSONB,
    after_json JSONB
);

CREATE INDEX idx_audit_log_orders_entity ON audit_log_orders(entity_type, entity_id);
