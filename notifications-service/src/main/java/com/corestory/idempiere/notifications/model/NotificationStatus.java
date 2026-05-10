package com.corestory.idempiere.notifications.model;

/**
 * Notification dispatch status — mirrors {@code notification_log.status} CHECK constraint.
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    SKIPPED
}
