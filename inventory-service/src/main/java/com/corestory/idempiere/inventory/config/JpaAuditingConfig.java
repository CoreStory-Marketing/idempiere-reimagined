package com.corestory.idempiere.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Supplies {@code created_by} / {@code updated_by} for the audit columns on
 * {@link com.corestory.idempiere.inventory.model.AuditableEntity}.
 *
 * <p>For now we hard-code the auditor to "system" — once the gateway threads a JWT
 * context through, this can lift the principal from the SecurityContext.
 *
 * <p>Note: Spring Data Auditing's default {@code DateTimeProvider} resolves the temporal
 * value based on the field type (handles {@link java.time.OffsetDateTime} natively in
 * Spring Data JPA 3.x).
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }

    /**
     * Returns {@link OffsetDateTime} so Spring Data Auditing can populate the
     * {@code OffsetDateTime}-typed {@code @CreatedDate}/{@code @LastModifiedDate}
     * fields on {@code AuditableEntity}. The default provider returns
     * {@link java.time.LocalDateTime}, which the auditing layer cannot convert.
     * Referenced via {@code dateTimeProviderRef = "auditingDateTimeProvider"} on
     * {@code @EnableJpaAuditing}.
     */
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
