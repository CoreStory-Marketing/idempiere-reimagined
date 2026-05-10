-- notifications-service Flyway baseline (STUB — port + listener empty)
-- iDempiere parity: R_MailText, R_MailText_Trl, X_AD_UserMail, AD_Note, AD_User.IsNoEMail
--                   MMailText.getMailText() — token substitution
--                   MClient.sendEMail() / MClient.SMTPHost — SMTP dispatch

CREATE TABLE notification_groups (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE notification_group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES notification_groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    UNIQUE (group_id, user_id)
);

CREATE TABLE email_templates (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    subject_template VARCHAR(512) NOT NULL,
    body_template TEXT NOT NULL,
    body_format VARCHAR(8) NOT NULL DEFAULT 'TEXT',
    language VARCHAR(8) NOT NULL DEFAULT 'en',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (body_format IN ('HTML','TEXT'))
);

CREATE TABLE email_template_translations (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES email_templates(id) ON DELETE CASCADE,
    language VARCHAR(8) NOT NULL,
    subject_template VARCHAR(512) NOT NULL,
    body_template TEXT NOT NULL,
    UNIQUE (template_id, language)
);

CREATE TABLE notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel VARCHAR(16) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    is_subscribed BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, channel, event_type)
);

CREATE TABLE notification_log (
    id BIGSERIAL PRIMARY KEY,
    channel VARCHAR(16) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(512),
    body_rendered TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    related_event_id VARCHAR(64),
    related_event_type VARCHAR(64),
    dedup_key VARCHAR(128) UNIQUE,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(1024),
    retry_count INTEGER NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 1, org_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (channel IN ('EMAIL','WAREHOUSE','ACCOUNTING','SMS','IN_APP')),
    CHECK (status IN ('PENDING','SENT','FAILED','SKIPPED'))
);

CREATE INDEX idx_notification_log_channel_status ON notification_log(channel, status);
CREATE INDEX idx_notification_log_related ON notification_log(related_event_type, related_event_id);

CREATE TABLE email_outbox (
    id BIGSERIAL PRIMARY KEY,
    to_address VARCHAR(255) NOT NULL,
    from_address VARCHAR(255) NOT NULL,
    subject VARCHAR(512),
    body TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempted_at TIMESTAMP WITH TIME ZONE,
    smtp_response VARCHAR(512),
    CHECK (status IN ('PENDING','SENT','FAILED'))
);

CREATE TABLE in_app_notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    link_url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_in_app_notifications_recipient ON in_app_notifications(recipient_user_id, is_read);

CREATE TABLE delivery_attempts (
    id BIGSERIAL PRIMARY KEY,
    notification_log_id BIGINT NOT NULL REFERENCES notification_log(id) ON DELETE CASCADE,
    attempt_no INTEGER NOT NULL,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(16) NOT NULL,
    error_code VARCHAR(64),
    error_message VARCHAR(1024)
);
