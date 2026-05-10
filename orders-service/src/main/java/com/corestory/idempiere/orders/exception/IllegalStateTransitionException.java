package com.corestory.idempiere.orders.exception;

import com.corestory.idempiere.orders.model.OrderStatus;

/**
 * Thrown by {@code OrderService} when a caller attempts a status transition that
 * is not allowed by the configured state machine
 * ({@code DRAFT → CONFIRMED → SHIPPED → INVOICED → COMPLETE}, with
 * {@code * → CANCELLED} only allowed before SHIPPED).
 */
public class IllegalStateTransitionException extends OrderException {

    public IllegalStateTransitionException(OrderStatus from, OrderStatus to) {
        super("ILLEGAL_STATE_TRANSITION",
              "Illegal order status transition: " + from + " → " + to);
    }

    public IllegalStateTransitionException(String message) {
        super("ILLEGAL_STATE_TRANSITION", message);
    }
}
