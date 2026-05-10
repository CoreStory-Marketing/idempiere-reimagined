package com.corestory.idempiere.orders.exception;

/**
 * Thrown when a referenced foreign-key entity cannot be resolved
 * (e.g. customer id from a {@code CreateOrderRequest} doesn't exist).
 */
public class ReferenceNotFoundException extends OrderException {

    public ReferenceNotFoundException(String entityType, Long id) {
        super("REFERENCE_NOT_FOUND", entityType + " not found: id=" + id);
    }
}
