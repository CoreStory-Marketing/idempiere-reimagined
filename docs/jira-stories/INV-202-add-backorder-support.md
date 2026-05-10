# INV-202 — Add backorder support to inventory

**Type:** Feature
**Priority:** Medium
**Status:** Backup story (takeaway only — not recorded)
**Components:** inventory-service, frontend
**Tier:** Easy (single-service, no inter-service event)

## User story

As an operations user, I want partially-fulfillable orders to record a backorder line for the unmet qty, so we can replenish and complete them later instead of rejecting the whole order.

## Acceptance criteria

1. When `ReservationService.reserve()` is called and the requested qty exceeds available `qty_on_hand - qty_reserved` for the product across all warehouses, the service:
   - Creates reservations for the qty that is available (best-effort)
   - Creates a `replenishment_orders` row for the unmet qty (`status=PENDING`, `qty_required=<unmet qty>`)
   - Returns a `BackorderResult` indicating both fulfilled-now and backordered amounts
2. New endpoint: `GET /products/{id}/backorders` returns the open `replenishment_orders` for that product.
3. New endpoint: `POST /replenishment-orders/{id}/fulfill` decrements stock and updates the order status when stock arrives.
4. Frontend `/inventory` page shows a "Backorder" column when a product has open `replenishment_orders`. Clicking the cell drills into a backorder list.
5. Tests cover: partial reservation creates a backorder row, fulfilling the backorder updates statuses, racing reservations don't double-book.

## Out of scope

- Multi-location backorder routing
- Customer notification when backorder fulfills (future story)
- Backorder priority queue

## iDempiere parity reference

iDempiere has `M_Replenish` and `M_RequisitionLine` for similar flows. **Read-only reference at `/Users/johnives/Downloads/Claude Context/idempiere/`.**

Run `dual-store-gap-analysis` against this story to surface what exists in target's `replenishment_orders` schema (which is already defined) vs what's missing in the service layer (the actual partial-fulfillment logic and frontend column).

## Notes

This story is intentionally simpler than SHIP-101 — single-service scope, no inter-service event, no template rendering. Useful for letting a team try the skill against an easier ticket.

**Markdown-only:** no test fixtures, no skeleton implementation. The skill is the takeaway; this is a target it can fire against.
