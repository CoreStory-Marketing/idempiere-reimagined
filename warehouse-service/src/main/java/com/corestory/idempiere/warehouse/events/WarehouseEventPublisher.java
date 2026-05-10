package com.corestory.idempiere.warehouse.events;

import com.corestory.idempiere.common.events.DomainEvent;
import com.corestory.idempiere.common.events.ReceiptPostedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper over Artemis {@link JmsTemplate} for warehouse domain events. Pub-sub mode
 * (topic). Inventory-service is the intended downstream consumer of {@link ReceiptPostedEvent}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseEventPublisher {

    private final JmsTemplate topicJmsTemplate;

    @Value("${idempiere.events.warehouse-topic}")
    private String warehouseTopic;

    public void publishReceiptPosted(ReceiptPostedEvent event) {
        publish(event);
    }

    private void publish(DomainEvent event) {
        log.info("publishing {} eventId={} to topic={}", event.eventType(), event.eventId(), warehouseTopic);
        topicJmsTemplate.convertAndSend(warehouseTopic, event);
    }
}
