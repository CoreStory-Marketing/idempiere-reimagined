-- shipping-service Flyway baseline (STUB — schema seeded, no service code)
-- iDempiere parity: M_InOut (shipments side), M_Package, M_Tracking, C_Freight,
--                   M_ShipmentSchedule (deferred), and the SendEMail flag (MInOut.java:599)

CREATE TABLE carriers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    scac_code VARCHAR(8),
    supports_tracking BOOLEAN NOT NULL DEFAULT TRUE,
    api_endpoint VARCHAR(255),
    requires_label BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE carrier_services (
    id BIGSERIAL PRIMARY KEY,
    carrier_id BIGINT NOT NULL REFERENCES carriers(id) ON DELETE CASCADE,
    service_code VARCHAR(32) NOT NULL,
    service_name VARCHAR(128) NOT NULL,
    transit_days_min SMALLINT,
    transit_days_max SMALLINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (carrier_id, service_code)
);

CREATE TABLE shipping_zones (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    country_id BIGINT,
    region_id BIGINT,
    postal_pattern VARCHAR(32)
);

CREATE TABLE freight_rates (
    id BIGSERIAL PRIMARY KEY,
    carrier_id BIGINT NOT NULL REFERENCES carriers(id),
    carrier_service_id BIGINT REFERENCES carrier_services(id),
    origin_country_id BIGINT,
    dest_country_id BIGINT,
    weight_min NUMERIC(19,4) NOT NULL DEFAULT 0,
    weight_max NUMERIC(19,4) NOT NULL DEFAULT 999999,
    rate_amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD'
);

CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    ship_to_address_id BIGINT NOT NULL,
    carrier_id BIGINT REFERENCES carriers(id),
    tracking_number VARCHAR(64),
    send_email_flag BOOLEAN NOT NULL DEFAULT TRUE,
    weight_total NUMERIC(19,4),
    freight_amount NUMERIC(19,4),
    ship_date TIMESTAMP WITH TIME ZONE,
    delivery_date TIMESTAMP WITH TIME ZONE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (status IN ('DRAFT','PENDING','IN_TRANSIT','SHIPPED','DELIVERED','CANCELLED'))
);

CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status);

CREATE TABLE shipment_lines (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    order_line_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    qty_shipped NUMERIC(19,4) NOT NULL,
    package_id BIGINT,
    line_no INTEGER NOT NULL,
    UNIQUE (shipment_id, line_no)
);

CREATE TABLE packages (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    package_no VARCHAR(64) NOT NULL,
    weight NUMERIC(19,4),
    length NUMERIC(19,4),
    width NUMERIC(19,4),
    height NUMERIC(19,4),
    package_type VARCHAR(32),
    UNIQUE (shipment_id, package_no)
);

CREATE TABLE tracking_events (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    event_code VARCHAR(32) NOT NULL,
    event_description VARCHAR(255),
    event_location VARCHAR(255),
    event_at TIMESTAMP WITH TIME ZONE NOT NULL,
    raw_payload_json JSONB
);

CREATE INDEX idx_tracking_events_shipment ON tracking_events(shipment_id);
