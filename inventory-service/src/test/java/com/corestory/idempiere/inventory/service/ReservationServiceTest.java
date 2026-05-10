package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.config.InventoryProperties;
import com.corestory.idempiere.inventory.exception.InsufficientStockException;
import com.corestory.idempiere.inventory.exception.MandatoryAttributeException;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.model.AttributeSet;
import com.corestory.idempiere.inventory.model.Locator;
import com.corestory.idempiere.inventory.model.Lot;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.model.ReservationStatus;
import com.corestory.idempiere.inventory.model.StockLevel;
import com.corestory.idempiere.inventory.model.StockMovement;
import com.corestory.idempiere.inventory.model.Warehouse;
import com.corestory.idempiere.inventory.repo.LotRepository;
import com.corestory.idempiere.inventory.repo.ProductRepository;
import com.corestory.idempiere.inventory.repo.ReservationRepository;
import com.corestory.idempiere.inventory.repo.SerialNumberRepository;
import com.corestory.idempiere.inventory.repo.StockLevelRepository;
import com.corestory.idempiere.inventory.repo.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage for {@link ReservationService}, with all repositories mocked.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private StockLevelRepository stockLevelRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private LotRepository lotRepository;
    @Mock private SerialNumberRepository serialNumberRepository;

    private InventoryProperties properties;

    @InjectMocks private ReservationService reservationService;

    private Product product;
    private Warehouse warehouse;
    private Locator locA;
    private Locator locB;

    @BeforeEach
    void setUp() {
        properties = new InventoryProperties();
        properties.getReservation().setTtlHours(24);

        // Re-construct the SUT manually because @InjectMocks can't pass the InventoryProperties.
        reservationService = new ReservationService(
            productRepository, stockLevelRepository, reservationRepository,
            stockMovementRepository, lotRepository, serialNumberRepository, properties
        );

        product = Product.builder()
            .id(101L).sku("SKU-1").name("Widget").isActive(true).isStocked(true).build();

        warehouse = Warehouse.builder().id(10L).code("WH1").name("Main").isActive(true).build();
        locA = Locator.builder()
            .id(201L).warehouse(warehouse).code("A-01").priorityNo((short) 10).isActive(true).build();
        locB = Locator.builder()
            .id(202L).warehouse(warehouse).code("B-01").priorityNo((short) 50).isActive(true).build();

        lenient().when(productRepository.findById(101L)).thenReturn(Optional.of(product));
        lenient().when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(inv -> {
                Reservation r = inv.getArgument(0);
                if (r.getId() == null) r.setId(System.nanoTime());
                return r;
            });
        lenient().when(stockLevelRepository.save(any(StockLevel.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(stockMovementRepository.save(any(StockMovement.class)))
            .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Reserves from a single locator when its on-hand covers the requested qty")
    void reservesFromSingleLocator() {
        StockLevel level = StockLevel.builder()
            .id(1L).product(product).warehouse(warehouse).locator(locA)
            .qtyOnHand(new BigDecimal("100")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build();

        when(stockLevelRepository.findByProductIdOrderByLocatorPriority(101L))
            .thenReturn(List.of(level));

        List<Reservation> result = reservationService.reserve(
            101L, new BigDecimal("5"), 1000L, 2000L);

        assertThat(result).hasSize(1);
        Reservation r = result.get(0);
        assertThat(r.getQty()).isEqualByComparingTo("5");
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(r.getLocator().getId()).isEqualTo(201L);
        assertThat(level.getQtyReserved()).isEqualByComparingTo("5");

        // Ledger entry should be SHIPMENT with negative qty.
        ArgumentCaptor<StockMovement> mc = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository, atLeastOnce()).save(mc.capture());
        StockMovement m = mc.getValue();
        assertThat(m.getQty()).isEqualByComparingTo("-5");
    }

    @Test
    @DisplayName("Locator priority: pulls from priority=10 BEFORE priority=50")
    void locatorPriorityOrdering() {
        StockLevel highPriority = StockLevel.builder()
            .id(1L).product(product).warehouse(warehouse).locator(locA) // priority 10
            .qtyOnHand(new BigDecimal("3")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build();
        StockLevel lowPriority = StockLevel.builder()
            .id(2L).product(product).warehouse(warehouse).locator(locB) // priority 50
            .qtyOnHand(new BigDecimal("10")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build();

        // Repository returns them already sorted (the @Query enforces ASC priority_no).
        when(stockLevelRepository.findByProductIdOrderByLocatorPriority(101L))
            .thenReturn(List.of(highPriority, lowPriority));

        List<Reservation> result = reservationService.reserve(
            101L, new BigDecimal("5"), 1000L, 2000L);

        // Should pull all 3 from locA, then 2 from locB → 2 reservations.
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocator().getId()).isEqualTo(201L);
        assertThat(result.get(0).getQty()).isEqualByComparingTo("3");
        assertThat(result.get(1).getLocator().getId()).isEqualTo(202L);
        assertThat(result.get(1).getQty()).isEqualByComparingTo("2");
        assertThat(highPriority.getQtyReserved()).isEqualByComparingTo("3");
        assertThat(lowPriority.getQtyReserved()).isEqualByComparingTo("2");
    }

    @Test
    @DisplayName("Throws InsufficientStockException when total available across all locators is short")
    void throwsWhenInsufficientStock() {
        StockLevel level = StockLevel.builder()
            .id(1L).product(product).warehouse(warehouse).locator(locA)
            .qtyOnHand(new BigDecimal("3")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build();
        when(stockLevelRepository.findByProductIdOrderByLocatorPriority(101L))
            .thenReturn(List.of(level));

        assertThatThrownBy(() ->
            reservationService.reserve(101L, new BigDecimal("10"), 1000L, 2000L)
        )
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock for product 101")
            .satisfies(ex -> {
                InsufficientStockException ise = (InsufficientStockException) ex;
                assertThat(ise.getRequested()).isEqualByComparingTo("10");
                assertThat(ise.getAvailable()).isEqualByComparingTo("3");
            });

        // The reservation row must NOT be persisted.
        verify(reservationRepository, times(0)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Available accounts for already-reserved qty (oversell prevention)")
    void availableExcludesAlreadyReserved() {
        StockLevel level = StockLevel.builder()
            .id(1L).product(product).warehouse(warehouse).locator(locA)
            .qtyOnHand(new BigDecimal("10")).qtyReserved(new BigDecimal("8")) // 2 available
            .qtyOrdered(BigDecimal.ZERO).build();
        when(stockLevelRepository.findByProductIdOrderByLocatorPriority(101L))
            .thenReturn(List.of(level));

        assertThatThrownBy(() ->
            reservationService.reserve(101L, new BigDecimal("5"), 1000L, 2000L)
        ).isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Cancel: ACTIVE -> CANCELLED, qty_reserved decremented, release ledger entry written")
    void cancelReleasesReservedQty() {
        StockLevel level = StockLevel.builder()
            .id(1L).product(product).warehouse(warehouse).locator(locA)
            .qtyOnHand(new BigDecimal("100")).qtyReserved(new BigDecimal("10"))
            .qtyOrdered(BigDecimal.ZERO).build();
        Reservation r = Reservation.builder()
            .id(99L).product(product).qty(new BigDecimal("10"))
            .orderId(1L).orderLineId(1L).warehouse(warehouse).locator(locA)
            .expiresAt(java.time.OffsetDateTime.now().plusHours(24))
            .status(ReservationStatus.ACTIVE).build();

        when(reservationRepository.findById(99L)).thenReturn(Optional.of(r));
        when(stockLevelRepository.findByProductIdAndWarehouseIdAndLocatorId(101L, 10L, 201L))
            .thenReturn(Optional.of(level));

        Reservation result = reservationService.cancel(99L);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(level.getQtyReserved()).isEqualByComparingTo("0");
        verify(stockMovementRepository, atLeast(1)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Cancel is idempotent on a non-ACTIVE reservation")
    void cancelTerminalIsNoop() {
        Reservation r = Reservation.builder()
            .id(99L).product(product).qty(new BigDecimal("10"))
            .orderId(1L).orderLineId(1L).warehouse(warehouse)
            .expiresAt(java.time.OffsetDateTime.now())
            .status(ReservationStatus.FULFILLED).build();
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(r));

        Reservation result = reservationService.cancel(99L);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.FULFILLED);
        verify(stockMovementRepository, times(0)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("404 when product is unknown")
    void unknownProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
            reservationService.reserve(999L, BigDecimal.ONE, 1L, 1L)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Mandatory-lot enforcement: AttributeSet.isLotMandatory=true, no lotNumber → MandatoryAttributeException")
    void mandatoryLotEnforced() {
        AttributeSet set = AttributeSet.builder()
            .id(50L).name("Pharma").isLotMandatory(true).build();
        product.setAttributeSet(set);

        assertThatThrownBy(() ->
            reservationService.reserve(101L, BigDecimal.ONE, 1L, 1L, null, null)
        )
            .isInstanceOf(MandatoryAttributeException.class)
            .hasMessageContaining("lotNumber");
    }

    @Test
    @DisplayName("Mandatory-lot enforcement: lot number passed but not registered → MandatoryAttributeException")
    void mandatoryLotMustBeRegistered() {
        AttributeSet set = AttributeSet.builder()
            .id(50L).name("Pharma").isLotMandatory(true).build();
        product.setAttributeSet(set);

        when(lotRepository.findByProductIdAndLotNumber(101L, "BAD-LOT")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            reservationService.reserve(101L, BigDecimal.ONE, 1L, 1L, "BAD-LOT", null)
        )
            .isInstanceOf(MandatoryAttributeException.class)
            .hasMessageContaining("not registered");
    }

    @Test
    @DisplayName("Mandatory-lot enforcement: registered lot passes the check")
    void mandatoryLotPasses() {
        AttributeSet set = AttributeSet.builder()
            .id(50L).name("Pharma").isLotMandatory(true).build();
        product.setAttributeSet(set);

        when(lotRepository.findByProductIdAndLotNumber(101L, "LOT-001"))
            .thenReturn(Optional.of(Lot.builder().id(1L).product(product).lotNumber("LOT-001").build()));

        StockLevel level = StockLevel.builder()
            .id(1L).product(product).warehouse(warehouse).locator(locA)
            .qtyOnHand(new BigDecimal("10")).qtyReserved(BigDecimal.ZERO)
            .qtyOrdered(BigDecimal.ZERO).build();
        when(stockLevelRepository.findByProductIdOrderByLocatorPriority(101L))
            .thenReturn(List.of(level));

        List<Reservation> result = reservationService.reserve(
            101L, BigDecimal.ONE, 1L, 1L, "LOT-001", null);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Rejects non-positive qty up front")
    void rejectsZeroQty() {
        assertThatThrownBy(() ->
            reservationService.reserve(101L, BigDecimal.ZERO, 1L, 1L)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
