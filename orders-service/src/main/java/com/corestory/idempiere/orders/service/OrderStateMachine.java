package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.orders.exception.IllegalStateTransitionException;
import com.corestory.idempiere.orders.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Encodes the order document lifecycle as a small, well-typed state machine.
 *
 * <p>Allowed transitions (matches {@code idempiere.state-machine.transitions} in
 * {@code application.yml}):
 * <pre>
 *     DRAFT      → CONFIRMED, CANCELLED
 *     CONFIRMED  → SHIPPED, CANCELLED
 *     SHIPPED    → INVOICED
 *     INVOICED   → COMPLETE
 * </pre>
 *
 * <p>{@code CANCELLED} is a terminal sink reachable only before SHIPPED.
 * {@code COMPLETE} is the happy-path terminal.
 *
 * <p>iDempiere parity: {@code MOrder.completeIt()} / {@code MOrder.voidIt()} on
 * {@code DocStatus}; we model the transition rules declaratively rather than
 * spread across methods.
 */
@Component
public class OrderStateMachine {

    private final Map<OrderStatus, Set<OrderStatus>> allowed = new EnumMap<>(OrderStatus.class);

    public OrderStateMachine() {
        allowed.put(OrderStatus.DRAFT,     EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        allowed.put(OrderStatus.PENDING,   EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        allowed.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        allowed.put(OrderStatus.SHIPPED,   EnumSet.of(OrderStatus.INVOICED));
        allowed.put(OrderStatus.INVOICED,  EnumSet.of(OrderStatus.COMPLETE));
        allowed.put(OrderStatus.COMPLETE,  EnumSet.noneOf(OrderStatus.class));
        allowed.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
        allowed.put(OrderStatus.VOIDED,    EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return allowed.getOrDefault(from, EnumSet.noneOf(OrderStatus.class)).contains(to);
    }

    public void requireTransition(OrderStatus from, OrderStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateTransitionException(from, to);
        }
    }

    public Set<OrderStatus> nextStates(OrderStatus from) {
        return allowed.getOrDefault(from, EnumSet.noneOf(OrderStatus.class));
    }
}
