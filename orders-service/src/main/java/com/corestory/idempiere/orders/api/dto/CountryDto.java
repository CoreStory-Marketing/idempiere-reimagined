package com.corestory.idempiere.orders.api.dto;

public record CountryDto(
    Long id,
    String isoCode,
    String name,
    Long defaultCurrencyId,
    Boolean active
) {}
