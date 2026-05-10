package com.corestory.idempiere.inventory.api.dto;

public record UnitOfMeasureDto(
    Long id,
    String code,
    String name,
    Short stdPrecision,
    Short costingPrecision,
    boolean isDefault,
    boolean isActive
) {}
