package com.corestory.idempiere.orders.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderRequest(

    @NotBlank(message = "reason is required when cancelling an order")
    String reason
) {}
