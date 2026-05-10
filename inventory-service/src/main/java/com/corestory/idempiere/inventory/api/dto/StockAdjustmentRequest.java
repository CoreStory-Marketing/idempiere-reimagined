package com.corestory.idempiere.inventory.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Body of {@code POST /products/{id}/stock-adjustments}.
 * {@code qtyDelta} may be negative (write-off) or positive (count correction / receipt).
 */
public record StockAdjustmentRequest(
    @NotNull Long warehouseId,
    Long locatorId,
    @NotNull BigDecimal qtyDelta,
    String reason
) {}
