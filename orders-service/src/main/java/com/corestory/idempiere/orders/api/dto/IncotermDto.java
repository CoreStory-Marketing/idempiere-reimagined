package com.corestory.idempiere.orders.api.dto;

public record IncotermDto(
    Long id,
    String code,
    String name,
    String description,
    Boolean active
) {}
