package com.corestory.idempiere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Emitted by shipping-service after {@code POST /shipments/{id}/ship} succeeds.
 * notifications-service consumes this on the {@code shipments.events} Artemis topic.
 *
 * <p>iDempiere parity: {@code MInOut.completeIt()} → ModelValidator post-complete hook
 * (lib/org.adempiere.base/src/org/compiere/model/MInOut.java:1631). The legacy gates
 * mail send via the {@code SendEMail} boolean (line 599); we mirror with {@code sendEmailFlag}.
 */
public record ShipmentCreatedEvent(
    UUID eventId,
    Instant occurredAt,
    Long tenantId,
    Long orgId,
    Long shipmentId,
    String shipmentDocumentNo,
    Long orderId,
    String orderDocumentNo,
    Long customerId,
    String customerName,
    String customerEmail,
    Long shipToAddressId,
    Long carrierId,
    String carrierName,
    String trackingNumber,
    BigDecimal weightTotal,
    BigDecimal freightAmount,
    Boolean sendEmailFlag,
    Instant shipDate,
    List<Line> lines
) implements DomainEvent {

    @Override
    public String eventType() {
        return "shipment.created";
    }

    public record Line(
        Long shipmentLineId,
        Long orderLineId,
        Long productId,
        String sku,
        String productName,
        BigDecimal qtyShipped
    ) {}
}
