package com.corestory.idempiere.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;

/**
 * Topic-mode JMS plumbing for inventory-service.
 *
 * <p>Both the listener factory ({@code topicListenerFactory}) and the {@link JmsTemplate}
 * publisher run in pub/sub mode ({@code setPubSubDomain(true)}) so the consumer subscribes
 * to {@code orders.events} as a topic and the publisher emits {@link
 * com.corestory.idempiere.common.events.InventoryReservedEvent} on {@code inventory.events}.
 *
 * <p>Messages are JSON-encoded text via a {@link MappingJackson2MessageConverter} that
 * writes a {@code _type} property on each message so consumers can route to the right
 * polymorphic {@code DomainEvent} subtype.
 */
@Configuration
public class JmsConfig {

    @Bean
    public ObjectMapper jmsObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper jmsObjectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(jmsObjectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    /**
     * Listener container for topic subscribers ({@code orders.events}).
     * Wired to {@code @JmsListener(containerFactory = "topicListenerFactory")} on
     * {@code OrderConfirmedEventListener}.
     */
    @Bean(name = "topicListenerFactory")
    public DefaultJmsListenerContainerFactory topicListenerFactory(
        ConnectionFactory connectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer configurer,
        MessageConverter jacksonJmsMessageConverter,
        @Value("${idempiere.jms.concurrency:1-3}") String concurrency
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(true);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setSessionTransacted(true);
        factory.setConcurrency(concurrency);
        return factory;
    }

    /**
     * Topic-mode publisher used by {@code InventoryEventPublisher}.
     */
    @Bean
    public JmsTemplate jmsTemplate(
        ConnectionFactory connectionFactory,
        MessageConverter jacksonJmsMessageConverter
    ) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setPubSubDomain(true);
        template.setMessageConverter(jacksonJmsMessageConverter);
        return template;
    }
}
