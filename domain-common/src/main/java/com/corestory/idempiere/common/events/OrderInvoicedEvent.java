package com.corestory.idempiere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderInvoicedEvent(
    UUID eventId,
    Instant occurredAt,
    Long tenantId,
    Long orgId,
    Long orderId,
    String documentNo,
    Long customerId,
    BigDecimal invoiceAmount,
    String currency
) implements DomainEvent {

    @Override
    public String eventType() {
        return "order.invoiced";
    }
}
