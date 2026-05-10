package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.common.events.InventoryReservedEvent;
import com.corestory.idempiere.inventory.config.InventoryProperties;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InventoryEventPublisherTest {

    @Test
    void publishUsesConfiguredTopic() {
        JmsTemplate jms = mock(JmsTemplate.class);
        InventoryProperties props = new InventoryProperties();
        props.getEvents().setInventoryTopic("custom.inventory.topic");

        InventoryEventPublisher publisher = new InventoryEventPublisher(jms, props);
        InventoryReservedEvent event = new InventoryReservedEvent(
            UUID.randomUUID(), Instant.now(), 1L, 1L, 100L, "ORD-1", List.of()
        );

        publisher.publish(event);

        verify(jms).convertAndSend(eq("custom.inventory.topic"), any(Object.class));
    }
}
