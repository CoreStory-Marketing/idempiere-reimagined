package com.corestory.idempiere.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * shipping-service entry point.
 *
 * <p><b>Build state: STUB.</b> Schema + controllers + DTOs + repository interfaces only.
 * No service-layer logic. The recorded {@code brownfield-feature-implementation} demo
 * fills in the {@code POST /shipments/{id}/ship} flow + {@code ShipmentCreatedEvent} emission.
 */
@SpringBootApplication(scanBasePackages = {
    "com.corestory.idempiere.shipping",
    "com.corestory.idempiere.common"
})
@EnableJpaAuditing
public class ShippingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingApplication.class, args);
    }
}
