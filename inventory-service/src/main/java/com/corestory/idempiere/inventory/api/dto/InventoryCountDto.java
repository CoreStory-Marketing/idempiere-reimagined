package com.corestory.idempiere.inventory.api.dto;

import com.corestory.idempiere.inventory.model.InventoryCountStatus;

import java.time.LocalDate;

public record InventoryCountDto(
    Long id,
    Long warehouseId,
    InventoryCountStatus status,
    LocalDate countDate
) {}
