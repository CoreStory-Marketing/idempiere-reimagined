package com.corestory.idempiere.warehouse.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * <b>STUBBED.</b> Accepted by {@link com.corestory.idempiere.warehouse.api.PickController}
 * but the controller throws 501. The recorded brownfield-feature-implementation demo wires
 * picking up.
 */
public record PickRequestDto(
    @NotNull Long orderId,
    @NotNull Long warehouseId,
    @NotEmpty List<Line> lines
) {

    public record Line(
        @NotNull Long orderLineId,
        @NotNull Long productId,
        @NotNull @Positive BigDecimal qtyRequired,
        Long locatorId
    ) {}
}
