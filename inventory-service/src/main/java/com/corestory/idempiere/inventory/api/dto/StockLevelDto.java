package com.corestory.idempiere.inventory.api.dto;

import java.math.BigDecimal;

public record StockLevelDto(
    Long id,
    Long productId,
    Long warehouseId,
    Long locatorId,
    BigDecimal qtyOnHand,
    BigDecimal qtyReserved,
    BigDecimal qtyOrdered,
    BigDecimal qtyAvailable
) {}
