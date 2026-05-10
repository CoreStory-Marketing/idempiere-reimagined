package com.corestory.idempiere.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * orders-service entry point.
 *
 * Implements: order CRUD, state machine (DRAFT→CONFIRMED→SHIPPED→INVOICED→COMPLETE),
 * pricing + tax, customers/addresses/contacts, payment-terms/price-lists.
 * Emits {@code order.confirmed}, {@code order.cancelled}, {@code order.shipped},
 * {@code order.invoiced}, {@code order.completed} on Artemis topic {@code orders.events}.
 */
@SpringBootApplication(scanBasePackages = {
    "com.corestory.idempiere.orders",
    "com.corestory.idempiere.common"
})
@EnableJpaAuditing
@EnableScheduling
public class OrdersApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersApplication.class, args);
    }
}
