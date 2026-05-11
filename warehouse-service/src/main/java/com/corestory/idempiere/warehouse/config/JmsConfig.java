package com.corestory.idempiere.warehouse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Topic-mode JMS publisher. warehouse-service emits {@code receipt.posted} onto the
 * {@code warehouse.events} topic but does not consume any topic itself yet (inventory-service
 * will be the consumer when its half is built).
 *
 * <p><b>Profile-guarded:</b> excluded from the {@code test} profile so
 * {@link com.corestory.idempiere.warehouse.AbstractIntegrationTest.TestJmsConfig}'s mocked
 * {@code topicJmsTemplate} is the only bean of that name during integration tests
 * (otherwise the prod bean's real {@code JmsTemplate} wraps the mocked
 * {@code ConnectionFactory} and throws on first {@code convertAndSend} when
 * {@code createConnection()} returns null).
 */
@Profile("!test")
@Configuration
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public JmsTemplate topicJmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate t = new JmsTemplate(connectionFactory);
        t.setPubSubDomain(true);
        t.setMessageConverter(messageConverter);
        return t;
    }
}
