package com.corestory.idempiere.notifications.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Per-user opt-in registry. iDempiere parity: {@code MUser.IsNoEMail} (inverse — legacy
 * was opt-out, we are opt-in by default).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "notification_subscriptions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel", "event_type"})
)
public class NotificationSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private NotificationChannel channel;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "is_subscribed", nullable = false)
    private Boolean subscribed = Boolean.TRUE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationSubscription that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
