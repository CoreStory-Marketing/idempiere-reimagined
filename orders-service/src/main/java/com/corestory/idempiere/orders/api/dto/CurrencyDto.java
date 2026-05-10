package com.corestory.idempiere.orders.api.dto;

public record CurrencyDto(
    Long id,
    String isoCode,
    String symbol,
    Short precisionDigits,
    Boolean active
) {}
