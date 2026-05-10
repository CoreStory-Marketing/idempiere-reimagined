package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByOrderId(Long orderId);

    List<Reservation> findByOrderIdAndStatus(Long orderId, ReservationStatus status);

    /**
     * Drives the {@code ReservationExpiryScheduler}: fetch ACTIVE reservations whose TTL elapsed.
     */
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, OffsetDateTime cutoff);

    List<Reservation> findByProductIdAndStatus(Long productId, ReservationStatus status);
}
