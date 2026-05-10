# Guardrails mapping — the six rules

> Each of the six engineering guardrails mapped to (a) the file that enforces it, (b) the skill phase that fires it, (c) the spec section the agent must consult.

| # | Rule | Enforced in | Skill phase | Spec section |
|---|---|---|---|---|
| 1 | **Read the relevant spec before coding** | `AGENTS.md` § "Six guardrails" + skill prompts | `brownfield-feature-implementation` Phase 4 (implementation reads `docs/design-spec.md`, `docs/technical-spec.md`, `docs/api-contracts/<service>.md`) | All of `docs/design-spec.md` § 1–10; `docs/technical-spec.md` § 2–8 |
| 2 | **Search for similar existing implementations before writing new ones** | `AGENTS.md` § "Six guardrails" + skill prompts | `brownfield-feature-implementation` Phase 4 (search step before code emission); `dual-store-gap-analysis` Phase 4 (sweep target for existing surface) | `docs/technical-spec.md` § 5 (design patterns); `docs/design-spec.md` § 1 (capabilities) |
| 3 | **Reuse current patterns** | `AGENTS.md`, `docs/technical-spec.md` § 4 (coding conventions), `.github/copilot-instructions.md` | All implementation phases — pattern-matching on naming/layering/event shapes/tests | `docs/technical-spec.md` § 4 (naming, DTO/entity boundary, audit columns, transactions); § 5 (port/adapter) |
| 4 | **Generate or update tests for every change** | `AGENTS.md`, skill prompts | `brownfield-feature-implementation` Phase 5 (tests run green before reporting) | `docs/technical-spec.md` § 6 (test strategy: 1.5x ratio, Testcontainers, failure-mode tests) |
| 5 | **Explain architectural impact when proposing a plan** | Skill prompts (Phase 3 HITL gate) | `dual-store-gap-analysis` Phase 6 (synthesize gap report) and Phase 7 (validate); `brownfield-feature-implementation` Phase 3 (present plan, wait for approval) | `docs/design-spec.md` § 10 (iDempiere parity table — the architectural-impact reference) |
| 6 | **Don't change public contracts without approval; update DESIGN_SPEC after code changes** | `AGENTS.md` § "Forbidden actions"; skill prompts | `brownfield-feature-implementation` Phase 6 (spec touch-up) | `docs/api-contracts/<service>.md` (the contracts that must not change without approval); `docs/design-spec.md` (gets the one-line update) |

## Forbidden actions (a 7th implicit guardrail)

- Never modify the legacy iDempiere repository at `/Users/johnives/Downloads/Claude Context/idempiere/` — it's read-only.
- Never reuse iDempiere code — clean-room only.
- Never modify Flyway migrations once committed — always add a new V-migration.
- Never expose JPA entities in controllers — DTO at the boundary.
- Never use `double`/`float` for money or qty — `BigDecimal` always.

These appear in `AGENTS.md`, `CLAUDE.md`, and `.github/copilot-instructions.md`.

## How the skill enforces them

- **Pre-implementation gate:** the dual-store gap analysis surfaces every gap. If any gap implies legacy modification, the skill halts.
- **Implementation gate (HITL):** the user reviews the gap report and the plan before code lands. Acceptance is explicit.
- **Test gate:** the skill runs tests. Failures are surfaced, not suppressed.
- **Spec gate:** post-implementation, the skill updates DESIGN_SPEC with a one-line entry.

Each gate corresponds to one or more guardrails. The result: the six rules are not an honor system; they're enforceable checkpoints.
