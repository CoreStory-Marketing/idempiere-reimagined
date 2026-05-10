package com.corestory.idempiere.orders.model;

/**
 * Address usage classification.
 *
 * <p>Aligned with {@code addresses.address_type} CHECK constraint.
 * iDempiere parity: {@code C_BPartner_Location.IsBillTo} / {@code IsShipTo} flags;
 * we collapse the boolean pair into an enum for clarity.
 */
public enum AddressType {
    BILLING,
    SHIPPING,
    BOTH
}
