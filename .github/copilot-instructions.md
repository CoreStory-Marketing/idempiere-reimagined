# Copilot Instructions — `idempiere-reimagined`

> GitHub Copilot's coding agent reads this file. It carries the same guardrails as `AGENTS.md` but is reformatted for Copilot's prompt style.

## Project context

This is a partially-built clean-room Spring Boot reimagining of iDempiere's logistics domain — orders and inventory are full implementations, warehouse is half-built, shipping and notifications are deliberate stubs the demo agent fills in.
Five services + gateway + Next.js frontend. Stack: Spring Boot 3.4 + Apache Artemis (JMS) + Postgres-per-service + Spring Cloud Gateway + Next.js 14 + Docker Compose.

## Forbidden actions

- Never modify the legacy iDempiere repository at `/Users/johnives/Downloads/Claude Context/idempiere/`.
- Never reuse iDempiere code. Clean-room only.
- Never change public API contracts in `docs/api-contracts/` without approval.
- Never modify Flyway migrations once committed.

## Two intelligence stores

- Legacy: CoreStory project **457** — query via `mcp__corestoryProduct-Marketing-Lab__send_message`. **Static PRD/tech-spec are unreliable; use `send_message` only.**
- Target: this repo — CoreStory project ID listed in `AGENTS.md` after ingestion.

## Six guardrails

1. Read the relevant spec (`docs/design-spec.md`, `docs/technical-spec.md`, `docs/api-contracts/<service>.md`) before coding.
2. Search for similar existing implementations in this repo first.
3. Reuse current patterns — naming, layering, event shapes, test structure.
4. Generate or update tests for every change.
5. Explain architectural impact when proposing a plan.
6. Don't change public contracts without approval; update DESIGN_SPEC after code changes.

---

## Skill: `brownfield-feature-implementation`

Activates on JIRA-style ticket implementation requests, "build this feature in target", "extend the new system with X", or any prompt referencing a JIRA-ID format like SHIP-101 or PROJ-NNN.

Phases:

1. **Ticket intake** — fetch ticket, verify both intel stores reachable.
2. **Dual-store gap analysis** — invoke the `dual-store-gap-analysis` skill below. Output: structured gap report.
3. **HITL gate** — present gap report + implementation plan, wait for explicit user approval before proceeding.
4. **Implement** — generate code in target only; refuse legacy modifications.
5. **Test** — generate or update tests, run green.
6. **Spec touch-up** — update `docs/design-spec.md`.
7. **Report** — summary of changes + tests.

---

## Skill: `dual-store-gap-analysis`

Activates on dual-store gap analysis, modernization gap analysis, "compare legacy and target", or "what's missing in target vs legacy" requests.

Phases:

1. **Identify both stores** via `mcp__corestory*__list_projects`. Confirm `ingestion_status: completed` on both.
2. **Create paired conversations** — title with feature + `[legacy]` / `[target]`. Resume conversation 5276 if shipping-notification flow.
3. **Load context** — paste JIRA story.
4. **Seven-category sweep against TARGET** — RO, DM, UI, BL, RE, IG, CO.
5. **Seven-category sweep against LEGACY** — same categories, verified prompt template.
6. **Synthesize gap report** — per-category table + numbered gap IDs (DM-001, BL-001, etc.) + plan.
7. **Validate** — every AC addressed, every gap closeable in target only.
8. **Output** — structured markdown.

Constraints:

- Never modify the legacy repo. CoreStory queries only.
- Never trust the static iDempiere PRD/tech-spec. `send_message` only.
- Cite source paths in every gap row.
- Flag `[unverified]` cells when CoreStory hedges.
