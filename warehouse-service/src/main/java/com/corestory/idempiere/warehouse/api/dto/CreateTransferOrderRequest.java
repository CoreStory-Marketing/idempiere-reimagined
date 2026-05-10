package com.corestory.idempiere.warehouse.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateTransferOrderRequest(
    @NotBlank String documentNo,
    @NotNull Long fromWarehouseId,
    @NotNull Long toWarehouseId,
    @NotEmpty @Valid List<Line> lines
) {

    public record Line(
        @NotNull Long productId,
        @NotNull @Positive BigDecimal qty
    ) {}
}
