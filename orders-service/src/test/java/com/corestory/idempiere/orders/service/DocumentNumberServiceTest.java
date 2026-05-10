package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.orders.repo.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentNumberServiceTest {

    @Mock private OrderRepository orderRepository;

    @Test
    @DisplayName("Document number format is ORD-YYYYMMDD-NNNNN starting at 00001")
    void firstNumberOfDay() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-09T12:00:00Z"), ZoneOffset.UTC);
        DocumentNumberService svc = new DocumentNumberService(orderRepository, clock);
        when(orderRepository.countByDocumentNoStartingWith(startsWith("ORD-20260509-"))).thenReturn(0L);

        assertThat(svc.nextOrderNumber()).isEqualTo("ORD-20260509-00001");
    }

    @Test
    @DisplayName("Counter increments past existing rows for the day")
    void incrementsPastExisting() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-09T12:00:00Z"), ZoneOffset.UTC);
        DocumentNumberService svc = new DocumentNumberService(orderRepository, clock);
        when(orderRepository.countByDocumentNoStartingWith(startsWith("ORD-20260509-"))).thenReturn(42L);

        assertThat(svc.nextOrderNumber()).isEqualTo("ORD-20260509-00043");
    }

    @Test
    @DisplayName("Counter pads to 5 digits even past 100")
    void zeroPaddedFiveDigits() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-09T00:00:00Z"), ZoneOffset.UTC);
        DocumentNumberService svc = new DocumentNumberService(orderRepository, clock);
        when(orderRepository.countByDocumentNoStartingWith(startsWith("ORD-20260509-"))).thenReturn(123L);

        assertThat(svc.nextOrderNumber()).isEqualTo("ORD-20260509-00124");
    }
}
