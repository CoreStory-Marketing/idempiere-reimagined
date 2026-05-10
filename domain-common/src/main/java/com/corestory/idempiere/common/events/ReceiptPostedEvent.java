package com.corestory.idempiere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReceiptPostedEvent(
    UUID eventId,
    Instant occurredAt,
    Long tenantId,
    Long orgId,
    Long receiptId,
    String receiptDocumentNo,
    Long warehouseId,
    Long vendorId,
    List<Line> lines
) implements DomainEvent {

    @Override
    public String eventType() {
        return "receipt.posted";
    }

    public record Line(
        Long receiptLineId,
        Long productId,
        String sku,
        BigDecimal qtyAccepted,
        Long locatorId
    ) {}
}
