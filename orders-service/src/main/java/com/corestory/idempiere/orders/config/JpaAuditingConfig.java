package com.corestory.idempiere.orders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Wires up the {@link AuditorAware} bean used by Spring Data JPA Auditing
 * to populate {@code @CreatedBy} and {@code @LastModifiedBy} fields on
 * {@link com.corestory.idempiere.orders.model.AuditableEntity}.
 *
 * <p>The actual {@code @EnableJpaAuditing} annotation is on
 * {@link com.corestory.idempiere.orders.OrdersApplication}.
 *
 * <p>For now we always return {@code "system"} — Rupam's brief explicitly says
 * the demo uses a single hardcoded admin and we should not build a real user store.
 * Once an authenticated principal is available this should be replaced with
 * {@code SecurityContextHolder} lookup.
 */
@Configuration
public class JpaAuditingConfig {

    public static final String SYSTEM_PRINCIPAL = "system";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(SYSTEM_PRINCIPAL);
    }
}
