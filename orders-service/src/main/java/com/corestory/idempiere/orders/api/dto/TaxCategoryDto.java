package com.corestory.idempiere.orders.api.dto;

public record TaxCategoryDto(
    Long id,
    String code,
    String name,
    Boolean active
) {}
