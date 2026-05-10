package com.corestory.idempiere.common.model;

/**
 * Document lifecycle status — orders, receipts, shipments, picks all share this lifecycle.
 *
 * <p>iDempiere parity: {@code DocStatus} on every M_* table. Legacy uses two-letter codes
 * (DR/IP/CO/CL/RE/VO); we use full names since the database is per-service Postgres rather
 * than the shared Adempiere DB.
 */
public enum DocumentStatus {
    DRAFT,
    PENDING,
    IN_PROGRESS,
    CONFIRMED,
    SHIPPED,
    INVOICED,
    COMPLETE,
    CANCELLED,
    VOIDED,
    REVERSED
}
