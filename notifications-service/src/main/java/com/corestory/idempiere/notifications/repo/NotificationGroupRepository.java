package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationGroupRepository extends JpaRepository<NotificationGroup, Long> {

    Optional<NotificationGroup> findByCode(String code);
}
