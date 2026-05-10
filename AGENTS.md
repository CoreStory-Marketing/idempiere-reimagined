# AGENTS.md — `idempiere-reimagined`

> Universal cross-harness instruction file per the [agents.md spec](https://agents.md). Read by Codex, Aider, Cursor, Factory.ai, Jules, Gemini CLI, Windsurf, GitHub Copilot's coding agent, JetBrains Junie, Warp.

This is a 40%-built Spring Boot reimagining of iDempiere's logistics domain. **Two intelligence stores back this repo:**

- **Legacy:** iDempiere — CoreStory project ID **457** (`Product-Marketing-Lab` org).
  Query via `mcp__corestoryProduct-Marketing-Lab__send_message` with `project_id=457`.
  **DO NOT trust the static PRD/tech-spec for legacy** — use `send_message` only. The static artifacts have known accuracy gaps (see `idempiere-corestory-audit.md`).
- **Target:** This repo — CoreStory project ID **{TBD — fill in after ingestion completes; see `docs/design-spec.md`}** (also `Product-Marketing-Lab` org). Query via the same MCP tool with the target `project_id`.

## Forbidden actions

- **Never modify the legacy iDempiere repository.** It is at `/Users/johnives/Downloads/Claude Context/idempiere/` for read-only reference. If a feature requires changes to legacy code, that's a sign the gap analysis is wrong — re-run the analysis, do not reach into legacy.
- **Never change public API contracts** in `docs/api-contracts/` without explicit approval.
- **Never modify the database schema** of services marked "Full" without going through Flyway migrations (`src/main/resources/db/migration/V<N>__<name>.sql`).
- **Never reuse iDempiere code.** This repo is clean-room. Reference iDempiere via CoreStory queries for behavior parity only.

## Six guardrails (encoded as enforceable rules)

These are checked at every phase of the `brownfield-feature-implementation` skill:

1. **Read the relevant spec before coding.** Consult `docs/design-spec.md`, `docs/technical-spec.md`, and `docs/api-contracts/<service>.md` before writing new code.
2. **Search for similar existing implementations in this repo before writing new ones.** Prefer extending existing patterns to inventing.
3. **Reuse current patterns** — naming (`*Service`, `*Repository`, `*Controller`, `*Event`), layering, event shapes, test structure.
4. **Generate or update tests for every change.** Use Testcontainers for integration tests, Mockito for unit tests.
5. **Explain architectural impact when proposing a plan.** The `dual-store-gap-analysis` output is the structured form of this.
6. **Don't change public contracts without approval.** Update `docs/design-spec.md` after substantive code changes.

## Activation pointers — when to invoke which skill

- For any JIRA-style ticket implementing a feature in this repo (e.g., `SHIP-101`, `INV-202`, `ORD-303`, generic `PROJ-NNN`): invoke **`brownfield-feature-implementation`**. It calls `dual-store-gap-analysis` internally and gates implementation on human approval.
- For pure gap analysis without implementation: invoke **`dual-store-gap-analysis`** directly.

Skill files:
- `.claude/skills/brownfield-feature-implementation/SKILL.md` (Claude Code)
- `.claude/skills/dual-store-gap-analysis/SKILL.md` (Claude Code)
- This file's "Skill bundle" section below (universal)
- `.github/copilot-instructions.md` (Copilot, with section heading per skill)

## Skill bundle (inline, for harnesses that don't read `.claude/skills/`)

### `brownfield-feature-implementation`

> Activates on JIRA-ID or "implement {feature} in target" requests. End-to-end ticket → implementation flow:
> 1. Ticket intake (verify both intel stores reachable)
> 2. Run `dual-store-gap-analysis` skill
> 3. **HITL gate** — present gap report + plan; wait for explicit user approval
> 4. Implement in target only; refuse to modify legacy
> 5. Generate/update tests; run them green
> 6. Update `docs/design-spec.md` with a one-line addition
> 7. Report changes

### `dual-store-gap-analysis`

> Run a feature gap analysis across two CoreStory intel stores (legacy + target). Phases:
> 1. Identify both stores via `list_projects`
> 2. Create paired conversations (resume legacy conversation 5276 if shipping-notification flow)
> 3. Load context (paste JIRA story into both)
> 4. Seven-category sweep against TARGET (RO/DM/UI/BL/RE/IG/CO)
> 5. Seven-category sweep against LEGACY using verified prompt template
> 6. Synthesize gap report (per-category table + numbered gap inventory + plan)
> 7. Validate (every AC addressed; every gap closeable in target only)
> 8. Output structured markdown

## Project structure

```
idempiere-reimagined/
├── domain-common/                 # Shared DTOs, events, ports
├── api-gateway/                   # Spring Cloud Gateway + JWT
├── orders-service/                # FULL — state machine + events
├── inventory-service/             # FULL — listener + reservation
├── warehouse-service/             # HALF — receiving works, picking stub
├── shipping-service/              # STUB — controllers return 501
├── notifications-service/         # STUB — port + listener empty
├── frontend/                      # Next.js 14
├── docs/
│   ├── design-spec.md
│   ├── technical-spec.md
│   ├── api-contracts/<service>.md
│   ├── jira-stories/SHIP-101-*.md  (recorded)
│   ├── jira-stories/INV-202-*.md   (backup)
│   ├── jira-stories/ORD-303-*.md   (backup)
│   ├── grounded-vs-ungrounded.md
│   └── guardrails-mapping.md
├── .claude/skills/
│   ├── brownfield-feature-implementation/SKILL.md
│   └── dual-store-gap-analysis/SKILL.md
└── .github/copilot-instructions.md
```

## CoreStory operational facts

| Item | Value |
|---|---|
| Legacy project ID | 457 (`idempiere`) |
| Org for both projects | `Product-Marketing-Lab` |
| Live conversation API | Healthy. ~80–110s per round-trip. Self-flags `[unverified]` conservatively. |
| Verified dry-run conversation | 5276 — Demo Dry-Run for Shipping Notification Flow |
| Static PRD/tech-spec | **DO NOT SURFACE.** Use `send_message` only for legacy. |
| Target project ID | TBD after ingestion (Day 2 in the build sequence) |

For more detail see `docs/design-spec.md` (the iDempiere parity table and the `[unverified]` items to verify on-the-fly during the recorded demo).
