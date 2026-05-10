package com.corestory.idempiere.inventory.api.dto;

import com.corestory.idempiere.inventory.model.MovementType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StockMovementDto(
    Long id,
    OffsetDateTime movementDate,
    MovementType movementType,
    Long productId,
    BigDecimal qty,
    Long fromLocatorId,
    Long toLocatorId,
    Long referenceDocId,
    String referenceDocType
) {}
