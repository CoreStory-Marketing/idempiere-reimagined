package com.corestory.idempiere.orders.integration;

import com.corestory.idempiere.orders.events.OrderEventPublisher;
import com.corestory.idempiere.orders.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bootstraps the full Spring context with a Postgres Testcontainer and an
 * embedded Artemis broker to confirm every bean can be wired (Flyway migrations
 * apply, JPA validates the schema, JMS template is set up correctly).
 */
@SpringBootTest(classes = com.corestory.idempiere.orders.OrdersApplication.class)
@Testcontainers
@ActiveProfiles("test")
class ApplicationContextSmokeTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("orders")
        .withUsername("orders")
        .withPassword("orders");

    @DynamicPropertySource
    static void overrides(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.artemis.mode", () -> "embedded");
        registry.add("spring.artemis.embedded.enabled", () -> "true");
        registry.add("spring.artemis.embedded.persistent", () -> "false");
    }

    @Autowired private OrderService orderService;
    @MockBean private OrderEventPublisher eventPublisher;

    @Test
    @DisplayName("Spring context boots and OrderService is wired")
    void contextLoads() {
        assertThat(orderService).isNotNull();
    }
}
