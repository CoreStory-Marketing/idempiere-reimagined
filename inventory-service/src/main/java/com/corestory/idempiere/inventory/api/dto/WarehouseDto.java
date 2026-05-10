package com.corestory.idempiere.inventory.api.dto;

public record WarehouseDto(
    Long id,
    String code,
    String name,
    Long addressId,
    boolean isActive
) {}
