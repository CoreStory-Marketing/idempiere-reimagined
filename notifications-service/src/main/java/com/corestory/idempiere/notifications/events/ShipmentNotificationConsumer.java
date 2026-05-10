package com.corestory.idempiere.notifications.events;

import com.corestory.idempiere.common.events.ShipmentCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * <b>STUB.</b> The Artemis topic subscription is wired up, but the handler is empty —
 * it just logs receipt of the event. The recorded brownfield-feature-implementation
 * demo (SHIP-101) implements the three-channel fan-out:
 * <ul>
 *   <li>Email to {@code customerEmail} via {@link com.corestory.idempiere.notifications.channels.EmailNotificationAdapter}</li>
 *   <li>Warehouse log via {@link com.corestory.idempiere.notifications.channels.WarehouseLogAdapter}</li>
 *   <li>Accounting log via {@link com.corestory.idempiere.notifications.channels.AccountingLogAdapter}</li>
 * </ul>
 *
 * <p>iDempiere parity: ModelValidator post-completion hook on {@code MInOut.completeIt()}.
 */
@Slf4j
@Component
public class ShipmentNotificationConsumer {

    @JmsListener(destination = "${idempiere.events.shipments-topic}", containerFactory = "topicListenerFactory")
    public void onShipmentCreated(ShipmentCreatedEvent event) {
        log.info("Received shipment.created for shipmentId={}; handler not yet implemented (SHIP-101)",
            event != null ? event.shipmentId() : null);
    }
}
