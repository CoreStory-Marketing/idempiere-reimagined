package com.corestory.idempiere.orders.api.dto;

/**
 * Request body for {@code POST /orders/{id}/confirm}. Both fields optional —
 * {@code reason} is recorded on {@code OrderStatusHistory} and {@code idempotencyKey}
 * is reserved for future client-side retry safety (currently informational).
 */
public record ConfirmOrderRequest(
    String reason,
    String idempotencyKey
) {}
