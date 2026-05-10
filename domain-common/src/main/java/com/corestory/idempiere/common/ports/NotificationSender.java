package com.corestory.idempiere.common.ports;

import java.util.Map;

/**
 * Port for any side-effecting notification dispatch. Adapters implement per-channel logic
 * (email via SMTP, in-app via DB row, warehouse log via DB row, accounting log via DB row, sms).
 *
 * <p>iDempiere parity: {@code MClient.sendEMail()} (lib/org.adempiere.base/src/org/compiere/model/MClient.java)
 * is the legacy single-channel dispatch. We generalize with this port so the demo can show
 * the gap-analysis adding three new channel adapters in one ticket.
 */
public interface NotificationSender {

    String channelCode();

    SendResult send(SendRequest request);

    record SendRequest(
        String recipient,
        String subject,
        String body,
        String relatedEventType,
        Long relatedEventId,
        String dedupKey,
        Map<String, Object> context
    ) {}

    record SendResult(
        boolean success,
        String externalId,
        String errorCode,
        String errorMessage
    ) {
        public static SendResult ok(String externalId) {
            return new SendResult(true, externalId, null, null);
        }

        public static SendResult fail(String errorCode, String errorMessage) {
            return new SendResult(false, null, errorCode, errorMessage);
        }
    }
}
