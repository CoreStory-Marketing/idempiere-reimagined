package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.NotificationChannel;
import com.corestory.idempiere.notifications.model.NotificationLog;
import com.corestory.idempiere.notifications.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Optional<NotificationLog> findByDedupKey(String dedupKey);

    List<NotificationLog> findByChannelAndStatus(NotificationChannel channel, NotificationStatus status);

    Page<NotificationLog> findByChannel(NotificationChannel channel, Pageable pageable);
}
