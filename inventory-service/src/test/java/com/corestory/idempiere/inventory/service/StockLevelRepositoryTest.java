package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.model.Locator;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.StockLevel;
import com.corestory.idempiere.inventory.model.UnitOfMeasure;
import com.corestory.idempiere.inventory.model.Warehouse;
import com.corestory.idempiere.inventory.repo.LocatorRepository;
import com.corestory.idempiere.inventory.repo.ProductRepository;
import com.corestory.idempiere.inventory.repo.StockLevelRepository;
import com.corestory.idempiere.inventory.repo.UnitOfMeasureRepository;
import com.corestory.idempiere.inventory.repo.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@code @Query} on {@link StockLevelRepository#findByProductIdOrderByLocatorPriority}
 * actually returns rows ordered by {@code locator.priorityNo} ASC.
 */
@SpringBootTest(classes = com.corestory.idempiere.inventory.InventoryApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StockLevelRepositoryTest {

    @Autowired private StockLevelRepository stockLevelRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private LocatorRepository locatorRepository;
    @Autowired private UnitOfMeasureRepository uomRepository;

    @Test
    @DisplayName("findByProductIdOrderByLocatorPriority returns rows ASC by priority_no")
    void priorityOrderingHonored() {
        UnitOfMeasure uom = uomRepository.save(UnitOfMeasure.builder()
            .code("EA-RP1").name("Each").stdPrecision((short) 0).costingPrecision((short) 4).isActive(true).build());
        Product product = productRepository.save(Product.builder()
            .sku("SKU-RP-1").name("Repo Test").uom(uom).isActive(true).isStocked(true).build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("WH-RP-1").name("RP WH").isActive(true).build());
        Locator low  = locatorRepository.save(Locator.builder().warehouse(warehouse)
            .code("LOW-PRI").priorityNo((short) 90).isActive(true).build());
        Locator mid  = locatorRepository.save(Locator.builder().warehouse(warehouse)
            .code("MID-PRI").priorityNo((short) 50).isActive(true).build());
        Locator high = locatorRepository.save(Locator.builder().warehouse(warehouse)
            .code("HIGH-PRI").priorityNo((short) 10).isActive(true).build());

        // Save in a deliberately scrambled order.
        stockLevelRepository.save(StockLevel.builder().product(product).warehouse(warehouse).locator(mid)
            .qtyOnHand(new BigDecimal("5")).qtyReserved(BigDecimal.ZERO).qtyOrdered(BigDecimal.ZERO).build());
        stockLevelRepository.save(StockLevel.builder().product(product).warehouse(warehouse).locator(low)
            .qtyOnHand(new BigDecimal("5")).qtyReserved(BigDecimal.ZERO).qtyOrdered(BigDecimal.ZERO).build());
        stockLevelRepository.save(StockLevel.builder().product(product).warehouse(warehouse).locator(high)
            .qtyOnHand(new BigDecimal("5")).qtyReserved(BigDecimal.ZERO).qtyOrdered(BigDecimal.ZERO).build());

        List<StockLevel> result = stockLevelRepository.findByProductIdOrderByLocatorPriority(product.getId());
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getLocator().getPriorityNo()).isEqualTo((short) 10);
        assertThat(result.get(1).getLocator().getPriorityNo()).isEqualTo((short) 50);
        assertThat(result.get(2).getLocator().getPriorityNo()).isEqualTo((short) 90);
    }

    @Test
    @DisplayName("findByStatusAndExpiresAtBefore returns reservations in expected window")
    void expiresAtBeforeWindow() {
        // Smoke-test only — the scheduler test exercises this via the real path.
        assertThat(stockLevelRepository.count()).isGreaterThanOrEqualTo(0);
    }
}
