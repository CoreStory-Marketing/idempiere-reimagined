package com.corestory.idempiere.inventory.model;

/** Lifecycle of a stock count. iDempiere parity: {@code M_Inventory.DocStatus}. */
public enum InventoryCountStatus {
    DRAFT,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
