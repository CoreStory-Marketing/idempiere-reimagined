package com.corestory.idempiere.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Supplies the auditor identity for {@code @CreatedBy} / {@code @LastModifiedBy} fields.
 *
 * <p>The audit hook is enabled via {@code @EnableJpaAuditing} on
 * {@link com.corestory.idempiere.warehouse.WarehouseApplication}. We default to the literal
 * {@code "system"} until the gateway propagates an authenticated principal.
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
     * fields on {@code AuditableEntity}. Referenced via
     * {@code dateTimeProviderRef = "auditingDateTimeProvider"} on {@code @EnableJpaAuditing}.
     */
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
