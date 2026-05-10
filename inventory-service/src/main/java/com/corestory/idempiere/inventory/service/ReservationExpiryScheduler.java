package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.model.ReservationStatus;
import com.corestory.idempiere.inventory.repo.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Periodic scan that releases ACTIVE reservations whose {@code expires_at} has passed.
 *
 * <p>Cron expression is read from {@code idempiere.reservation.expiry-job-cron} (default:
 * every 15 minutes). Each expired reservation goes to {@code EXPIRED}; the on-hand row's
 * {@code qty_reserved} is decremented and a release {@link com.corestory.idempiere.inventory.model.StockMovement}
 * is appended via {@link ReservationService#releaseReservation}.
 */
@Component
public class ReservationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReservationExpiryScheduler(
        ReservationRepository reservationRepository,
        ReservationService reservationService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "${idempiere.reservation.expiry-job-cron:0 */15 * * * *}")
    @Transactional
    public void releaseExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Reservation> expired = reservationRepository
            .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);

        if (expired.isEmpty()) {
            log.debug("Reservation expiry sweep: none expired");
            return;
        }

        log.info("Reservation expiry sweep: releasing {} reservations", expired.size());
        for (Reservation r : expired) {
            try {
                reservationService.releaseReservation(r, ReservationStatus.EXPIRED, "RESERVATION_EXPIRY");
            } catch (RuntimeException ex) {
                log.error("Failed to release expired reservation {}: {}", r.getId(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * Test-friendly entrypoint that runs the same logic outside the scheduler trigger.
     */
    @Transactional
    public int runOnce() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Reservation> expired = reservationRepository
            .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);
        for (Reservation r : expired) {
            reservationService.releaseReservation(r, ReservationStatus.EXPIRED, "RESERVATION_EXPIRY");
        }
        return expired.size();
    }
}
