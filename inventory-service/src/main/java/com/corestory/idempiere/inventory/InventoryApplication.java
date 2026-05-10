package com.corestory.idempiere.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * inventory-service entry point.
 *
 * Listens for {@code OrderConfirmedEvent}, decrements stock via reservations with TTL,
 * emits {@code inventory.reserved}. Background scheduler releases expired reservations.
 */
@SpringBootApplication(scanBasePackages = {
    "com.corestory.idempiere.inventory",
    "com.corestory.idempiere.common"
})
@EnableJms
@EnableJpaAuditing
@EnableScheduling
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
