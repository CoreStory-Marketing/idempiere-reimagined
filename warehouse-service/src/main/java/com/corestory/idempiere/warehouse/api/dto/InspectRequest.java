package com.corestory.idempiere.warehouse.api.dto;

import com.corestory.idempiere.warehouse.model.InspectionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record InspectRequest(
    @NotNull InspectionStatus status,
    @NotNull @PositiveOrZero BigDecimal qtyInspected,
    @NotNull @PositiveOrZero BigDecimal qtyAccepted,
    Long inspectorId,
    String notes
) {}
