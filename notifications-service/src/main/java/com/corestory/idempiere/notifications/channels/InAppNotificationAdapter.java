package com.corestory.idempiere.notifications.channels;

import com.corestory.idempiere.common.ports.NotificationSender;
import org.springframework.stereotype.Component;

/**
 * <b>STUB.</b> SHIP-101 implements: insert into {@code in_app_notifications} keyed by
 * {@code recipient_user_id}.
 */
@Component
public class InAppNotificationAdapter implements NotificationSender {

    @Override
    public String channelCode() {
        return "IN_APP";
    }

    @Override
    public SendResult send(SendRequest request) {
        throw new UnsupportedOperationException("Pending SHIP-101 implementation");
    }
}
