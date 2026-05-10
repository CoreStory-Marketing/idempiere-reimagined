package com.corestory.idempiere.orders.model;

/**
 * Order document lifecycle.
 *
 * <p>Mirrors {@link com.corestory.idempiere.common.model.DocumentStatus} but is
 * scoped specifically to the orders-service state machine. Values must align with
 * the {@code CHECK (status IN ...)} constraint on {@code orders.status} in
 * {@code V1__init.sql}.
 *
 * <p>iDempiere parity: {@code C_Order.DocStatus}.
 */
public enum OrderStatus {
    DRAFT,
    PENDING,
    CONFIRMED,
    SHIPPED,
    INVOICED,
    COMPLETE,
    CANCELLED,
    VOIDED
}
