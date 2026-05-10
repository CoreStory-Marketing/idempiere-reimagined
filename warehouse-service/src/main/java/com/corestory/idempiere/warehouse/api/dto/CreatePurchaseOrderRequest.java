package com.corestory.idempiere.warehouse.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseOrderRequest(
    @NotBlank String documentNo,
    @NotNull Long vendorId,
    LocalDate expectedDate,
    @NotEmpty @Valid List<Line> lines
) {

    public record Line(
        @NotNull Long productId,
        @NotNull @Positive BigDecimal qtyOrdered
    ) {}
}
