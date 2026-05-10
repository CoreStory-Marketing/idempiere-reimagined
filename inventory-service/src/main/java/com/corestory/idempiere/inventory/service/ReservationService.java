package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.config.InventoryProperties;
import com.corestory.idempiere.inventory.exception.InsufficientStockException;
import com.corestory.idempiere.inventory.exception.MandatoryAttributeException;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.model.AttributeSet;
import com.corestory.idempiere.inventory.model.Lot;
import com.corestory.idempiere.inventory.model.MovementType;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.model.ReservationStatus;
import com.corestory.idempiere.inventory.model.SerialNumber;
import com.corestory.idempiere.inventory.model.StockLevel;
import com.corestory.idempiere.inventory.model.StockMovement;
import com.corestory.idempiere.inventory.repo.LotRepository;
import com.corestory.idempiere.inventory.repo.ProductRepository;
import com.corestory.idempiere.inventory.repo.ReservationRepository;
import com.corestory.idempiere.inventory.repo.SerialNumberRepository;
import com.corestory.idempiere.inventory.repo.StockLevelRepository;
import com.corestory.idempiere.inventory.repo.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core reservation business logic.
 *
 * <h3>Reservation algorithm</h3>
 * Given a (productId, qty, orderId, orderLineId) request:
 * <ol>
 *     <li>Resolve the {@link Product} and verify lot/serial mandatory rules.</li>
 *     <li>Load all {@link StockLevel} rows for the product, ordered ascending by
 *         {@code locator.priorityNo}.</li>
 *     <li>Walk the list and take qty from the highest-priority locator that has
 *         sufficient available qty (on_hand - reserved). The first locator that satisfies
 *         the request wins (we do NOT split across locators in this implementation —
 *         multi-locator splitting is a future story; the request fails if no single row
 *         can cover the demand and total available across all rows is also short).</li>
 *     <li>If no single locator can satisfy it, but cumulative available across all rows
 *         covers it, split: take greedily from highest priority down. If even the
 *         cumulative total falls short, throw {@link InsufficientStockException}.</li>
 *     <li>For each chosen row: increment {@code qtyReserved}, persist (the
 *         {@code @Version} on {@link com.corestory.idempiere.inventory.model.AuditableEntity}
 *         provides optimistic locking — concurrent transactions racing on the same row
 *         produce one Optimistic-lock failure, surfaced to the caller as
 *         CONCURRENT_MODIFICATION).</li>
 *     <li>Create a {@link Reservation} row (status=ACTIVE, expires_at=now+ttl_hours)
 *         and a {@link StockMovement} ledger entry (movement_type=SHIPMENT, qty negative).</li>
 * </ol>
 *
 * <p>iDempiere parity: this is the {@code MStorage.add()}/{@code MStorageReservation.add()}
 * decomposition; the Java original is in {@code MStorage.java:540} (priority-walk by locator)
 * and {@code MStorageReservation.add():120} (the reservation row).
 */
@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ReservationRepository reservationRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LotRepository lotRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final InventoryProperties properties;

    public ReservationService(
        ProductRepository productRepository,
        StockLevelRepository stockLevelRepository,
        ReservationRepository reservationRepository,
        StockMovementRepository stockMovementRepository,
        LotRepository lotRepository,
        SerialNumberRepository serialNumberRepository,
        InventoryProperties properties
    ) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.reservationRepository = reservationRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.lotRepository = lotRepository;
        this.serialNumberRepository = serialNumberRepository;
        this.properties = properties;
    }

    /**
     * Convenience overload for the listener path: no lot / serial supplied.
     */
    @Transactional
    public List<Reservation> reserve(
        Long productId, BigDecimal qty, Long orderId, Long orderLineId
    ) {
        return reserve(productId, qty, orderId, orderLineId, null, null);
    }

    /**
     * Reserve {@code qty} units of {@code productId} against the order line.
     * May produce 1..N {@link Reservation} rows (one per locator pulled from).
     *
     * @throws InsufficientStockException if total available across all locators is &lt; qty.
     * @throws MandatoryAttributeException if the product's attribute set requires a lot/serial
     *         that wasn't supplied.
     * @throws ResourceNotFoundException if {@code productId} doesn't exist.
     */
    @Transactional
    public List<Reservation> reserve(
        Long productId, BigDecimal qty, Long orderId, Long orderLineId,
        String lotNumber, String serialNumber
    ) {
        if (qty == null || qty.signum() <= 0) {
            throw new IllegalArgumentException("qty must be positive: " + qty);
        }
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        validateMandatoryAttributes(product, lotNumber, serialNumber);

        // Locator-priority ordered scan.
        List<StockLevel> levels = stockLevelRepository.findByProductIdOrderByLocatorPriority(productId);

        BigDecimal totalAvailable = levels.stream()
            .map(StockLevel::getQtyAvailable)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(qty) < 0) {
            log.warn("Cannot reserve {} of product {}: only {} available across {} locator rows",
                qty, productId, totalAvailable, levels.size());
            throw new InsufficientStockException(productId, qty, totalAvailable);
        }

        BigDecimal remaining = qty;
        OffsetDateTime expiresAt = OffsetDateTime.now()
            .plusHours(properties.getReservation().getTtlHours());
        List<Reservation> created = new ArrayList<>();

        for (StockLevel level : levels) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal available = level.getQtyAvailable();
            if (available.signum() <= 0) {
                continue;
            }
            BigDecimal take = available.min(remaining);

            // Increment reserved on the level (the @Version field will detect a concurrent writer).
            level.setQtyReserved(level.getQtyReserved().add(take));
            stockLevelRepository.save(level);

            Reservation reservation = Reservation.builder()
                .product(product)
                .qty(take)
                .orderId(orderId)
                .orderLineId(orderLineId)
                .warehouse(level.getWarehouse())
                .locator(level.getLocator())
                .expiresAt(expiresAt)
                .status(ReservationStatus.ACTIVE)
                .build();
            reservation = reservationRepository.save(reservation);
            created.add(reservation);

            // Append-only ledger entry. Negative qty for the hold (matches iDempiere
            // M_Transaction sign convention for reservation/shipment).
            StockMovement movement = StockMovement.builder()
                .movementDate(OffsetDateTime.now())
                .movementType(MovementType.SHIPMENT)
                .product(product)
                .qty(take.negate())
                .fromLocator(level.getLocator())
                .toLocator(null)
                .referenceDocId(reservation.getId())
                .referenceDocType("RESERVATION")
                .build();
            stockMovementRepository.save(movement);

            remaining = remaining.subtract(take);
            log.debug("Reserved {} from locator {} (priority {}); remaining={}",
                take,
                level.getLocator() == null ? null : level.getLocator().getId(),
                level.getLocator() == null ? null : level.getLocator().getPriorityNo(),
                remaining);
        }

        if (remaining.signum() != 0) {
            // Should not happen — we pre-checked total. Defensive.
            throw new InsufficientStockException(productId, qty, qty.subtract(remaining));
        }

        log.info("Reserved {} of product {} for order {} line {} across {} locators",
            qty, productId, orderId, orderLineId, created.size());
        return created;
    }

    /**
     * Cancel an ACTIVE reservation (e.g. order cancelled before shipment).
     * Decrements {@code qty_reserved} on the source stock level and writes a release
     * ledger entry with positive qty.
     */
    @Transactional
    public Reservation cancel(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        if (r.getStatus() != ReservationStatus.ACTIVE) {
            log.info("Reservation {} already in terminal state {}, no-op", reservationId, r.getStatus());
            return r;
        }
        releaseReservation(r, ReservationStatus.CANCELLED, "RESERVATION_CANCEL");
        return r;
    }

    /**
     * Mark an ACTIVE reservation as FULFILLED (called when shipping-service confirms the pick).
     * Decrements both {@code qty_on_hand} and {@code qty_reserved} on the underlying stock level.
     */
    @Transactional
    public Reservation fulfill(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        if (r.getStatus() != ReservationStatus.ACTIVE) {
            return r;
        }
        Optional<StockLevel> levelOpt = findStockLevelFor(r);
        levelOpt.ifPresent(level -> {
            level.setQtyOnHand(level.getQtyOnHand().subtract(r.getQty()));
            level.setQtyReserved(level.getQtyReserved().subtract(r.getQty()));
            stockLevelRepository.save(level);
        });

        r.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.save(r);
        return r;
    }

    /**
     * Used by {@link ReservationExpiryScheduler} (and by {@link #cancel}) to release a hold:
     * decrement reserved on the level, write a positive (release) ledger entry, set status.
     */
    @Transactional
    public void releaseReservation(Reservation r, ReservationStatus newStatus, String referenceDocType) {
        Optional<StockLevel> levelOpt = findStockLevelFor(r);
        levelOpt.ifPresent(level -> {
            level.setQtyReserved(level.getQtyReserved().subtract(r.getQty()));
            if (level.getQtyReserved().signum() < 0) {
                // Defensive: never let it go negative due to data drift.
                level.setQtyReserved(BigDecimal.ZERO);
            }
            stockLevelRepository.save(level);
        });

        StockMovement release = StockMovement.builder()
            .movementDate(OffsetDateTime.now())
            .movementType(MovementType.ADJUSTMENT)
            .product(r.getProduct())
            .qty(r.getQty()) // positive — release
            .toLocator(r.getLocator())
            .referenceDocId(r.getId())
            .referenceDocType(referenceDocType)
            .build();
        stockMovementRepository.save(release);

        r.setStatus(newStatus);
        reservationRepository.save(r);
    }

    /* ------------------------------------------------------------------ helpers */

    private Optional<StockLevel> findStockLevelFor(Reservation r) {
        Long locatorId = r.getLocator() == null ? null : r.getLocator().getId();
        if (locatorId == null) {
            return stockLevelRepository.findByProductIdAndWarehouseId(
                r.getProduct().getId(), r.getWarehouse().getId()
            ).stream().findFirst();
        }
        return stockLevelRepository.findByProductIdAndWarehouseIdAndLocatorId(
            r.getProduct().getId(), r.getWarehouse().getId(), locatorId);
    }

    private void validateMandatoryAttributes(Product product, String lotNumber, String serialNumber) {
        AttributeSet set = product.getAttributeSet();
        if (set == null) {
            return;
        }
        if (set.isLotMandatory()) {
            if (lotNumber == null || lotNumber.isBlank()) {
                throw new MandatoryAttributeException(product.getId(), "lotNumber");
            }
            // Verify the lot exists for this product.
            Lot lot = lotRepository.findByProductIdAndLotNumber(product.getId(), lotNumber)
                .orElseThrow(() -> new MandatoryAttributeException(product.getId(),
                    "lotNumber=" + lotNumber + " (not registered for product)"));
            log.debug("Lot {} validated for product {}", lot.getLotNumber(), product.getId());
        }
        if (set.isSerialMandatory()) {
            if (serialNumber == null || serialNumber.isBlank()) {
                throw new MandatoryAttributeException(product.getId(), "serialNumber");
            }
            SerialNumber sn = serialNumberRepository.findByProductIdAndSerialNumber(
                product.getId(), serialNumber
            ).orElseThrow(() -> new MandatoryAttributeException(product.getId(),
                "serialNumber=" + serialNumber + " (not registered)"));
            log.debug("Serial {} validated for product {}", sn.getSerialNumber(), product.getId());
        }
    }
}
