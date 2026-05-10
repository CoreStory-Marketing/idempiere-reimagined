# Grounded vs ungrounded — side-by-side

> A single prompt run against an agent without intel-store grounding vs. with.

Prompt: **"Add a `backorder_qty` column to the inventory products table."**

## Ungrounded (no env loaded — generic Spring Boot agent)

```diff
- # The agent guesses based on common patterns:
+ ALTER TABLE products ADD COLUMN backorder_qty NUMERIC(19,4) NOT NULL DEFAULT 0;

  # In Product.java:
+ @Column(name = "backorder_qty")
+ private BigDecimal backorderQty = BigDecimal.ZERO;

  # New endpoint guess:
+ @PostMapping("/products/{id}/backorder")
+ public ProductDto setBackorder(@PathVariable Long id, @RequestParam BigDecimal qty) { ... }
```

**Problems:**
- The schema in `idempiere-reimagined` separates `products` (catalog) from `stock_levels` (per-warehouse on-hand/reserved). Backorder qty is a **per-product-per-warehouse** concept — it belongs on `stock_levels`, not `products`.
- The repo already has a `replenishment_orders` table designed for this exact concept. The agent reinvents instead of reusing.
- The new endpoint `POST /products/{id}/backorder` doesn't match the project's CRUD conventions.
- No tests, no Flyway migration version, no event emission.

The output looks reasonable in isolation but is **wrong in context**.

## Grounded (env loaded — `dual-store-gap-analysis` runs first)

```markdown
## Gap report — Add backorder support to inventory

### 1. Existing capabilities (RO)
- Target already has `replenishment_orders` table with (product_id, warehouse_id, qty_required, status).
  See: inventory-service/src/main/resources/db/migration/V1__init.sql:182
- iDempiere has `M_Replenish` and `M_RequisitionLine` for similar flows.
  See: CoreStory project 457, MRequisitionLine.java

### 2. Data model (DM)
- DM-001: `replenishment_orders` exists in target but no service-layer logic to populate it.
  Resolution: extend `ReservationService.reserve()` to write a row when stock is insufficient.
- DM-002: No need to add `backorder_qty` to `products` — that would conflate catalog with operational state.

### 3. UI (UI)
- UI-001: `/inventory` page has no "Backorder" column.
  Resolution: add a column rendered when `replenishment_orders` exist for the product.

### 4. Business logic (BL)
- BL-001: `ReservationService.reserve(productId, qty, ...)` currently throws `InsufficientStockException`
  on shortfall. Resolution: change semantics to return a `BackorderResult { fulfilledNow, backordered }`,
  create the `replenishment_orders` row.

### 7. Constraints (CO)
- CO-001: `qty_reserved` on `stock_levels` must not double-count backordered qty.
- CO-002: Tests must cover concurrent reservation race that produces a partial-fulfill + backorder atomically.

### Implementation plan
1. New Flyway V2__backorder_status.sql (add column to replenishment_orders)
2. ReservationService.reserve() returns BackorderResult; create replenishment_orders row on shortfall
3. New GET /products/{id}/backorders endpoint
4. POST /replenishment-orders/{id}/fulfill
5. Frontend column on /inventory
6. Tests at the 1.5x ratio per project convention
```

**Differences:**
- No fabricated columns. Reuses existing schema.
- Five gaps, each with a concrete file + line citation.
- Implementation plan respects the project's conventions (Flyway versioning, DTO at boundary, test ratio, naming).
- Idempotency, concurrency, and DTO shapes called out before any code is written.

The grounded run is **the difference between a sample app and a working modernization**.

## Why this matters

- **Time saved:** the grounded run produces a plan in minutes that a senior engineer would otherwise discover in days of code-spelunking.
- **Quality:** the grounded plan respects existing patterns. The ungrounded plan invents.
- **Trust:** every assertion in the grounded plan is citable. The ungrounded plan is asserting from training data.

This is the pitch for `dual-store-gap-analysis` + `brownfield-feature-implementation`. Drop this page into a slide if you need a one-pager for a steering committee.
