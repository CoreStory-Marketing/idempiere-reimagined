package com.corestory.idempiere.notifications.config;

import org.springframework.context.annotation.Configuration;

/**
 * SMTP / mail configuration is bound automatically by Spring Boot from
 * {@code spring.mail.*} in {@code application.yml} (MailHog at {@code localhost:1025}
 * for local development). This class is a placeholder for future custom
 * {@code JavaMailSenderImpl} customization (e.g., per-tenant from-address override).
 */
@Configuration
public class MailConfig {
}
