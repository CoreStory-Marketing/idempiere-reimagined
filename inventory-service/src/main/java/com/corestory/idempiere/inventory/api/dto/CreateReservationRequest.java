package com.corestory.idempiere.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateReservationRequest(
    @NotNull Long productId,
    @NotNull @Positive BigDecimal qty,
    @NotNull Long orderId,
    @NotNull Long orderLineId,
    Long warehouseId,
    String lotNumber,
    String serialNumber
) {}
