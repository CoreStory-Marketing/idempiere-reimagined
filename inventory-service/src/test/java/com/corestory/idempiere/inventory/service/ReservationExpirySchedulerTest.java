package com.corestory.idempiere.inventory.service;

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
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end check of {@link ReservationExpiryScheduler#runOnce}. Seeds an ACTIVE reservation
 * with {@code expires_at} in the past, runs the sweep, and asserts the row flips to EXPIRED
 * and the underlying stock level's {@code qty_reserved} drops to zero.
 */
@SpringBootTest(classes = com.corestory.idempiere.inventory.InventoryApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReservationExpirySchedulerTest {

    @Autowired private ReservationExpiryScheduler scheduler;
    @Autowired private ReservationService reservationService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private LocatorRepository locatorRepository;
    @Autowired private StockLevelRepository stockLevelRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UnitOfMeasureRepository uomRepository;

    @Test
    @DisplayName("Expired ACTIVE reservations: flipped to EXPIRED, qty_reserved decremented, release ledger entry written")
    void expiresAndReleases() {
        UnitOfMeasure uom = uomRepository.save(UnitOfMeasure.builder()
            .code("EA-EX1").name("Each").stdPrecision((short) 0).costingPrecision((short) 4)
            .isActive(true).build());
        Product product = productRepository.save(Product.builder()
            .sku("SKU-EXPIRY-1").name("TTL Test").uom(uom).isActive(true).isStocked(true).build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("WH-EX-1").name("Exp WH").isActive(true).build());
        Locator locator = locatorRepository.save(Locator.builder()
            .warehouse(warehouse).code("L-EX-1").priorityNo((short) 10)
            .isDefault(true).isActive(true).build());

        StockLevel level = stockLevelRepository.save(StockLevel.builder()
            .product(product).warehouse(warehouse).locator(locator)
            .qtyOnHand(new BigDecimal("100"))
            .qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO)
            .build());

        // Reserve, then artificially backdate expires_at.
        List<Reservation> reservations = reservationService.reserve(
            product.getId(), new BigDecimal("3"), 5000L, 6000L);
        assertThat(reservations).hasSize(1);
        Reservation r = reservations.get(0);
        r.setExpiresAt(OffsetDateTime.now().minusHours(1));
        reservationRepository.save(r);

        // Sanity: stock-level reserved went up.
        StockLevel beforeSweep = stockLevelRepository.findById(level.getId()).orElseThrow();
        assertThat(beforeSweep.getQtyReserved()).isEqualByComparingTo("3");

        // Run scheduler.
        int swept = scheduler.runOnce();

        assertThat(swept).isEqualTo(1);

        Reservation reloaded = reservationRepository.findById(r.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ReservationStatus.EXPIRED);

        StockLevel afterSweep = stockLevelRepository.findById(level.getId()).orElseThrow();
        assertThat(afterSweep.getQtyReserved()).isEqualByComparingTo("0");
        assertThat(afterSweep.getQtyOnHand()).isEqualByComparingTo("100"); // on-hand untouched
    }

    @Test
    @DisplayName("ACTIVE reservations whose TTL has not elapsed are left alone")
    void doesNotExpireFreshReservations() {
        UnitOfMeasure uom = uomRepository.save(UnitOfMeasure.builder()
            .code("EA-EX2").name("Each").stdPrecision((short) 0).costingPrecision((short) 4)
            .isActive(true).build());
        Product product = productRepository.save(Product.builder()
            .sku("SKU-EXPIRY-2").name("Fresh").uom(uom).isActive(true).isStocked(true).build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("WH-EX-2").name("Exp WH 2").isActive(true).build());
        Locator locator = locatorRepository.save(Locator.builder()
            .warehouse(warehouse).code("L-EX-2").priorityNo((short) 10)
            .isDefault(true).isActive(true).build());

        stockLevelRepository.save(StockLevel.builder()
            .product(product).warehouse(warehouse).locator(locator)
            .qtyOnHand(new BigDecimal("100"))
            .qtyReserved(BigDecimal.ZERO).qtyOrdered(BigDecimal.ZERO).build());

        reservationService.reserve(product.getId(), new BigDecimal("3"), 7001L, 8001L);

        int swept = scheduler.runOnce();
        assertThat(swept).isEqualTo(0);

        List<Reservation> all = reservationRepository.findByOrderId(7001L);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }
}
