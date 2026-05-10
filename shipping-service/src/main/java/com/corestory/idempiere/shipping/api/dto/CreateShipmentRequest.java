package com.corestory.idempiere.shipping.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for {@code POST /shipments}. The controller method is currently a 501 stub —
 * the recorded brownfield-feature-implementation demo (SHIP-101) implements the handler.
 */
public record CreateShipmentRequest(
    @NotNull Long orderId,
    @NotNull Long customerId,
    @NotNull Long shipToAddressId,
    Long carrierId,
    Boolean sendEmailFlag,
    BigDecimal weightTotal,
    BigDecimal freightAmount,
    @NotEmpty @Valid List<Line> lines
) {

    public record Line(
        @NotNull Long orderLineId,
        @NotNull Long productId,
        @NotNull @Positive BigDecimal qtyShipped
    ) {}
}
