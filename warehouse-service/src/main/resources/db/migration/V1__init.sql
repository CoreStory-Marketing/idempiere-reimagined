-- warehouse-service Flyway baseline
-- iDempiere parity: M_InOut (receipts side via MovementType discriminator), M_Locator,
--                   M_Warehouse, M_QualityTest, M_QualityTestResult

CREATE TABLE vendors (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    default_address_id BIGINT,
    payment_term_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    expected_date DATE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE purchase_order_lines (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    qty_ordered NUMERIC(19,4) NOT NULL,
    qty_delivered NUMERIC(19,4) NOT NULL DEFAULT 0,
    UNIQUE (purchase_order_id, product_id)
);

CREATE TABLE put_away_rules (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    locator_id BIGINT NOT NULL,
    priority SMALLINT NOT NULL DEFAULT 50
);

CREATE TABLE receipts (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    vendor_invoice_no VARCHAR(64),
    purchase_order_id BIGINT REFERENCES purchase_orders(id),
    warehouse_id BIGINT NOT NULL,
    receipt_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (status IN ('DRAFT','IN_PROGRESS','POSTED','CANCELLED'))
);

CREATE INDEX idx_receipts_status ON receipts(status);

CREATE TABLE receipt_lines (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL REFERENCES receipts(id) ON DELETE CASCADE,
    line_no INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    qty_received NUMERIC(19,4) NOT NULL,
    qty_inspected NUMERIC(19,4) NOT NULL DEFAULT 0,
    qty_accepted NUMERIC(19,4) NOT NULL DEFAULT 0,
    locator_id BIGINT,
    UNIQUE (receipt_id, line_no)
);

CREATE TABLE inspection_records (
    id BIGSERIAL PRIMARY KEY,
    receipt_line_id BIGINT NOT NULL REFERENCES receipt_lines(id) ON DELETE CASCADE,
    inspector_id BIGINT,
    status VARCHAR(8) NOT NULL DEFAULT 'PASS',
    notes VARCHAR(512),
    inspected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CHECK (status IN ('PASS','FAIL','HOLD'))
);

CREATE TABLE transfer_orders (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE transfer_order_lines (
    id BIGSERIAL PRIMARY KEY,
    transfer_order_id BIGINT NOT NULL REFERENCES transfer_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    qty NUMERIC(19,4) NOT NULL
);

-- Picking — STUBBED (schema only; controllers return 501)
CREATE TABLE picks (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    order_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE pick_lines (
    id BIGSERIAL PRIMARY KEY,
    pick_id BIGINT NOT NULL REFERENCES picks(id) ON DELETE CASCADE,
    order_line_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    qty_required NUMERIC(19,4) NOT NULL,
    qty_picked NUMERIC(19,4) NOT NULL DEFAULT 0,
    locator_id BIGINT
);

CREATE TABLE wave_picks (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    scheduled_at TIMESTAMP WITH TIME ZONE
);
