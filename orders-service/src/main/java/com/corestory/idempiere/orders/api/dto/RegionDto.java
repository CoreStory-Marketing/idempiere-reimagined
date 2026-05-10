package com.corestory.idempiere.orders.api.dto;

public record RegionDto(
    Long id,
    Long countryId,
    String code,
    String name
) {}
