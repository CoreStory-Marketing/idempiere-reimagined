package com.corestory.idempiere.inventory.api.dto;

import com.corestory.idempiere.inventory.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservationDto(
    Long id,
    Long productId,
    BigDecimal qty,
    Long orderId,
    Long orderLineId,
    Long warehouseId,
    Long locatorId,
    OffsetDateTime expiresAt,
    ReservationStatus status
) {}
