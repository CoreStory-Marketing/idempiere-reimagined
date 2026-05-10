package com.corestory.idempiere.warehouse.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateReceiptRequest(
    @NotNull Long vendorId,
    Long purchaseOrderId,
    String vendorInvoiceNo,
    @NotNull Long warehouseId,
    LocalDate receiptDate,
    String notes,
    @NotEmpty @Valid List<Line> lines
) {

    public record Line(
        @NotNull Long productId,
        @NotNull @Positive BigDecimal qtyReceived,
        Long locatorId
    ) {}
}
