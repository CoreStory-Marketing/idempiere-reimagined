---
name: dual-store-gap-analysis
description: Run a feature gap analysis across two CoreStory intel stores (legacy + target) to identify what exists, what's missing, and what needs to change before implementing a brownfield feature. Activates on dual-store gap analysis, modernization gap analysis, "compare legacy and target", or "what's missing in target vs legacy" requests.
---

# Skill — `dual-store-gap-analysis`

A clean extension of the standard `feature-gap-analysis` playbook into **dual-store mode**: we run the seven-category sweep against TWO CoreStory intel stores at once (a *legacy* system to mirror behavior parity with, and a *target* system that is partially built).

This skill is **standalone**. The orchestrator skill `brownfield-feature-implementation` calls it during Phase 2, but you can also invoke it on its own when you just want a gap report without a code implementation following.

## When to use

Use this skill when:
- The user references a JIRA-style ticket and asks the agent to analyze gaps before implementing
- The user wants a "what would it take to add X" report grounded in real codebase intel
- The user has two CoreStory projects (legacy + target) and wants a compare/contrast for a specific feature

Do not use this skill for:
- Greenfield design (no legacy reference exists)
- Single-store analysis (use the standard `feature-gap-analysis` playbook)
- Code implementation without prior gap analysis (use `brownfield-feature-implementation` directly)

## Phases

### Phase 1 — Identify both stores

1. Call `mcp__corestory<Org>__list_projects` to enumerate available CoreStory projects.
2. Identify the legacy and target projects from context (the user usually states them, or `AGENTS.md` declares them).
3. Confirm both have `ingestion_status: completed`. If not, halt with a clear message.
4. Verify the user has not asked to use the static PRD/tech-spec for the legacy. **Default behavior: query via `send_message` only.** Static artifacts are a known accuracy risk for legacy.

### Phase 2 — Create paired conversations

5. For each store, create or resume a conversation. Title both with the feature name + `[legacy]` / `[target]` suffix.
6. If a previously verified conversation exists for this feature on the legacy store (e.g. CoreStory project 457 conversation 5276 for shipping-notification flow), prefer resuming it.

### Phase 3 — Load context

7. Paste the JIRA story (or feature description) into both conversations.
8. Note any iDempiere parity references already in the spec — quote them in the legacy briefing so the answers stay scoped.

### Phase 4 — Run the seven-category sweep against TARGET

Per the canonical seven categories (from `feature-gap-analysis.mdx`):

1. **Existing capabilities (RO)** — what does target already do for this feature
2. **Data model (DM)** — what tables/entities/columns exist
3. **UI (UI)** — what frontend surface exists
4. **Business logic (BL)** — what services/handlers are wired
5. **Rendering (RE)** — what template/view rendering exists
6. **Integration (IG)** — what external systems / event topics / adapters exist
7. **Constraints (CO)** — what guardrails / forbidden patterns / NFRs apply

Send one query per category to the target conversation. Quote the response with citation paths.

### Phase 5 — Run the seven-category sweep against LEGACY

Same seven categories. The verified prompt template (proven against iDempiere project 457):

> I'm preparing a brownfield-feature-implementation demo for an enterprise customer. The demo runs a dual-store gap analysis (legacy + target) for the JIRA story:
>
> "{paste JIRA story user-story + acceptance-criteria here}"
>
> Brief me on how this system implements this flow today, organized by these seven categories. I need concrete file paths, class names, method names, table names, and column names — verbose is fine, this drives spec authoring.
>
> [enumerate the 7 categories]

For iDempiere specifically, expected concrete answers (example — verified 2026-05-09):

| Category | Sample legacy answer |
|---|---|
| RO | `MInOut.completeIt()` (`MInOut.java:1631`) orchestrates shipment lifecycle |
| DM | `M_InOut`, `M_InOutLine`, `R_MailText`, `R_MailText_Trl`, `AD_User`, `X_AD_UserMail`, `AD_Note` |
| UI | `AD_Window`/`AD_Tab` metadata; `WEMailDialog.java`, `AbstractADWindowContent.java` |
| BL | Email failure does NOT roll back shipment; sync send within doc-action transaction |
| RE | `MMailText.getMailText(boolean all, boolean parsed)` — `@variable@` substitution |
| IG | ModelValidator hooks, DocAction callbacks, `MClient.sendEMail()`, `MClient.SMTPHost` |
| CO | `AD_User.IsNoEMail` opt-in (verify), `AD_Client_ID`/`AD_Org_ID` always required |

### Phase 6 — Synthesize the dual-store gap report

Render a markdown report following this template:

```markdown
# Dual-Store Gap Report — {Feature Name}

**Legacy store:** {project name + ID}, conversation {ID}
**Target store:** {project name + ID}, conversation {ID}
**Generated:** {timestamp}

## Summary

{1-paragraph overview of the gap surface area}

## Per-category analysis

### 1. Existing capabilities (RO)
| Legacy state | Target state | Gap |
|---|---|---|
| {legacy ans, with citations} | {target ans, with citations} | {RO-001, RO-002 — concrete actionable gaps} |

### 2. Data model (DM)
... (same table shape) ...

### 3. UI (UI)
...

### 4. Business logic (BL)
...

### 5. Rendering (RE)
...

### 6. Integration (IG)
...

### 7. Constraints (CO)
...

## Gap inventory (numbered)

| ID | Category | Description | Resolution sketch | Effort |
|---|---|---|---|---|
| DM-001 | Data model | {desc} | {sketch} | S/M/L |
| BL-001 | Business logic | {desc} | {sketch} | S/M/L |
| IG-001 | Integration | {desc} | {sketch} | S/M/L |
| ... | ... | ... | ... | ... |

## Implementation plan

Sequenced, dependency-aware plan to close every gap. For each gap, one bullet:
- DM-001: change schema in `<file>`, run Flyway migration, regenerate JPA entity
- BL-001: implement `XService.foo()` per the legacy `MFoo.bar()` pattern
- ...

## Out of scope (deliberately deferred)

- {list anything called out in the JIRA story's "Out of scope" section}
- {plus anything the analysis surfaced as "yes but later"}
```

### Phase 7 — Validate

8. Cross-check that every JIRA acceptance criterion is addressed by at least one gap.
9. Cross-check that every gap is closeable in target only — no legacy modification implied. **If any gap requires legacy changes, halt.** The legacy is read-only; the gap analysis is wrong.

### Phase 8 — Output

Return the markdown gap report to the user. If invoked from `brownfield-feature-implementation`, hand off to its Phase 3 (HITL gate).

## Constraints

- **Never modify the legacy repo.** It's read-only. CoreStory queries only.
- **Never trust the static iDempiere PRD/tech-spec.** Use `send_message` only against the legacy project. The static artifacts have known accuracy gaps (verified 2026-05-09).
- **Cite source paths in both stores.** Each row in the gap table should reference real file paths — gap analysis with no citations is unreliable.
- **Flag `[unverified]` cells.** If a CoreStory answer is hedged or uncertain, propagate the uncertainty into the gap row. Don't invent confidence.

## Triggers (for harness loaders)

- "dual-store gap analysis"
- "modernization gap analysis"
- "compare legacy and target for {feature}"
- "what's missing in target vs legacy for X"
- "gap analysis across two intel stores"

## Output template

The skill always produces a markdown gap report (Phase 6 template). The structure is invariant; only the content changes per ticket.
