package com.corestory.idempiere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderConfirmedEvent(
    UUID eventId,
    Instant occurredAt,
    Long tenantId,
    Long orgId,
    Long orderId,
    String documentNo,
    Long customerId,
    BigDecimal grandTotal,
    String currency,
    List<Line> lines
) implements DomainEvent {

    @Override
    public String eventType() {
        return "order.confirmed";
    }

    public record Line(
        Long orderLineId,
        Long productId,
        String sku,
        BigDecimal qtyOrdered,
        BigDecimal unitPrice,
        BigDecimal lineAmount
    ) {}
}
