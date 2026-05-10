package com.corestory.idempiere.notifications.channels;

import com.corestory.idempiere.common.ports.NotificationSender;
import org.springframework.stereotype.Component;

/**
 * <b>STUB.</b> The recorded brownfield-feature-implementation demo (SHIP-101) wires this
 * up to {@code JavaMailSender} (which the application.yml already binds to MailHog).
 */
@Component
public class EmailNotificationAdapter implements NotificationSender {

    @Override
    public String channelCode() {
        return "EMAIL";
    }

    @Override
    public SendResult send(SendRequest request) {
        throw new UnsupportedOperationException("Pending SHIP-101 implementation");
    }
}
