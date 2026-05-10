package com.corestory.idempiere.inventory.model;

/** Strategy for {@link ReplenishmentRule}. iDempiere parity: {@code M_Replenish.ReplenishType}. */
public enum ReplenishmentType {
    REORDER,
    MAX,
    CUSTOM
}
