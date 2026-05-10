package com.corestory.idempiere.orders.api.dto;

import java.math.BigDecimal;

public record PaymentTermDto(
    Long id,
    String code,
    String name,
    Short netDays,
    BigDecimal discountPct,
    Short discountDays,
    Boolean active
) {}
