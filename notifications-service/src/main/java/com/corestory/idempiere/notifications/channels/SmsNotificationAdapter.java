package com.corestory.idempiere.notifications.channels;

import com.corestory.idempiere.common.ports.NotificationSender;
import org.springframework.stereotype.Component;

/**
 * <b>STUB.</b> SMS dispatch — out of scope for the SHIP-101 demo (stays unimplemented).
 */
@Component
public class SmsNotificationAdapter implements NotificationSender {

    @Override
    public String channelCode() {
        return "SMS";
    }

    @Override
    public SendResult send(SendRequest request) {
        throw new UnsupportedOperationException("Pending SHIP-101 implementation");
    }
}
