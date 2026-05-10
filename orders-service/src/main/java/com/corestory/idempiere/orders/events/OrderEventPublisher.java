package com.corestory.idempiere.orders.events;

import com.corestory.idempiere.common.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Wraps {@link JmsTemplate#convertAndSend(String, Object)} so the rest of the
 * service does not need to know the topic name or care about JMS concerns.
 *
 * <p>The destination is read from {@code idempiere.events.orders-topic} in
 * {@code application.yml} (default: {@code orders.events}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final JmsTemplate jmsTemplate;

    @Value("${idempiere.events.orders-topic:orders.events}")
    private String ordersTopic;

    /**
     * Publishes an event to the configured orders topic. JMS errors are logged
     * but not rethrown so a transient broker outage doesn't roll back the order
     * transaction; persisted state is the system of record and the event can be
     * re-emitted via the audit log if needed.
     *
     * <p>TODO: bind to {@code TransactionalEventListener(AFTER_COMMIT)} or use
     * the outbox pattern to avoid the rare case where the event is sent but the
     * surrounding DB transaction subsequently rolls back. For the dual-store
     * demo the in-line publish is acceptable.
     */
    public void publish(DomainEvent event) {
        try {
            jmsTemplate.convertAndSend(ordersTopic, event);
            log.debug("Published {} eventId={} to topic={}",
                event.eventType(), event.eventId(), ordersTopic);
        } catch (RuntimeException ex) {
            log.error("Failed to publish event {} eventId={} to topic={}: {}",
                event.eventType(), event.eventId(), ordersTopic, ex.getMessage(), ex);
        }
    }

    public String getOrdersTopic() {
        return ordersTopic;
    }
}
