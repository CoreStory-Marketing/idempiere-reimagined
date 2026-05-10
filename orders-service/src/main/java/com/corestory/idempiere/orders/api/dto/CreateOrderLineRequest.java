package com.corestory.idempiere.orders.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderLineRequest(

    @NotNull
    Long productId,

    @NotNull
    @DecimalMin(value = "0.0001", message = "qtyOrdered must be positive")
    BigDecimal qtyOrdered,

    @NotNull
    @DecimalMin(value = "0.0000", message = "unitPrice must be non-negative")
    BigDecimal unitPrice,

    BigDecimal lineDiscountPct,

    Long taxRateId
) {}
