-- inventory-service Flyway baseline migration
-- iDempiere parity: M_Product, M_Storage, MStorageReservation, M_Warehouse, M_Locator,
--                   M_AttributeSet, M_Attribute, M_Lot, M_SerNoCtl, M_Transaction, M_Inventory

CREATE TABLE units_of_measure (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    std_precision SMALLINT NOT NULL DEFAULT 0,
    costing_precision SMALLINT NOT NULL DEFAULT 4,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE uom_conversions (
    id BIGSERIAL PRIMARY KEY,
    from_uom_id BIGINT NOT NULL REFERENCES units_of_measure(id),
    to_uom_id BIGINT NOT NULL REFERENCES units_of_measure(id),
    multiply_rate NUMERIC(19,8),
    divide_rate NUMERIC(19,8),
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (from_uom_id, to_uom_id)
);

CREATE TABLE product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    parent_category_id BIGINT REFERENCES product_categories(id),
    is_self_service BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE product_category_paths (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES product_categories(id) ON DELETE CASCADE,
    path TEXT NOT NULL
);

CREATE TABLE attribute_sets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    mandatory_type VARCHAR(16) NOT NULL DEFAULT 'NONE',
    is_lot_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    is_serial_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (mandatory_type IN ('SERIAL','LOT','NONE'))
);

CREATE TABLE attributes (
    id BIGSERIAL PRIMARY KEY,
    attribute_set_id BIGINT NOT NULL REFERENCES attribute_sets(id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    value_type VARCHAR(16) NOT NULL DEFAULT 'STRING',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (value_type IN ('STRING','NUMBER','DATE','LIST'))
);

CREATE TABLE attribute_values (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE,
    value VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    product_category_id BIGINT REFERENCES product_categories(id),
    uom_id BIGINT REFERENCES units_of_measure(id),
    attribute_set_id BIGINT REFERENCES attribute_sets(id),
    is_stocked BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    weight NUMERIC(19,4),
    volume NUMERIC(19,4),
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(product_category_id);

CREATE TABLE product_attributes (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id),
    value VARCHAR(255),
    value_number NUMERIC(19,4),
    value_date DATE,
    UNIQUE (product_id, attribute_id)
);

CREATE TABLE lots (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    lot_number VARCHAR(64) NOT NULL,
    valid_from DATE,
    valid_to DATE,
    UNIQUE (product_id, lot_number)
);

CREATE TABLE serial_numbers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    serial_number VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
    current_locator_id BIGINT,
    UNIQUE (product_id, serial_number)
);

CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    address_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE locators (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    code VARCHAR(64) NOT NULL,
    x VARCHAR(8),
    y VARCHAR(8),
    z VARCHAR(8),
    priority_no SMALLINT NOT NULL DEFAULT 50,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (warehouse_id, code)
);

CREATE TABLE stock_levels (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    locator_id BIGINT REFERENCES locators(id),
    qty_on_hand NUMERIC(19,4) NOT NULL DEFAULT 0,
    qty_reserved NUMERIC(19,4) NOT NULL DEFAULT 0,
    qty_ordered NUMERIC(19,4) NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (product_id, warehouse_id, locator_id)
);

CREATE INDEX idx_stock_levels_product ON stock_levels(product_id);
CREATE INDEX idx_stock_levels_warehouse ON stock_levels(warehouse_id);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    qty NUMERIC(19,4) NOT NULL,
    order_id BIGINT NOT NULL,
    order_line_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    locator_id BIGINT REFERENCES locators(id),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (status IN ('ACTIVE','FULFILLED','EXPIRED','CANCELLED'))
);

CREATE INDEX idx_reservations_order ON reservations(order_id);
CREATE INDEX idx_reservations_status_expires ON reservations(status, expires_at);

CREATE TABLE replenishment_rules (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    replenish_type VARCHAR(16) NOT NULL DEFAULT 'REORDER',
    min_level NUMERIC(19,4) NOT NULL DEFAULT 0,
    max_level NUMERIC(19,4) NOT NULL DEFAULT 0,
    UNIQUE (product_id, warehouse_id),
    CHECK (replenish_type IN ('REORDER','MAX','CUSTOM'))
);

CREATE TABLE cost_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    cost_type VARCHAR(8) NOT NULL,
    cost_value NUMERIC(19,6) NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK (cost_type IN ('STD','AVG','FIFO','LIFO'))
);

CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    movement_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    movement_type VARCHAR(16) NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products(id),
    qty NUMERIC(19,4) NOT NULL,
    from_locator_id BIGINT REFERENCES locators(id),
    to_locator_id BIGINT REFERENCES locators(id),
    reference_doc_id BIGINT,
    reference_doc_type VARCHAR(32),
    CHECK (movement_type IN ('RECEIPT','SHIPMENT','ADJUSTMENT','TRANSFER'))
);

CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_date ON stock_movements(movement_date);

CREATE TABLE inventory_counts (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    count_date DATE NOT NULL DEFAULT CURRENT_DATE,
    CHECK (status IN ('DRAFT','IN_PROGRESS','COMPLETED','CANCELLED'))
);

CREATE TABLE inventory_count_lines (
    id BIGSERIAL PRIMARY KEY,
    inventory_count_id BIGINT NOT NULL REFERENCES inventory_counts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    locator_id BIGINT REFERENCES locators(id),
    qty_book NUMERIC(19,4) NOT NULL DEFAULT 0,
    qty_count NUMERIC(19,4) NOT NULL DEFAULT 0,
    variance NUMERIC(19,4) NOT NULL DEFAULT 0
);

CREATE TABLE replenishment_orders (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    qty_required NUMERIC(19,4) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_log_inventory (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(16) NOT NULL,
    changed_by VARCHAR(64) NOT NULL DEFAULT 'system',
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    before_json JSONB,
    after_json JSONB
);
