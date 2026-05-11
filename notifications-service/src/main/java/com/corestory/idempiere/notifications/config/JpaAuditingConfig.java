package com.corestory.idempiere.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.time.OffsetDateTime;
import java.util.Optional;

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
