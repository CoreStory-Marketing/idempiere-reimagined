package com.corestory.idempiere.orders.api.dto;

import java.math.BigDecimal;

public record OrderLineDto(
    Long id,
    Integer lineNo,
    Long productId,
    BigDecimal qtyOrdered,
    BigDecimal qtyDelivered,
    BigDecimal qtyInvoiced,
    BigDecimal unitPrice,
    BigDecimal lineDiscountPct,
    BigDecimal lineAmount,
    Long taxRateId
) {}
