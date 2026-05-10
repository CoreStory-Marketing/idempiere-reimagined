package com.corestory.idempiere.warehouse.model;

/**
 * Pick lifecycle status — STUBBED. Used by {@link Pick} but no service-layer transitions
 * are wired yet. The recorded brownfield-feature-implementation demo will fill this in.
 */
public enum PickStatus {
    DRAFT,
    RELEASED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
