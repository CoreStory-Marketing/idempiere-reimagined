# CLAUDE.md — `idempiere-reimagined`

> Claude Code project memory. Slim by design — defers to `AGENTS.md` for the universal install and `.claude/skills/` for skill definitions.

## Read first

- **`AGENTS.md`** — full instruction set, including forbidden actions, six guardrails, skill activation pointers
- **`docs/design-spec.md`** — application-level capabilities, services, APIs, schemas, events, iDempiere parity table
- **`docs/technical-spec.md`** — architecture style, package structure, frameworks, conventions

## Available skills

Loaded automatically from `.claude/skills/`:

- **`brownfield-feature-implementation`** — invoke for JIRA-style ticket implementation against this 40%-built target. Triggers: "implement SHIP-101", "build this feature in the new system", JIRA-ID format.
- **`dual-store-gap-analysis`** — invoke for standalone gap analysis across legacy (CoreStory 457) and target. Triggers: "dual-store gap analysis", "compare legacy and target for {feature}".

## Six guardrails (enforceable)

1. Read the relevant spec before coding.
2. Search for similar existing implementations before writing new ones.
3. Reuse current patterns.
4. Generate or update tests for every change.
5. Explain architectural impact when proposing a plan.
6. Don't change public contracts without approval; update DESIGN_SPEC after code changes.

## Two intelligence stores

- **Legacy:** CoreStory project **457** (iDempiere) — `mcp__corestoryProduct-Marketing-Lab__send_message`. **Use `send_message` only**, not the static PRD/tech-spec.
- **Target:** This repo — CoreStory project **458** (`Product-Marketing-Lab` org). Same MCP tool. See `AGENTS.md` for full operational facts.

## Hard constraints

- **Never modify the legacy iDempiere repo** (`/Users/johnives/Downloads/Claude Context/idempiere/` is read-only).
- **Never reuse iDempiere code.** Clean-room only.
- **Never modify Flyway migrations once committed** — always add a new V-number.
- **Never expose JPA entities in controllers** — DTO at the boundary.
- **Use BigDecimal for money/qty** — never double.
