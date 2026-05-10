package com.corestory.idempiere.notifications;

import com.corestory.idempiere.notifications.events.ShipmentNotificationConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.jms.annotation.JmsListener;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test — confirms the {@link ShipmentNotificationConsumer#onShipmentCreated} method
 * carries the {@code @JmsListener} annotation pointing at the
 * {@code idempiere.events.shipments-topic} placeholder and the {@code topicListenerFactory}
 * container factory. We intentionally do <strong>not</strong> boot a Spring context here —
 * the notifications-service stub does not pull in Testcontainers, and a full integration
 * test belongs in SHIP-101 once the listener body is written.
 */
class ApplicationContextSmokeTest {

    @Test
    void shipmentNotificationConsumerListenerIsWiredCorrectly() throws Exception {
        Method m = ShipmentNotificationConsumer.class.getDeclaredMethod(
            "onShipmentCreated",
            com.corestory.idempiere.common.events.ShipmentCreatedEvent.class);

        JmsListener listener = m.getAnnotation(JmsListener.class);
        assertThat(listener).as("@JmsListener missing on onShipmentCreated").isNotNull();
        assertThat(listener.destination()).isEqualTo("${idempiere.events.shipments-topic}");
        assertThat(listener.containerFactory()).isEqualTo("topicListenerFactory");
    }
}
