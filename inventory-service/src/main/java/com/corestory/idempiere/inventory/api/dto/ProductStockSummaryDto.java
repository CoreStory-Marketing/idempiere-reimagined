package com.corestory.idempiere.inventory.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregates per-locator {@link StockLevelDto} rows plus the summed totals for one product.
 * Returned by {@code GET /products/{id}/stock}.
 */
public record ProductStockSummaryDto(
    Long productId,
    String sku,
    BigDecimal totalOnHand,
    BigDecimal totalReserved,
    BigDecimal totalAvailable,
    List<StockLevelDto> levels
) {}
