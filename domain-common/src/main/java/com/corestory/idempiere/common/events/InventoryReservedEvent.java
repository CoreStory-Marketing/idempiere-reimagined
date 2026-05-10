package com.corestory.idempiere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryReservedEvent(
    UUID eventId,
    Instant occurredAt,
    Long tenantId,
    Long orgId,
    Long orderId,
    String orderDocumentNo,
    List<Reservation> reservations
) implements DomainEvent {

    @Override
    public String eventType() {
        return "inventory.reserved";
    }

    public record Reservation(
        Long reservationId,
        Long productId,
        String sku,
        BigDecimal qty,
        Long warehouseId,
        Long locatorId,
        Instant expiresAt
    ) {}
}
