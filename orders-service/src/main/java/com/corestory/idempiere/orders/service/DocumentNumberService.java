package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.orders.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates human-readable order document numbers in the format
 * {@code ORD-YYYYMMDD-NNNNN}.
 *
 * <p>iDempiere parity: legacy {@code AD_Sequence}-based numbering. We use a simple
 * prefix-count algorithm that is good enough for the demo and for the ~100 orders
 * per day load implied by the seed data.
 *
 * <p>Note: this is best-effort. For true uniqueness under heavy concurrency, the
 * unique constraint on {@code orders.document_no} is the actual safety net — if
 * two threads race and both pick the same suffix, the second {@code INSERT} will
 * fail with a {@code DataIntegrityViolationException} and the caller can retry.
 */
@Service
@RequiredArgsConstructor
public class DocumentNumberService {

    private static final String PREFIX = "ORD-";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public String nextOrderNumber() {
        LocalDate today = LocalDate.now(clock);
        String prefix = PREFIX + today.format(DATE_FMT) + "-";
        long countToday = orderRepository.countByDocumentNoStartingWith(prefix + "%");
        long next = countToday + 1L;
        return prefix + String.format("%05d", next);
    }
}
