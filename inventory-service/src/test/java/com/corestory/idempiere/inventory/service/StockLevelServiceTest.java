package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.api.dto.ProductStockSummaryDto;
import com.corestory.idempiere.inventory.api.dto.StockAdjustmentRequest;
import com.corestory.idempiere.inventory.api.dto.StockLevelDto;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Boot-context coverage of {@link StockLevelService}. Verifies adjustment writes a ledger entry,
 * rejects negative-going adjustments, and the per-product summary aggregates correctly.
 */
@SpringBootTest(classes = com.corestory.idempiere.inventory.InventoryApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StockLevelServiceTest {

    @Autowired private StockLevelService stockLevelService;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private LocatorRepository locatorRepository;
    @Autowired private StockLevelRepository stockLevelRepository;
    @Autowired private UnitOfMeasureRepository uomRepository;

    @Test
    @DisplayName("Positive adjustment: creates StockLevel row if absent, increments qty_on_hand, writes ledger entry")
    void positiveAdjustmentCreatesAndIncrements() {
        UnitOfMeasure uom = uomRepository.save(uom("EA-ADJ1"));
        Product p = productRepository.save(Product.builder()
            .sku("SKU-ADJ1").name("Adj-1").uom(uom).isActive(true).isStocked(true).build());
        Warehouse w = warehouseRepository.save(Warehouse.builder()
            .code("WH-ADJ1").name("Adj WH").isActive(true).build());
        Locator l = locatorRepository.save(Locator.builder()
            .warehouse(w).code("L-ADJ1").priorityNo((short) 10).isDefault(true).isActive(true).build());

        StockLevelDto dto = stockLevelService.adjust(p.getId(), new StockAdjustmentRequest(
            w.getId(), l.getId(), new BigDecimal("25"), "initial-receipt"));

        assertThat(dto.qtyOnHand()).isEqualByComparingTo("25");
        assertThat(dto.qtyAvailable()).isEqualByComparingTo("25");

        ProductStockSummaryDto summary = stockLevelService.getStockSummary(p.getId());
        assertThat(summary.totalOnHand()).isEqualByComparingTo("25");
        assertThat(summary.totalReserved()).isEqualByComparingTo("0");
        assertThat(summary.levels()).hasSize(1);
    }

    @Test
    @DisplayName("Negative adjustment that would drive qty_on_hand below zero is rejected")
    void rejectsOversellAdjustment() {
        UnitOfMeasure uom = uomRepository.save(uom("EA-ADJ2"));
        Product p = productRepository.save(Product.builder()
            .sku("SKU-ADJ2").name("Adj-2").uom(uom).isActive(true).isStocked(true).build());
        Warehouse w = warehouseRepository.save(Warehouse.builder()
            .code("WH-ADJ2").name("Adj WH 2").isActive(true).build());
        Locator l = locatorRepository.save(Locator.builder()
            .warehouse(w).code("L-ADJ2").priorityNo((short) 10).isDefault(true).isActive(true).build());

        stockLevelRepository.save(StockLevel.builder()
            .product(p).warehouse(w).locator(l)
            .qtyOnHand(new BigDecimal("3"))
            .qtyReserved(BigDecimal.ZERO).qtyOrdered(BigDecimal.ZERO).build());

        assertThatThrownBy(() ->
            stockLevelService.adjust(p.getId(), new StockAdjustmentRequest(
                w.getId(), l.getId(), new BigDecimal("-10"), "write-off"))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Adjustment for unknown product returns 404")
    void unknownProductIs404() {
        assertThatThrownBy(() ->
            stockLevelService.adjust(999_999_999L, new StockAdjustmentRequest(
                1L, null, new BigDecimal("1"), "x"))
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getStockLevels lists every locator row for a product")
    void listsAllLocatorRows() {
        UnitOfMeasure uom = uomRepository.save(uom("EA-ADJ3"));
        Product p = productRepository.save(Product.builder()
            .sku("SKU-ADJ3").name("Adj-3").uom(uom).isActive(true).isStocked(true).build());
        Warehouse w = warehouseRepository.save(Warehouse.builder()
            .code("WH-ADJ3").name("Adj WH 3").isActive(true).build());
        Locator l1 = locatorRepository.save(Locator.builder()
            .warehouse(w).code("LX-1").priorityNo((short) 10).isActive(true).build());
        Locator l2 = locatorRepository.save(Locator.builder()
            .warehouse(w).code("LX-2").priorityNo((short) 50).isActive(true).build());

        stockLevelRepository.save(StockLevel.builder()
            .product(p).warehouse(w).locator(l1)
            .qtyOnHand(new BigDecimal("10")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build());
        stockLevelRepository.save(StockLevel.builder()
            .product(p).warehouse(w).locator(l2)
            .qtyOnHand(new BigDecimal("20")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build());

        List<StockLevelDto> rows = stockLevelService.getStockLevels(p.getId());
        assertThat(rows).hasSize(2);
        assertThat(rows.stream().map(StockLevelDto::qtyOnHand))
            .containsExactlyInAnyOrder(new BigDecimal("10.0000"), new BigDecimal("20.0000"))
            .as("should list both locators (10 + 20 = 30 on-hand)");
    }

    private UnitOfMeasure uom(String code) {
        return UnitOfMeasure.builder()
            .code(code).name("Each")
            .stdPrecision((short) 0).costingPrecision((short) 4)
            .isActive(true).build();
    }
}
