package com.corestory.idempiere.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * notifications-service entry point.
 *
 * <p><b>Build state: STUB.</b> Controller stub returns 501; {@code NotificationSender}
 * port declared in {@code domain-common}; Artemis listener for {@code shipments.events}
 * is wired but the {@code onShipmentCreated} handler is empty (logs + exits). The recorded
 * demo fills in the three-channel fan-out (email via SMTP/MailHog, warehouse log, accounting
 * log) and template rendering.
 */
@SpringBootApplication(scanBasePackages = {
    "com.corestory.idempiere.notifications",
    "com.corestory.idempiere.common"
})
@EnableJms
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableScheduling
public class NotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
}
