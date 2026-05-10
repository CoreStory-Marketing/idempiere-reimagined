package com.corestory.idempiere.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Common base type for inter-service events published over Apache Artemis.
 *
 * Every event carries an {@code eventId} (idempotency key for downstream consumers),
 * an {@code occurredAt} timestamp, and a {@code tenantId} / {@code orgId} pair to
 * preserve multi-tenant scoping across the boundary.
 *
 * <p>iDempiere parity: in legacy this is the ModelValidator / DocAction post-completion
 * hook. We model it as JMS topic publish.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderConfirmedEvent.class, name = "order.confirmed"),
    @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "order.cancelled"),
    @JsonSubTypes.Type(value = OrderShippedEvent.class, name = "order.shipped"),
    @JsonSubTypes.Type(value = OrderInvoicedEvent.class, name = "order.invoiced"),
    @JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "order.completed"),
    @JsonSubTypes.Type(value = ShipmentCreatedEvent.class, name = "shipment.created"),
    @JsonSubTypes.Type(value = InventoryReservedEvent.class, name = "inventory.reserved"),
    @JsonSubTypes.Type(value = ReceiptPostedEvent.class, name = "receipt.posted")
})
public interface DomainEvent {

    UUID eventId();

    String eventType();

    Instant occurredAt();

    Long tenantId();

    Long orgId();
}
