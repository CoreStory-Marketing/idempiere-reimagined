package com.corestory.idempiere.orders.integration;

import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.orders.api.dto.CreateOrderLineRequest;
import com.corestory.idempiere.orders.api.dto.CreateOrderRequest;
import com.corestory.idempiere.orders.events.OrderEventPublisher;
import com.corestory.idempiere.orders.exception.IllegalStateTransitionException;
import com.corestory.idempiere.orders.model.Address;
import com.corestory.idempiere.orders.model.AddressType;
import com.corestory.idempiere.orders.model.Country;
import com.corestory.idempiere.orders.model.Customer;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderStatus;
import com.corestory.idempiere.orders.repo.AddressRepository;
import com.corestory.idempiere.orders.repo.CountryRepository;
import com.corestory.idempiere.orders.repo.CustomerRepository;
import com.corestory.idempiere.orders.repo.OrderRepository;
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Concurrent-confirm safety check: two threads racing to {@code confirm} the
 * same DRAFT order must result in exactly one successful CONFIRMED transition
 * and exactly one {@link OrderConfirmedEvent} on the wire.
 *
 * <p>The pessimistic lock in
 * {@link com.corestory.idempiere.orders.repo.OrderRepository#findByIdForUpdate}
 * is what makes this safe — without it we'd risk publishing two events.
 */
@SpringBootTest(classes = com.corestory.idempiere.orders.OrdersApplication.class)
@Testcontainers
@ActiveProfiles("test")
class ConcurrentConfirmIntegrationTest {

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
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private CountryRepository countryRepository;
    @MockBean private OrderEventPublisher eventPublisher;

    @Test
    @DisplayName("Two concurrent confirms result in exactly one CONFIRMED state and one event")
    void concurrentConfirm() throws InterruptedException, ExecutionException, TimeoutException {
        Customer customer = createCustomer();
        Country country = createCountry();
        Address address = createAddress(customer, country);

        CreateOrderRequest req = new CreateOrderRequest(
            customer.getId(), address.getId(), address.getId(),
            null, null, null, "USD", null, null, null,
            List.of(new CreateOrderLineRequest(1L, BigDecimal.ONE, new BigDecimal("50"), null, null))
        );
        Order order = orderService.create(req);
        Long orderId = order.getId();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Callable<Void> task = () -> {
            try {
                orderService.confirm(orderId, "race");
                successCount.incrementAndGet();
            } catch (IllegalStateTransitionException expected) {
                failureCount.incrementAndGet();
            }
            return null;
        };

        Future<Void> a = pool.submit(task);
        Future<Void> b = pool.submit(task);
        a.get(30, TimeUnit.SECONDS);
        b.get(30, TimeUnit.SECONDS);
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);

        Order reloaded = orderRepository.findById(orderId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(eventPublisher, times(1)).publish(any(OrderConfirmedEvent.class));
    }

    private Customer createCustomer() {
        Customer c = new Customer();
        c.setDocumentNo("RACE-" + System.nanoTime());
        c.setName("Race Co");
        c.setCustomer(true);
        return customerRepository.saveAndFlush(c);
    }

    private Country createCountry() {
        return countryRepository.findByIsoCode("US").orElseGet(() -> {
            Country c = new Country();
            c.setIsoCode("US");
            c.setName("United States");
            return countryRepository.saveAndFlush(c);
        });
    }

    private Address createAddress(Customer customer, Country country) {
        Address a = new Address();
        a.setCustomer(customer);
        a.setCountry(country);
        a.setAddress1("1 Race Lane");
        a.setCity("Speedway");
        a.setAddressType(AddressType.BOTH);
        return addressRepository.saveAndFlush(a);
    }
}
