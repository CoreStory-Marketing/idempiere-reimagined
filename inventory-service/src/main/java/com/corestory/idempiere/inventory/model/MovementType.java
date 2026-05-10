package com.corestory.idempiere.inventory.model;

/**
 * Categorizes a {@link StockMovement} ledger entry. iDempiere parity: {@code M_Transaction.MovementType}.
 *
 * <p>Sign convention: a SHIPMENT or ADJUSTMENT-out is recorded as a negative qty;
 * RECEIPT and ADJUSTMENT-in are positive. TRANSFER is two paired ledger rows
 * (out from {@code from_locator_id}, in to {@code to_locator_id}).
 */
public enum MovementType {
    RECEIPT,
    SHIPMENT,
    ADJUSTMENT,
    TRANSFER
}
