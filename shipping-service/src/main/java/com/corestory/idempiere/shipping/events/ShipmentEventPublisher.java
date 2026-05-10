package com.corestory.idempiere.shipping.events;

import com.corestory.idempiere.common.events.ShipmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * <b>STUBBED.</b> Publisher is declared and {@code JmsTemplate} is wired, but the publish
 * method has a TODO body — the recorded brownfield-feature-implementation demo (SHIP-101)
 * fills it in alongside {@code ShipmentService.ship()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventPublisher {

    private final JmsTemplate topicJmsTemplate;

    @Value("${idempiere.events.shipments-topic}")
    private String shipmentsTopic;

    public void publishShipmentCreated(ShipmentCreatedEvent event) {
        // TODO: implement during SHIP-101 — should call topicJmsTemplate.convertAndSend(shipmentsTopic, event)
        log.warn("publishShipmentCreated not yet implemented (SHIP-101) — event {} dropped", event != null ? event.eventId() : null);
    }
}
