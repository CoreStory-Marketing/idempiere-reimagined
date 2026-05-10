package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {

    List<DeliveryAttempt> findByNotificationLogIdOrderByAttemptNoAsc(Long notificationLogId);
}
