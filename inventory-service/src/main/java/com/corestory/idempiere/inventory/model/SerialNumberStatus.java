package com.corestory.idempiere.inventory.model;

/** Lifecycle status of an individual {@link SerialNumber}. */
public enum SerialNumberStatus {
    AVAILABLE,
    RESERVED,
    SHIPPED,
    RETURNED,
    SCRAPPED
}
