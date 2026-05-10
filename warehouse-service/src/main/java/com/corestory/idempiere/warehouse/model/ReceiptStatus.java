package com.corestory.idempiere.warehouse.model;

/**
 * Receipt status — mirrors {@code receipts.status} CHECK constraint in V1__init.sql.
 *
 * <p>iDempiere parity: {@code MInOut.DocStatus} (DR/IP/CO/VO).
 */
public enum ReceiptStatus {
    DRAFT,
    IN_PROGRESS,
    POSTED,
    CANCELLED
}
