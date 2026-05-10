package com.corestory.idempiere.common.events;

/**
 * Canonical Artemis topic names. Keep service yaml ({@code idempiere.events.*}) in sync.
 */
public final class EventTopics {

    public static final String ORDERS = "orders.events";
    public static final String INVENTORY = "inventory.events";
    public static final String WAREHOUSE = "warehouse.events";
    public static final String SHIPMENTS = "shipments.events";
    public static final String NOTIFICATIONS = "notifications.events";

    private EventTopics() {}
}
