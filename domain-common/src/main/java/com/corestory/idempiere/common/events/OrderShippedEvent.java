package com.corestory.idempiere.common.events;

import java.time.Instant;
import java.util.UUID;

public record OrderShippedEvent(
    UUID eventId,
    Instant occurredAt,
    Long tenantId,
    Long orgId,
    Long orderId,
    String documentNo,
    Long customerId,
    Long shipmentId,
    String shipmentDocumentNo
) implements DomainEvent {

    @Override
    public String eventType() {
        return "order.shipped";
    }
}
