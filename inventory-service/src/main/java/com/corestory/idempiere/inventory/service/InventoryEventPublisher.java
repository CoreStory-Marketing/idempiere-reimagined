package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.common.events.DomainEvent;
import com.corestory.idempiere.inventory.config.InventoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around the topic-mode {@link JmsTemplate} for outbound inventory events.
 * Centralized so the topic name is read from {@link InventoryProperties#getEvents()}.
 */
@Component
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final InventoryProperties properties;

    public InventoryEventPublisher(JmsTemplate jmsTemplate, InventoryProperties properties) {
        this.jmsTemplate = jmsTemplate;
        this.properties = properties;
    }

    /**
     * Publish a {@link DomainEvent} to the configured {@code idempiere.events.inventory-topic}.
     */
    public void publish(DomainEvent event) {
        String topic = properties.getEvents().getInventoryTopic();
        log.info("Publishing event {} (id={}) to topic {}",
            event.eventType(), event.eventId(), topic);
        jmsTemplate.convertAndSend(topic, event);
    }
}
