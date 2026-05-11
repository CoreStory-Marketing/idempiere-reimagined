---
name: brownfield-feature-implementation
description: Implement features in a partially-built modern target system using CoreStory dual-store gap analysis (legacy + target intel stores) and human-in-the-loop plan approval before code generation. Activates on JIRA-style ticket implementation requests, "build this feature in target", "extend the new system with X", or any prompt referencing a JIRA-ID format like SHIP-101 or PROJ-NNN.
---

# Skill — `brownfield-feature-implementation`

End-to-end JIRA → working code workflow against a partially-built modern target with a legacy reference. Calls into `dual-store-gap-analysis` for the fact-gathering phase, then enforces a human-in-the-loop gate before generating any implementation code.

## When to use

Use this skill when:
- The user pastes a JIRA-style ticket key (e.g., `SHIP-101`) or a markdown ticket file
- The user asks to "implement this feature" in a system that has both a legacy reference and a partially-built target
- The user says "build feature X in the new system using both intel stores"

Do not use this skill for:
- Greenfield development (no legacy reference)
- Pure analysis without a planned implementation (use `dual-store-gap-analysis` directly)
- Code reviews (use a code-review skill)

## Phases

### Phase 1 — Ticket intake

1. Read the JIRA ticket. If a markdown file is referenced (e.g., `docs/jira-stories/SHIP-101-shipping-notification-flow.md`), read it. If a JIRA key is provided, fetch via the Atlassian MCP tools.
2. Verify both intel stores are reachable:
   - Run `mcp__corestory<Org>__list_projects`.
   - Confirm both projects have `ingestion_status: completed`.
   - If either is missing or not yet ingested, halt with a clear message.

### Phase 2 — Dual-store gap analysis

3. Invoke the `dual-store-gap-analysis` skill with the ticket as input.
4. Capture its output — the structured gap report.
5. **Carry forward every citation** from the gap report into a running context-source ledger (used in Phase 7). Each cited file path, table name, class, or method counts as one grounding source.

### Phase 3 — HITL gate (Stop Sign 2)

6. Present the gap report to the user. Quote:
   - Number of gaps per category
   - Top 3 surprises (e.g., target lacks the listener even though the topic is wired)
   - The proposed implementation order
7. **Wait for explicit user approval.** Do not proceed without it. Acceptable approvals: "approve", "looks good, proceed", "ship it". Acceptable revisions: "skip gap RE-001", "add a gap for X", "re-run analysis with this clarification".
8. If revisions are requested, loop back to Phase 2 with updates.

### Phase 4 — Implement

9. Generate code in the **target repo only**. Refuse to modify the legacy.
10. Touch only the gaps approved in Phase 3.
11. Follow the patterns in:
    - `docs/technical-spec.md` (architecture style, package structure, conventions)
    - `docs/design-spec.md` (capabilities, parity table, event topics)
    - The existing service code (e.g., `orders-service` for the FULL pattern)
12. **Refuse to modify legacy.** If during implementation the agent's plan calls for changes outside the target repo, abort and re-run gap analysis. The legacy is read-only.
13. **Track every grounding source consulted** during implementation. Each retrieval — gap-analysis citations carried forward, mid-flight intel-store queries, repo-spec sections read, ticket fields referenced — appends to the context-source ledger. The ledger is a required Phase 7 deliverable; do not skip the bookkeeping.

#### Phase 4a — Mid-flight context integration (REQUIRED behavior)

The user may inject new guidance during implementation — clarifications, additional constraints, or references to existing patterns. The skill must handle these without abandoning the approved plan, and must visibly retrieve the referenced context rather than guessing from training data.

**Detection.** Treat any user message arriving during Phase 4 as a candidate for mid-flight integration. Trigger the integration loop on any of these patterns (or close paraphrases):

- "Use the [pattern/convention/approach] from [service/file/module]"
- "Match how [existing thing] does [something]"
- "Follow the [thing] approach used in [reference]"
- "Make sure to [constraint/property] like [reference]"
- "Don't forget [constraint] — see [reference]"
- "[Service]/[file]/[module] has the [pattern] we want — apply it here"
- Any directive that names an in-repo or in-legacy artifact by path, class, table, or service

**Required behavior on detection:**

1. **Acknowledge in one line:** *"Picking up: [paraphrase of guidance]. Querying [target | legacy] intel store for [referenced artifact]."*
2. **Query** the appropriate intel store via `send_message`. Target store for in-repo references; legacy store for legacy references. Quote the retrieved response inline (file paths, line numbers, code snippets) so the integration is auditable.
3. **Update remaining implementation.** For files not yet written, apply the new guidance. For files already written, revisit only if the new guidance materially conflicts — otherwise note "applies forward only" and continue.
4. **Log the new source** in the context-source ledger with attribution: *"User-injected guidance → retrieved [pattern X] from [target | legacy] intel store at [path:line]."*
5. **Resume implementation** from where you paused, with the new context integrated. Do not restart, do not re-run gap analysis, do not re-prompt for plan approval (the plan stands; the guidance is a refinement).

**Edge cases:**

- **Legacy reference ("match how iDempiere does X").** Query the legacy intel store, replicate the *spirit* of the pattern in target code, never copy-paste. Log the retrieval as a legacy source.
- **Conflicts with the approved plan.** If the injection materially contradicts an approved gap (e.g., "skip BL-001 entirely"), halt and ask: *"This conflicts with approved gap [BL-001]. Should I revise the plan, or apply this only to remaining work?"* Do not silently override the approved plan.
- **Vague guidance ("make it cleaner").** Ask one targeted question: *"Cleaner in what dimension — naming, layering, error handling, performance? And do you want me to query for an existing pattern in the repo to anchor against?"* Do not interpret vague guidance unilaterally.
- **Out-of-scope guidance ("also implement INV-202 while you're at it").** Refuse. Cite scope: *"INV-202 is out of scope for this run. Want me to queue it as a follow-up after SHIP-101 closes?"*

### Phase 5 — Test + deploy

14. Generate or update tests per the gap report's "tests" column.
15. Run them (`mvn -pl <service> -am verify` or harness equivalent).
16. Iterate until green. Surface failures clearly — don't suppress.
17. **Deploy to the local stack.** Once tests pass, rebuild the Docker images for the services you modified and restart their containers so the changes are actually running. Example: `docker compose build shipping-service notifications-service && docker compose up -d shipping-service notifications-service`. Wait ~30s for services to become healthy (Flyway migrations run on startup; the gateway's downstream health probes need a moment to pick up the new instances). This step is non-optional — without it, every end-to-end verification (UI flow, MailHog, /notifications) still runs against the old stub images.

### Phase 6 — Spec touch-up (optional)

17. Update `docs/design-spec.md` with one line per substantive change ("Added shipping-notification flow handler in `notifications-service`").

### Phase 7 — Report

18. Summary of changes:
    - Files added/modified
    - Test results
    - Gap IDs addressed
    - Anything intentionally deferred

19. **Context inventory** — render the running context-source ledger as a structured inventory. This is a required deliverable, not optional. Format:

    ```markdown
    ## Context Inventory — Implementation Plan Grounded Against:

    ### Legacy intel store ({project name}, project {ID})
    - [1] {citation 1 with file:line or table.column}
    - [2] {citation 2}
    - ...

    ### Target intel store ({project name}, project {ID})
    - [N] {citation}
    - ...

    ### Repo specs
    - [N] DESIGN_SPEC.md §{section}
    - [N] TECHNICAL_SPEC.md §{section}
    - [N] api-contracts/{service}.md
    - ...

    ### JIRA ticket
    - [N] {TICKET-ID} acceptance criteria
    - [N] {TICKET-ID} parity reference
    - ...

    ### User-injected guidance (mid-flight)
    - [N] "{paraphrase of guidance}" → resolved to {retrieved artifact at path:line}
    - ...

    **Total: {count} grounding sources retrieved across {N} intel stores.**
    **Approximate token cost: ~{N,NNN} grounding tokens (vs ~{N}K full spec set).**
    ```

20. Hand back to user with both the change summary AND the context inventory.

## Critical guardrails

- **Refuse to modify legacy.** The legacy repo at `/Users/johnives/Downloads/Claude Context/idempiere/` is read-only. CoreStory queries only.
- **Never trust the static iDempiere PRD/tech-spec for legacy queries.** Always use `send_message` against project 457. Static artifacts have known accuracy gaps.
- **HITL gate is non-skippable.** The agent must wait for explicit human approval before Phase 4. No exceptions.
- **No public API breaking changes** without explicit approval. Never modify shapes in `docs/api-contracts/`.
- **Mid-flight integration is non-skippable.** When the user injects guidance during Phase 4, the agent must retrieve referenced context via the intel store rather than guessing. The auditable retrieval is the value prop; skipping it forfeits the grounding.
- **Context inventory is a required Phase 7 deliverable.** The ledger captures every grounding source consulted across the run. Skipping the inventory is not allowed.

## Triggers (for harness loaders)

- "implement this JIRA story"
- "implement this ticket"
- "build this feature in the new system"
- "build this feature in the target"
- "extend the new system with X"
- "implement {JIRA-ID}" (e.g., SHIP-101, INV-202, ORD-303, generic PROJ-NNN)
- "implement {feature} using both intel stores"

## Six guardrails (encoded as enforceable rules)

The skill enforces these throughout:

1. **Read the relevant spec before coding** — Phase 4 reads `docs/design-spec.md`, `docs/technical-spec.md`, `docs/api-contracts/<service>.md`.
2. **Search for similar existing implementations before writing new ones** — Phase 4 starts by grepping the target for the closest pattern.
3. **Reuse current patterns** — naming, layering, event shapes, test structure.
4. **Generate or update tests for every change** — Phase 5.
5. **Explain architectural impact when proposing a plan** — Phase 3's gap report does this.
6. **Don't change public contracts without approval; update DESIGN_SPEC after code changes** — Phase 6.

## Demo recording note

This skill is the focus of the recorded `idempiere-reimagined` demo. The recording shows:

1. Open `docs/jira-stories/SHIP-101-shipping-notification-flow.md`
2. Invoke this skill
3. Phase 2 fires `dual-store-gap-analysis` — captions on screen call out which intel store is being queried, source-by-source
4. Gap report renders
5. Context inventory renders alongside (or as a follow-up message) — proves volume of grounding
6. HITL gate fires — user approves
7. Implementation begins; mid-flight, the user injects: *"Use the publisher pattern from `OrderEventPublisher` in `orders-service` — same `JmsTemplate.convertAndSend` shape, topic-config-via-`@Value`, try/catch error handling that logs-but-doesn't-rethrow. Apply this to `ShipmentEventPublisher` in `shipping-service`."* Phase 4a fires — agent acknowledges, queries the target intel store for `OrderEventPublisher`'s body + design rationale (the `@Value` topic config with default fallback, the try/catch with rationale comment about transactional decoupling, the structured log shape), integrates the pattern faithfully into `ShipmentEventPublisher`, logs the new source
8. Code lands across `shipping-service`, `notifications-service`, frontend
9. Tests run green
10. UI demo: "Ship Order" button enables, click triggers flow, MailHog shows email, `/notifications` shows three log entries
11. Phase 7 final context inventory renders — "16 grounding sources, ~4,200 tokens vs ~120K full spec set"
