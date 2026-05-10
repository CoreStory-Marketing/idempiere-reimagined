package com.corestory.idempiere.inventory.model;

/**
 * Lifecycle states for a {@link Reservation}. Mirrors iDempiere's
 * {@code MStorageReservation} working-set status flag.
 *
 * <ul>
 *   <li>{@link #ACTIVE} — created when an order is confirmed; counts against
 *       {@link StockLevel#getQtyReserved()}.</li>
 *   <li>{@link #FULFILLED} — shipping-service has consumed the reservation.</li>
 *   <li>{@link #EXPIRED} — TTL passed without fulfillment; the
 *       reservation-expiry scheduler decremented {@code qty_reserved} and emitted a release ledger entry.</li>
 *   <li>{@link #CANCELLED} — explicitly released via DELETE /reservations/{id} or order cancellation.</li>
 * </ul>
 */
public enum ReservationStatus {
    ACTIVE,
    FULFILLED,
    EXPIRED,
    CANCELLED
}
