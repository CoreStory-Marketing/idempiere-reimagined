package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.exception.InsufficientStockException;
import com.corestory.idempiere.inventory.model.Locator;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.Reservation;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Two orders racing for the last unit" — verifies optimistic locking on {@link StockLevel}
 * causes exactly one of N concurrent reservations to win when the row holds a single unit.
 *
 * <p>Backed by H2 (test profile). The {@code @Version} column on AuditableEntity drives the
 * serialization: the second commit observes a stale version and raises
 * OptimisticLockingFailureException, which the SUT wraps and the loser observes either as
 * that exception or as InsufficientStockException (if its retry observed the now-zero stock).
 */
@SpringBootTest(classes = com.corestory.idempiere.inventory.InventoryApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConcurrentReservationTest {

    @Autowired private ReservationService reservationService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private LocatorRepository locatorRepository;
    @Autowired private StockLevelRepository stockLevelRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UnitOfMeasureRepository uomRepository;

    @Test
    @DisplayName("Two orders race for the last unit; one wins, the other fails (no oversell)")
    void onlyOneWinnerForLastUnit() throws Exception {
        // Seed.
        UnitOfMeasure uom = uomRepository.save(UnitOfMeasure.builder()
            .code("EA-CR1").name("Each").stdPrecision((short) 0).costingPrecision((short) 4)
            .isDefault(false).isActive(true).build());
        Product product = productRepository.save(Product.builder()
            .sku("SKU-RACE-1").name("Last One").uom(uom).isStocked(true).isActive(true).build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("WH-RACE-1").name("Race WH").isActive(true).build());
        Locator locator = locatorRepository.save(Locator.builder()
            .warehouse(warehouse).code("L-RACE-1").priorityNo((short) 10)
            .isDefault(true).isActive(true).build());
        stockLevelRepository.save(StockLevel.builder()
            .product(product).warehouse(warehouse).locator(locator)
            .qtyOnHand(new BigDecimal("1"))
            .qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO)
            .build());

        // Two threads, each tries to reserve 1 unit.
        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);
        ConcurrentLinkedQueue<String> failureKinds = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            final long orderId = 1000L + i;
            pool.submit(() -> {
                try {
                    ready.countDown();
                    go.await(5, TimeUnit.SECONDS);
                    List<Reservation> created = reservationService.reserve(
                        product.getId(), BigDecimal.ONE, orderId, orderId);
                    assertThat(created).hasSize(1);
                    successes.incrementAndGet();
                } catch (InsufficientStockException ex) {
                    failures.incrementAndGet();
                    failureKinds.add("INSUFFICIENT");
                } catch (ObjectOptimisticLockingFailureException ex) {
                    failures.incrementAndGet();
                    failureKinds.add("OPTIMISTIC");
                } catch (Exception ex) {
                    failures.incrementAndGet();
                    failureKinds.add(ex.getClass().getSimpleName());
                }
                return null;
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        go.countDown();
        pool.shutdown();
        boolean done = pool.awaitTermination(30, TimeUnit.SECONDS);
        assertThat(done).as("all reserve() calls should complete in time").isTrue();

        // Exactly one wins.
        assertThat(successes.get())
            .as("exactly one reservation should succeed (failures: %s)", failureKinds)
            .isEqualTo(1);
        assertThat(failures.get())
            .as("the loser should fail cleanly")
            .isEqualTo(1);

        // Final state: one ACTIVE reservation; qty_reserved == 1 and qty_on_hand still 1.
        List<StockLevel> levels = stockLevelRepository.findByProductId(product.getId());
        assertThat(levels).hasSize(1);
        assertThat(levels.get(0).getQtyOnHand()).isEqualByComparingTo("1");
        assertThat(levels.get(0).getQtyReserved()).isEqualByComparingTo("1");

        long activeCount = reservationRepository.findByOrderId(1000L).size()
            + reservationRepository.findByOrderId(1001L).size();
        assertThat(activeCount).isEqualTo(1);
    }
}
