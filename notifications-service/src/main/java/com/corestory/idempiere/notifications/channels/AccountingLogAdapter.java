package com.corestory.idempiere.notifications.channels;

import com.corestory.idempiere.common.ports.NotificationSender;
import org.springframework.stereotype.Component;

/**
 * <b>STUB.</b> Logs the message into {@code notification_log} for accounting-team consumption.
 */
@Component
public class AccountingLogAdapter implements NotificationSender {

    @Override
    public String channelCode() {
        return "ACCOUNTING";
    }

    @Override
    public SendResult send(SendRequest request) {
        throw new UnsupportedOperationException("Pending SHIP-101 implementation");
    }
}
