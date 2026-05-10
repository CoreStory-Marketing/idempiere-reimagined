package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.common.events.InventoryReservedEvent;
import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.inventory.exception.InsufficientStockException;
import com.corestory.idempiere.inventory.model.Locator;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.model.ReservationStatus;
import com.corestory.idempiere.inventory.model.Warehouse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderConfirmedEventListenerTest {

    @Test
    void emitsInventoryReservedAfterReserving() {
        ReservationService reservationService = mock(ReservationService.class);
        InventoryEventPublisher publisher = mock(InventoryEventPublisher.class);

        Product p = Product.builder().id(101L).sku("SKU-A").name("A").build();
        Warehouse w = Warehouse.builder().id(10L).code("WH").name("WH").build();
        Locator l = Locator.builder().id(201L).warehouse(w).code("L").priorityNo((short) 10).build();
        Reservation r = Reservation.builder()
            .id(50L).product(p).qty(new BigDecimal("3"))
            .orderId(900L).orderLineId(901L).warehouse(w).locator(l)
            .expiresAt(OffsetDateTime.now().plusHours(24))
            .status(ReservationStatus.ACTIVE).build();

        when(reservationService.reserve(any(Long.class), any(BigDecimal.class), any(Long.class), any(Long.class)))
            .thenReturn(List.of(r));

        OrderConfirmedEventListener listener = new OrderConfirmedEventListener(reservationService, publisher);

        OrderConfirmedEvent event = new OrderConfirmedEvent(
            UUID.randomUUID(), Instant.now(), 1L, 1L, 900L, "ORD-1",
            42L, new BigDecimal("0"), "USD",
            List.of(new OrderConfirmedEvent.Line(
                901L, 101L, "SKU-A",
                new BigDecimal("3"), new BigDecimal("10"), new BigDecimal("30")
            ))
        );

        listener.onOrderConfirmed(event);

        ArgumentCaptor<InventoryReservedEvent> capt = ArgumentCaptor.forClass(InventoryReservedEvent.class);
        verify(publisher).publish(capt.capture());
        InventoryReservedEvent emitted = capt.getValue();
        assertThat(emitted.orderId()).isEqualTo(900L);
        assertThat(emitted.reservations()).hasSize(1);
        assertThat(emitted.reservations().get(0).qty()).isEqualByComparingTo("3");
        assertThat(emitted.reservations().get(0).sku()).isEqualTo("SKU-A");
        assertThat(emitted.reservations().get(0).warehouseId()).isEqualTo(10L);
        assertThat(emitted.reservations().get(0).locatorId()).isEqualTo(201L);
    }

    @Test
    void rollsBackAndDoesNotEmitOnInsufficientStock() {
        ReservationService reservationService = mock(ReservationService.class);
        InventoryEventPublisher publisher = mock(InventoryEventPublisher.class);

        when(reservationService.reserve(any(Long.class), any(BigDecimal.class), any(Long.class), any(Long.class)))
            .thenThrow(new InsufficientStockException(101L, new BigDecimal("3"), new BigDecimal("0")));

        OrderConfirmedEventListener listener = new OrderConfirmedEventListener(reservationService, publisher);
        OrderConfirmedEvent event = new OrderConfirmedEvent(
            UUID.randomUUID(), Instant.now(), 1L, 1L, 900L, "ORD-1",
            42L, new BigDecimal("0"), "USD",
            List.of(new OrderConfirmedEvent.Line(
                901L, 101L, "SKU-A",
                new BigDecimal("3"), new BigDecimal("10"), new BigDecimal("30")
            ))
        );

        assertThatThrownBy(() -> listener.onOrderConfirmed(event))
            .isInstanceOf(InsufficientStockException.class);

        verify(publisher, never()).publish(any());
    }

}
