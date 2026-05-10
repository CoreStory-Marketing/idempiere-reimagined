package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.orders.exception.IllegalStateTransitionException;
import com.corestory.idempiere.orders.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link OrderStateMachine} transition rules — these encode
 * the state-machine contract documented in §5.1 of the SOW and pinned in
 * {@code application.yml#idempiere.state-machine.transitions}.
 */
class OrderStateMachineTest {

    private OrderStateMachine sm;

    @BeforeEach
    void setUp() {
        sm = new OrderStateMachine();
    }

    @ParameterizedTest(name = "{0} -> {1} should be allowed")
    @CsvSource({
        "DRAFT,CONFIRMED",
        "DRAFT,CANCELLED",
        "PENDING,CONFIRMED",
        "PENDING,CANCELLED",
        "CONFIRMED,SHIPPED",
        "CONFIRMED,CANCELLED",
        "SHIPPED,INVOICED",
        "INVOICED,COMPLETE"
    })
    @DisplayName("Allowed transitions follow the documented state machine")
    void allowedTransitions(OrderStatus from, OrderStatus to) {
        assertThat(sm.canTransition(from, to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} should be REJECTED")
    @CsvSource({
        // Cannot skip steps
        "DRAFT,SHIPPED",
        "DRAFT,INVOICED",
        "DRAFT,COMPLETE",
        "CONFIRMED,INVOICED",
        "CONFIRMED,COMPLETE",
        "SHIPPED,COMPLETE",
        // Cannot go backwards
        "CONFIRMED,DRAFT",
        "SHIPPED,CONFIRMED",
        "INVOICED,SHIPPED",
        "COMPLETE,INVOICED",
        // Cannot cancel post-shipment
        "SHIPPED,CANCELLED",
        "INVOICED,CANCELLED",
        "COMPLETE,CANCELLED",
        // Terminal states are terminal
        "CANCELLED,DRAFT",
        "CANCELLED,CONFIRMED",
        "COMPLETE,CONFIRMED",
        "VOIDED,CONFIRMED"
    })
    @DisplayName("Disallowed transitions are rejected")
    void disallowedTransitions(OrderStatus from, OrderStatus to) {
        assertThat(sm.canTransition(from, to)).isFalse();
    }

    @Test
    @DisplayName("requireTransition throws on disallowed transitions")
    void requireTransitionThrows() {
        assertThatThrownBy(() -> sm.requireTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED))
            .isInstanceOf(IllegalStateTransitionException.class)
            .hasMessageContaining("SHIPPED")
            .hasMessageContaining("CANCELLED");
    }

    @Test
    @DisplayName("requireTransition does not throw on allowed transitions")
    void requireTransitionAllowed() {
        sm.requireTransition(OrderStatus.DRAFT, OrderStatus.CONFIRMED);
        sm.requireTransition(OrderStatus.CONFIRMED, OrderStatus.SHIPPED);
        sm.requireTransition(OrderStatus.SHIPPED, OrderStatus.INVOICED);
        sm.requireTransition(OrderStatus.INVOICED, OrderStatus.COMPLETE);
    }

    @Test
    @DisplayName("Null inputs are rejected (no NPEs)")
    void nullInputs() {
        assertThat(sm.canTransition(null, OrderStatus.CONFIRMED)).isFalse();
        assertThat(sm.canTransition(OrderStatus.DRAFT, null)).isFalse();
        assertThat(sm.canTransition(null, null)).isFalse();
    }

    @Test
    @DisplayName("nextStates exposes the legal outgoing edges")
    void nextStates() {
        assertThat(sm.nextStates(OrderStatus.DRAFT))
            .containsExactlyInAnyOrder(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
        assertThat(sm.nextStates(OrderStatus.CONFIRMED))
            .containsExactlyInAnyOrder(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
        assertThat(sm.nextStates(OrderStatus.SHIPPED))
            .containsExactly(OrderStatus.INVOICED);
        assertThat(sm.nextStates(OrderStatus.INVOICED))
            .containsExactly(OrderStatus.COMPLETE);
        assertThat(sm.nextStates(OrderStatus.COMPLETE)).isEmpty();
        assertThat(sm.nextStates(OrderStatus.CANCELLED)).isEmpty();
    }
}
