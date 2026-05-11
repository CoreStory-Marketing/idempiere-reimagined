package com.corestory.idempiere.inventory.integration;

import com.corestory.idempiere.common.events.InventoryReservedEvent;
import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.inventory.model.Locator;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.model.ReservationStatus;
import com.corestory.idempiere.inventory.model.StockLevel;
import com.corestory.idempiere.inventory.model.UnitOfMeasure;
import com.corestory.idempiere.inventory.model.Warehouse;
import com.corestory.idempiere.inventory.repo.LocatorRepository;
import com.corestory.idempiere.inventory.repo.ProductRepository;
import com.corestory.idempiere.inventory.repo.ReservationRepository;
import com.corestory.idempiere.inventory.repo.StockLevelRepository;
import com.corestory.idempiere.inventory.repo.UnitOfMeasureRepository;
import com.corestory.idempiere.inventory.repo.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end JMS path:
 * <ol>
 *   <li>Publish an {@link OrderConfirmedEvent} to {@code orders.events} (the configured topic).</li>
 *   <li>Wait for the {@code OrderConfirmedEventListener} to consume it, reserve stock, and emit
 *       {@link InventoryReservedEvent} to {@code inventory.events}.</li>
 *   <li>Test-side {@code @JmsListener} captures the inventory.reserved event and asserts on it.</li>
 * </ol>
 *
 * <p>Backed by an embedded Apache Artemis broker (boot starter-artemis test profile config).
 */
@SpringBootTest(classes = com.corestory.idempiere.inventory.InventoryApplication.class)
@Import(OrderConfirmedListenerIntegrationTest.InventoryReservedSink.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderConfirmedListenerIntegrationTest {

    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private LocatorRepository locatorRepository;
    @Autowired private StockLevelRepository stockLevelRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UnitOfMeasureRepository uomRepository;
    @Autowired private InventoryReservedSink sink;

    @Test
    @DisplayName("OrderConfirmedEvent → reservations created → InventoryReservedEvent emitted")
    void endToEnd() throws Exception {
        // Seed inventory.
        UnitOfMeasure uom = uomRepository.save(UnitOfMeasure.builder()
            .code("EA-INT1").name("Each").stdPrecision((short) 0).costingPrecision((short) 4).isActive(true).build());
        Product product = productRepository.save(Product.builder()
            .sku("SKU-INT-1").name("Integration Test Product")
            .uom(uom).isActive(true).isStocked(true).build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("WH-INT-1").name("Int WH").isActive(true).build());
        Locator locator = locatorRepository.save(Locator.builder()
            .warehouse(warehouse).code("L-INT-1").priorityNo((short) 10)
            .isDefault(true).isActive(true).build());
        stockLevelRepository.save(StockLevel.builder()
            .product(product).warehouse(warehouse).locator(locator)
            .qtyOnHand(new BigDecimal("50"))
            .qtyReserved(BigDecimal.ZERO).qtyOrdered(BigDecimal.ZERO).build());

        long orderId = 88001L;
        OrderConfirmedEvent confirmed = new OrderConfirmedEvent(
            UUID.randomUUID(),
            Instant.now(),
            1L, 1L,
            orderId,
            "ORD-INT-001",
            42L,
            new BigDecimal("199.99"),
            "USD",
            List.of(new OrderConfirmedEvent.Line(
                999001L, product.getId(), product.getSku(),
                new BigDecimal("3"), new BigDecimal("66.66"), new BigDecimal("199.98")
            ))
        );

        // Publish to orders.events.
        jmsTemplate.convertAndSend("orders.events", confirmed);

        // Listener side-effect: a reservation persisted.
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Reservation> rs = reservationRepository.findByOrderId(orderId);
            assertThat(rs).hasSize(1);
            assertThat(rs.get(0).getStatus()).isEqualTo(ReservationStatus.ACTIVE);
            assertThat(rs.get(0).getQty()).isEqualByComparingTo("3");
        });

        // And inventory.reserved was published.
        boolean caught = sink.latch.await(15, TimeUnit.SECONDS);
        assertThat(caught).as("InventoryReservedEvent should arrive on inventory.events").isTrue();

        InventoryReservedEvent received = sink.received.get();
        assertThat(received).isNotNull();
        assertThat(received.orderId()).isEqualTo(orderId);
        assertThat(received.reservations()).hasSize(1);
        assertThat(received.reservations().get(0).qty()).isEqualByComparingTo("3");
        assertThat(received.reservations().get(0).sku()).isEqualTo(product.getSku());

        // Stock-level state.
        StockLevel level = stockLevelRepository.findByProductIdAndWarehouseIdAndLocatorId(
            product.getId(), warehouse.getId(), locator.getId()).orElseThrow();
        assertThat(level.getQtyReserved()).isEqualByComparingTo("3");
        assertThat(level.getQtyOnHand()).isEqualByComparingTo("50");
    }

    /**
     * Inline subscriber to {@code inventory.events} so the test can assert on the emission.
     */
    @Component
    static class InventoryReservedSink {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<InventoryReservedEvent> received = new AtomicReference<>();

        @JmsListener(destination = "inventory.events", containerFactory = "topicListenerFactory")
        public void onMessage(InventoryReservedEvent event) {
            received.set(event);
            latch.countDown();
        }
    }
}
