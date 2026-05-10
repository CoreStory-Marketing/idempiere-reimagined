package com.corestory.idempiere.shipping.model;

/**
 * Shipment lifecycle — mirrors {@code shipments.status} CHECK constraint in V1__init.sql.
 *
 * <p>iDempiere parity: {@code MInOut.DocStatus} (DR/IP/CO/RE/VO/CL).
 */
public enum ShipmentStatus {
    DRAFT,
    PENDING,
    IN_TRANSIT,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
