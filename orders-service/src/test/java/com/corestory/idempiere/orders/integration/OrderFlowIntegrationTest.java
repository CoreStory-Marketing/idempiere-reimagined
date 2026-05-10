package com.corestory.idempiere.orders.integration;

import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.orders.api.dto.CreateOrderLineRequest;
import com.corestory.idempiere.orders.api.dto.CreateOrderRequest;
import com.corestory.idempiere.orders.events.OrderEventPublisher;
import com.corestory.idempiere.orders.model.Address;
import com.corestory.idempiere.orders.model.AddressType;
import com.corestory.idempiere.orders.model.Country;
import com.corestory.idempiere.orders.model.Customer;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderStatus;
import com.corestory.idempiere.orders.model.OrderStatusHistory;
import com.corestory.idempiere.orders.repo.AddressRepository;
import com.corestory.idempiere.orders.repo.CountryRepository;
import com.corestory.idempiere.orders.repo.CustomerRepository;
import com.corestory.idempiere.orders.repo.OrderRepository;
import com.corestory.idempiere.orders.repo.OrderStatusHistoryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * End-to-end integration test: spins up a real Postgres via Testcontainers,
 * runs Flyway against the live schema, then exercises the create → confirm
 * flow through the actual {@link OrderService} bean.
 *
 * <p>The {@link OrderEventPublisher} is mocked so we can assert event payloads
 * without standing up the embedded Artemis broker on every test run; the JMS
 * wiring itself is covered by {@link com.corestory.idempiere.orders.events
 * .OrderEventPublisherJmsIntegrationTest}.
 */
@SpringBootTest(classes = com.corestory.idempiere.orders.OrdersApplication.class)
@Testcontainers
@ActiveProfiles("test")
class OrderFlowIntegrationTest {

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
        // Fall back to embedded Artemis so the broker connection doesn't hang the test.
        registry.add("spring.artemis.mode", () -> "embedded");
        registry.add("spring.artemis.embedded.enabled", () -> "true");
        registry.add("spring.artemis.embedded.persistent", () -> "false");
    }

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderStatusHistoryRepository statusHistoryRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private CountryRepository countryRepository;
    @MockBean private OrderEventPublisher eventPublisher;

    @Test
    @DisplayName("create -> confirm flow persists the order, advances state, emits OrderConfirmedEvent")
    void createAndConfirm() {
        Customer customer = createCustomer("ACME-001", "Acme Inc");
        Country country = createCountry("US", "United States");
        Address address = createAddress(customer, country);

        CreateOrderRequest req = new CreateOrderRequest(
            customer.getId(),
            address.getId(),
            address.getId(),
            null, null, null,
            "USD",
            LocalDate.of(2026, 5, 9),
            null, "integration test",
            List.of(
                new CreateOrderLineRequest(1L, new BigDecimal("3"),
                    new BigDecimal("20.0000"), BigDecimal.ZERO, null),
                new CreateOrderLineRequest(2L, new BigDecimal("2"),
                    new BigDecimal("15.0000"), new BigDecimal("10.00"), null)
            )
        );

        Order created = orderService.create(req);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(created.getDocumentNo()).startsWith("ORD-");
        // 3*20 + 2*15*0.9 = 60 + 27 = 87
        assertThat(created.getGrandTotal()).isEqualByComparingTo("87.0000");

        // Round-trip via DB to make sure JPA mapping survived the persist.
        Order reloaded = orderRepository.findById(created.getId()).orElseThrow();
        assertThat(reloaded.getLines()).hasSize(2);
        assertThat(reloaded.getCustomer().getId()).isEqualTo(customer.getId());

        Order confirmed = orderService.confirm(created.getId(), "ready");
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // status history should have at least DRAFT (created) and CONFIRMED rows
        List<OrderStatusHistory> history = statusHistoryRepository
            .findByOrderIdOrderByChangedAtAsc(created.getId());
        assertThat(history).extracting(OrderStatusHistory::getToStatus)
            .contains(OrderStatus.DRAFT, OrderStatus.CONFIRMED);

        verify(eventPublisher, atLeastOnce()).publish(any(OrderConfirmedEvent.class));
    }

    @Test
    @DisplayName("Cancel from DRAFT clears via the publisher and advances the status row")
    void cancelDraftFlow() {
        Customer customer = createCustomer("BETA-002", "Beta Co");
        Country country = createCountry("CA", "Canada");
        Address address = createAddress(customer, country);

        CreateOrderRequest req = new CreateOrderRequest(
            customer.getId(), address.getId(), address.getId(),
            null, null, null, "CAD", null, null, null,
            List.of(new CreateOrderLineRequest(1L, BigDecimal.ONE, new BigDecimal("100"), null, null))
        );
        Order created = orderService.create(req);

        Order cancelled = orderService.cancel(created.getId(), "buyer remorse");
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(eventPublisher, times(1))
            .publish(any(com.corestory.idempiere.common.events.OrderCancelledEvent.class));
    }

    @Test
    @DisplayName("Full lifecycle DRAFT -> CONFIRMED -> SHIPPED -> INVOICED -> COMPLETE persists each transition")
    void fullLifecycle() {
        Customer customer = createCustomer("LCYL-003", "Lifecycle Co");
        Country country = createCountry("GB", "United Kingdom");
        Address address = createAddress(customer, country);

        CreateOrderRequest req = new CreateOrderRequest(
            customer.getId(), address.getId(), address.getId(),
            null, null, null, "GBP", null, null, null,
            List.of(new CreateOrderLineRequest(1L, BigDecimal.ONE, new BigDecimal("99.99"), null, null))
        );
        Order created = orderService.create(req);

        orderService.confirm(created.getId(), null);
        orderService.ship(created.getId(), 1234L, "SHIP-1234");
        orderService.invoice(created.getId());
        Order completed = orderService.complete(created.getId());

        assertThat(completed.getStatus()).isEqualTo(OrderStatus.COMPLETE);

        List<OrderStatusHistory> history = statusHistoryRepository
            .findByOrderIdOrderByChangedAtAsc(created.getId());
        assertThat(history).extracting(OrderStatusHistory::getToStatus).containsExactly(
            OrderStatus.DRAFT,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPED,
            OrderStatus.INVOICED,
            OrderStatus.COMPLETE
        );
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Customer createCustomer(String docNo, String name) {
        Customer c = new Customer();
        c.setDocumentNo(docNo + "-" + System.nanoTime());
        c.setName(name);
        c.setCustomer(true);
        c.setVendor(false);
        return customerRepository.saveAndFlush(c);
    }

    private Country createCountry(String iso, String name) {
        return countryRepository.findByIsoCode(iso).orElseGet(() -> {
            Country c = new Country();
            c.setIsoCode(iso);
            c.setName(name);
            return countryRepository.saveAndFlush(c);
        });
    }

    private Address createAddress(Customer customer, Country country) {
        Address a = new Address();
        a.setCustomer(customer);
        a.setCountry(country);
        a.setAddress1("100 Main St");
        a.setCity("Anywhere");
        a.setAddressType(AddressType.BOTH);
        return addressRepository.saveAndFlush(a);
    }
}
