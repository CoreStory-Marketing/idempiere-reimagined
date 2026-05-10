package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.common.events.InventoryReservedEvent;
import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.inventory.exception.InsufficientStockException;
import com.corestory.idempiere.inventory.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Topic subscriber for {@code orders.events}. On {@link OrderConfirmedEvent}, walks the line
 * items and calls {@link ReservationService#reserve} for each. Then publishes a single
 * {@link InventoryReservedEvent} aggregating all created reservations.
 *
 * <p>If any line fails (insufficient stock), the whole transaction rolls back — no partial
 * reservations are persisted, no event is emitted. The order would then be marked
 * BACKORDERED on the orders side (out of scope for this listener).
 */
@Component
public class OrderConfirmedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedEventListener.class);

    private final ReservationService reservationService;
    private final InventoryEventPublisher eventPublisher;

    public OrderConfirmedEventListener(
        ReservationService reservationService,
        InventoryEventPublisher eventPublisher
    ) {
        this.reservationService = reservationService;
        this.eventPublisher = eventPublisher;
    }

    @JmsListener(
        destination = "${idempiere.events.orders-topic}",
        containerFactory = "topicListenerFactory"
    )
    @Transactional
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent eventId={} orderId={} lines={}",
            event.eventId(), event.orderId(), event.lines() == null ? 0 : event.lines().size());

        List<Reservation> all = new ArrayList<>();
        if (event.lines() == null) {
            log.warn("OrderConfirmedEvent {} has no lines; skipping", event.eventId());
            return;
        }
        for (OrderConfirmedEvent.Line line : event.lines()) {
            try {
                List<Reservation> created = reservationService.reserve(
                    line.productId(), line.qtyOrdered(), event.orderId(), line.orderLineId()
                );
                all.addAll(created);
            } catch (InsufficientStockException ex) {
                log.error("Reservation failed for order {} line {}: {}",
                    event.orderId(), line.orderLineId(), ex.getMessage());
                throw ex; // rolls back; no partial event emission
            }
        }

        InventoryReservedEvent out = new InventoryReservedEvent(
            UUID.randomUUID(),
            Instant.now(),
            event.tenantId(),
            event.orgId(),
            event.orderId(),
            event.documentNo(),
            all.stream().map(r -> new InventoryReservedEvent.Reservation(
                r.getId(),
                r.getProduct().getId(),
                r.getProduct().getSku(),
                r.getQty(),
                r.getWarehouse().getId(),
                r.getLocator() == null ? null : r.getLocator().getId(),
                r.getExpiresAt().toInstant()
            )).toList()
        );
        eventPublisher.publish(out);
        log.info("Emitted InventoryReservedEvent for order {} with {} reservations",
            event.orderId(), all.size());
    }
}
