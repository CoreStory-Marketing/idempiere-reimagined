package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.api.dto.ProductStockSummaryDto;
import com.corestory.idempiere.inventory.api.dto.StockAdjustmentRequest;
import com.corestory.idempiere.inventory.api.dto.StockLevelDto;
import com.corestory.idempiere.inventory.api.mapper.StockLevelMapper;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.model.Locator;
import com.corestory.idempiere.inventory.model.MovementType;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.StockLevel;
import com.corestory.idempiere.inventory.model.StockMovement;
import com.corestory.idempiere.inventory.model.Warehouse;
import com.corestory.idempiere.inventory.repo.LocatorRepository;
import com.corestory.idempiere.inventory.repo.ProductRepository;
import com.corestory.idempiere.inventory.repo.StockLevelRepository;
import com.corestory.idempiere.inventory.repo.StockMovementRepository;
import com.corestory.idempiere.inventory.repo.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Read-side queries against {@code stock_levels} plus the {@code POST /products/{id}/stock-adjustments}
 * write path. Pure on-hand mutations — reservation effects live in {@link ReservationService}.
 */
@Service
public class StockLevelService {

    private static final Logger log = LoggerFactory.getLogger(StockLevelService.class);

    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocatorRepository locatorRepository;
    private final StockLevelMapper stockLevelMapper;

    public StockLevelService(
        StockLevelRepository stockLevelRepository,
        StockMovementRepository stockMovementRepository,
        ProductRepository productRepository,
        WarehouseRepository warehouseRepository,
        LocatorRepository locatorRepository,
        StockLevelMapper stockLevelMapper
    ) {
        this.stockLevelRepository = stockLevelRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.locatorRepository = locatorRepository;
        this.stockLevelMapper = stockLevelMapper;
    }

    /**
     * Aggregated per-product stock summary for {@code GET /products/{id}/stock}.
     */
    @Transactional(readOnly = true)
    public ProductStockSummaryDto getStockSummary(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        List<StockLevel> levels = stockLevelRepository.findByProductId(productId);
        List<StockLevelDto> dtos = stockLevelMapper.toDtoList(levels);

        BigDecimal onHand = levels.stream()
            .map(StockLevel::getQtyOnHand)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reserved = levels.stream()
            .map(StockLevel::getQtyReserved)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductStockSummaryDto(
            product.getId(),
            product.getSku(),
            onHand,
            reserved,
            onHand.subtract(reserved),
            dtos
        );
    }

    /**
     * {@code POST /products/{id}/stock-adjustments} — direct mutation of {@code qty_on_hand}
     * with a paired ledger entry. Used for cycle-count corrections and write-offs.
     */
    @Transactional
    public StockLevelDto adjust(Long productId, StockAdjustmentRequest req) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepository.findById(req.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", req.warehouseId()));
        Locator locator = null;
        if (req.locatorId() != null) {
            locator = locatorRepository.findById(req.locatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Locator", req.locatorId()));
        } else {
            locator = locatorRepository.findByWarehouseIdAndIsDefaultTrue(warehouse.getId())
                .orElse(null);
        }

        final Locator resolvedLocator = locator;
        Long locatorId = resolvedLocator == null ? null : resolvedLocator.getId();
        StockLevel level = stockLevelRepository.findByProductIdAndWarehouseIdAndLocatorId(
            productId, warehouse.getId(), locatorId
        ).orElseGet(() -> StockLevel.builder()
            .product(product)
            .warehouse(warehouse)
            .locator(resolvedLocator)
            .qtyOnHand(BigDecimal.ZERO)
            .qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO)
            .build());

        BigDecimal newOnHand = level.getQtyOnHand().add(req.qtyDelta());
        if (newOnHand.signum() < 0) {
            throw new IllegalArgumentException(
                "Adjustment would drive qty_on_hand negative (current=" + level.getQtyOnHand()
                    + ", delta=" + req.qtyDelta() + ")");
        }
        level.setQtyOnHand(newOnHand);
        level = stockLevelRepository.save(level);

        StockMovement movement = StockMovement.builder()
            .movementDate(OffsetDateTime.now())
            .movementType(MovementType.ADJUSTMENT)
            .product(product)
            .qty(req.qtyDelta())
            .toLocator(req.qtyDelta().signum() > 0 ? resolvedLocator : null)
            .fromLocator(req.qtyDelta().signum() < 0 ? resolvedLocator : null)
            .referenceDocType("ADJUSTMENT")
            .build();
        stockMovementRepository.save(movement);

        log.info("Adjusted product {} at warehouse {} locator {} by {} (reason={})",
            productId, warehouse.getId(), locatorId, req.qtyDelta(), req.reason());
        return stockLevelMapper.toDto(level);
    }

    @Transactional(readOnly = true)
    public List<StockLevelDto> getStockLevels(Long productId) {
        return stockLevelMapper.toDtoList(stockLevelRepository.findByProductId(productId));
    }
}
