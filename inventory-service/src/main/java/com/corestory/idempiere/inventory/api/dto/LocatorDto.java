package com.corestory.idempiere.inventory.api.dto;

public record LocatorDto(
    Long id,
    Long warehouseId,
    String code,
    String x,
    String y,
    String z,
    Short priorityNo,
    boolean isDefault,
    boolean isActive
) {}
