# Schema digest — `notifications-service`

> Authoritative source: `notifications-service/src/main/resources/db/migration/V1__init.sql` (+ `V99__seed_templates.sql` for seed data).

## Tables

```
notification_groups         code, name, description                                [seeded: customer-events,
                                                                                     warehouse-events,
                                                                                     accounting-events,
                                                                                     system-alerts]
notification_group_members  group_id → notification_groups, user_id
email_templates             code, subject_template, body_template, body_format, language, *audit
                                                                                  [seeded: 8 templates including
                                                                                     shipment.confirmation.customer,
                                                                                     shipment.warehouse.acknowledgment,
                                                                                     shipment.accounting.record]
email_template_translations template_id → email_templates, language, subject_template, body_template
notification_subscriptions  user_id, channel, event_type, is_subscribed              [opt-in registry]
notification_log            channel, recipient, subject, body_rendered, status,
                            related_event_id, related_event_type, dedup_key UNIQUE,
                            sent_at, error_message, retry_count, *audit
                                                                                    [the table SHIP-101's
                                                                                     warehouse + accounting records
                                                                                     write to]
email_outbox                to_address, from_address, subject, body, status,
                            attempted_at, smtp_response                            [per-message outbound log,
                                                                                     mirrors X_AD_UserMail]
in_app_notifications        recipient_user_id, title, message, is_read, link_url,
                            created_at                                              [mirrors AD_Note]
delivery_attempts           notification_log_id → notification_log, attempt_no,
                            attempted_at, status, error_code, error_message
```

## Notification log channel codes

CHECK constraint on `notification_log.channel`:
- `EMAIL` — sent via SMTP / SES adapter
- `WAREHOUSE` — internal log entry for warehouse ops
- `ACCOUNTING` — internal log entry for accounting
- `SMS` — out of scope for SHIP-101 (adapter stays stubbed)
- `IN_APP` — writes also to `in_app_notifications`

CHECK on `notification_log.status`:
- `PENDING` → `SENT` (success) | `FAILED` (adapter threw) | `SKIPPED` (subscription opt-out)

## Idempotency

`notification_log.dedup_key` is UNIQUE. Convention (set by SHIP-101 implementation):
```
dedup_key = "<eventId>:<channelCode>"
```
Replays of the same `ShipmentCreatedEvent` to the same channel are no-ops.

## iDempiere parity

| Target | Legacy |
|---|---|
| `email_templates` | `R_MailText` |
| `email_template_translations` | `R_MailText_Trl` |
| `email_outbox` | `X_AD_UserMail` |
| `in_app_notifications` | `MNote` (`AD_Note`) |

The `MMailText.getMailText(boolean all, boolean parsed)` method in legacy is the precedent for `TemplateRenderer.render()` in target. Same `@variable@` substitution syntax. The actual legacy method that does the substitution is named `parse()` (verified in `MMailText.java:128`), not `parseVariables()` as some intel-store responses say.
