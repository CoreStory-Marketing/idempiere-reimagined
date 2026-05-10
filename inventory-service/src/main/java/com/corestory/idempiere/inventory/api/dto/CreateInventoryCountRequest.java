package com.corestory.idempiere.inventory.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateInventoryCountRequest(
    @NotNull Long warehouseId,
    LocalDate countDate
) {}
