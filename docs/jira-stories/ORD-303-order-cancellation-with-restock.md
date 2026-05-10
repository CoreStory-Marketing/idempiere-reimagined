# ORD-303 — Order cancellation with compensating restock

**Type:** Feature
**Priority:** Medium
**Status:** Backup story (takeaway only — not recorded)
**Components:** orders-service, inventory-service, notifications-service, frontend
**Tier:** Complex (multi-service, compensating-transaction logic)

## User story

As a customer service rep, I want to cancel a confirmed order and have inventory automatically released back to stock, so that the cancelled qty is immediately available for other orders. The customer should also receive an email confirmation of the cancellation.

## Acceptance criteria

1. `POST /orders/{id}/cancel` on a `CONFIRMED` order:
   - Transitions order to `CANCELLED` with reason captured in `order_status_history.reason`
   - Emits `OrderCancelledEvent` on `orders.events`
2. `inventory-service` consumes `OrderCancelledEvent`:
   - Finds all `ACTIVE` reservations for the order
   - Sets each reservation status to `CANCELLED`
   - Decrements `qty_reserved` on the corresponding `stock_levels` row
   - Writes a `stock_movements` ledger entry per reservation (movement_type=`ADJUSTMENT`, reverse qty)
3. `notifications-service` consumes `OrderCancelledEvent`:
   - Sends customer email via the existing `EmailNotificationAdapter`
   - Logs an accounting record
4. **Compensating-transaction property:** if the inventory release fails (e.g., `stock_levels` row not found because someone deleted the warehouse), the cancellation should NOT roll back. The order remains cancelled; a `compensation_failures` row is written for ops to handle manually. (This requires a new table — schema gap in target.)
5. Frontend "Cancel Order" button on `/orders/[id]`: enabled for `DRAFT` and `CONFIRMED` orders, disabled otherwise. Cancelling shows a confirmation dialog with reason input.
6. Tests cover:
   - Happy path cancel → reservation released → email sent → accounting logged
   - Cancellation of DRAFT (no reservations to release)
   - Compensating failure: reservations release fails, order stays cancelled, compensation_failures row written
   - Cancellation of SHIPPED order rejected with 409

## Out of scope

- Cancel after partial shipment (separate story)
- Refund processing (separate story)
- Re-pricing on cancel-with-restock (no price changes)

## iDempiere parity reference

iDempiere uses `MOrder.voidIt()` and `DocAction` reversals — different paradigm because legacy uses two-phase document close. We're using event-driven compensation, which is a clean modernization narrative the gap-analysis should call out.

## Notes

This is the **complex tier** backup. It exercises:
- Multi-service event flow (orders → inventory → notifications)
- Compensating-transaction pattern (no shared DB, no XA)
- Schema extension needed (compensation_failures table — new V-migration)

The skill should plan this as a multi-step implementation with a HITL gate after the gap report.

**Markdown-only:** no test fixtures, no skeleton implementation. Suitable for a longer demo if you want to show the skill on a non-trivial flow.
