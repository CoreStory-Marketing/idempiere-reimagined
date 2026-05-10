package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.NotificationChannel;
import com.corestory.idempiere.notifications.model.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {

    Optional<NotificationSubscription> findByUserIdAndChannelAndEventType(Long userId, NotificationChannel channel, String eventType);

    List<NotificationSubscription> findByUserId(Long userId);
}
