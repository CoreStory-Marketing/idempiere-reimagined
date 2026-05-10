package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.NotificationGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationGroupMemberRepository extends JpaRepository<NotificationGroupMember, Long> {

    List<NotificationGroupMember> findByGroupId(Long groupId);
}
