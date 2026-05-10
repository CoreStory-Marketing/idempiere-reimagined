package com.corestory.idempiere.orders.events;

import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Lightweight unit test for {@link OrderEventPublisher} — verifies the publisher
 * forwards to {@link JmsTemplate#convertAndSend(String, Object)} using the topic
 * configured by {@code idempiere.events.orders-topic}, and that a JMS exception
 * does NOT propagate out (the order transaction stays committed).
 */
class OrderEventPublisherJmsIntegrationTest {

    @Test
    @DisplayName("publish forwards to convertAndSend with the configured topic")
    void publishForwardsToTopic() {
        JmsTemplate template = mock(JmsTemplate.class);
        OrderEventPublisher publisher = new OrderEventPublisher(template);
        ReflectionTestUtils.setField(publisher, "ordersTopic", "orders.events");

        OrderConfirmedEvent event = new OrderConfirmedEvent(
            UUID.randomUUID(),
            Instant.now(),
            1L, 1L, 7L, "ORD-X", 42L,
            new BigDecimal("100.00"), "USD", List.of()
        );
        publisher.publish(event);

        ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(topic.capture(), payload.capture());
        assertThat(topic.getValue()).isEqualTo("orders.events");
        assertThat(payload.getValue()).isSameAs(event);
    }

    @Test
    @DisplayName("publish swallows JMS exceptions so order transactions still commit")
    void publishSwallowsExceptions() {
        JmsTemplate template = mock(JmsTemplate.class);
        doThrow(new org.springframework.jms.UncategorizedJmsException("broker down")).
            when(template).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(Object.class));
        OrderEventPublisher publisher = new OrderEventPublisher(template);
        ReflectionTestUtils.setField(publisher, "ordersTopic", "orders.events");

        OrderConfirmedEvent event = new OrderConfirmedEvent(
            UUID.randomUUID(), Instant.now(),
            1L, 1L, 7L, "ORD-X", 42L,
            BigDecimal.ZERO, "USD", List.of()
        );

        // Should not throw — exception logged and absorbed.
        publisher.publish(event);
    }
}
