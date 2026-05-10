package com.corestory.idempiere.notifications.api.dto;

import com.corestory.idempiere.notifications.model.NotificationChannel;
import com.corestory.idempiere.notifications.model.NotificationLog;
import com.corestory.idempiere.notifications.model.NotificationStatus;

import java.time.OffsetDateTime;

public record NotificationLogDto(
    Long id,
    NotificationChannel channel,
    String recipient,
    String subject,
    NotificationStatus status,
    String relatedEventId,
    String relatedEventType,
    String dedupKey,
    OffsetDateTime sentAt,
    String errorMessage,
    Integer retryCount,
    OffsetDateTime createdAt
) {

    public static NotificationLogDto from(NotificationLog n) {
        return new NotificationLogDto(
            n.getId(), n.getChannel(), n.getRecipient(), n.getSubject(), n.getStatus(),
            n.getRelatedEventId(), n.getRelatedEventType(), n.getDedupKey(),
            n.getSentAt(), n.getErrorMessage(), n.getRetryCount(), n.getCreatedAt()
        );
    }
}
