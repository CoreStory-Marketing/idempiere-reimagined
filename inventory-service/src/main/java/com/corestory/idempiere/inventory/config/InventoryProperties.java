package com.corestory.idempiere.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Strongly-typed view of {@code idempiere.*} configuration.
 *
 * <pre>
 * idempiere:
 *   events:
 *     orders-topic: orders.events
 *     inventory-topic: inventory.events
 *   reservation:
 *     ttl-hours: 24
 *     expiry-job-cron: "0 *&#47;15 * * * *"
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "idempiere")
public class InventoryProperties {

    private final Events events = new Events();
    private final Reservation reservation = new Reservation();

    public Events getEvents() { return events; }
    public Reservation getReservation() { return reservation; }

    public static class Events {
        private String ordersTopic = "orders.events";
        private String inventoryTopic = "inventory.events";

        public String getOrdersTopic() { return ordersTopic; }
        public void setOrdersTopic(String ordersTopic) { this.ordersTopic = ordersTopic; }

        public String getInventoryTopic() { return inventoryTopic; }
        public void setInventoryTopic(String inventoryTopic) { this.inventoryTopic = inventoryTopic; }
    }

    public static class Reservation {
        private int ttlHours = 24;
        private String expiryJobCron = "0 */15 * * * *";

        public int getTtlHours() { return ttlHours; }
        public void setTtlHours(int ttlHours) { this.ttlHours = ttlHours; }

        public String getExpiryJobCron() { return expiryJobCron; }
        public void setExpiryJobCron(String expiryJobCron) { this.expiryJobCron = expiryJobCron; }
    }
}
