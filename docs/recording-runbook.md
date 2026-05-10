# Recording runbook — `brownfield-feature-implementation` demo

> Step-by-step for QuickTime capture. Target length: 12–14 min. The framing emphasizes (a) how much grounding context the agent retrieves and integrates at runtime, (b) the auditable trail (Phase 7 context inventory ledger) of every grounding source consulted, and (c) the honest velocity diff vs a vanilla agent on tasks requiring legacy reach.
>
> **Important framing constraint (locked 2026-05-10 after dry-run findings):** vanilla agents *can* complete brownfield tasks at this codebase's scale (~33K target, ~1.4M legacy reach). CoreStory's load-bearing value is **NOT** "vanilla fails ugly." It is **auditability + consistency at scale + Living Intelligence on an evolving codebase + skill-bundle guardrail enforcement**. Frame the comparison honestly — both agents work; what differs is speed, tool-call efficiency, search-space confidence, and audit-trail legibility.

## Pre-stage (5 min before recording)

1. Open VSCode pinned to `/Users/johnives/Downloads/Claude Context/idempiere-reimagined/`.
2. Open Terminal panel; start `docker compose up` and wait until ALL containers report `healthy`. If any fail, halt — do not record until clean.
3. Open browser tabs:
   - `http://localhost:3000` — frontend (log in as admin)
   - `http://localhost:8025` — MailHog UI (must be empty)
4. Open Claude Code in the IDE pane.
5. Verify CoreStory project 457 reachable: `mcp__corestoryProduct-Marketing-Lab__list_projects` should return both projects (legacy 457 + target).
6. **Stage the user-guidance message** in a notes app or clipboard, ready to paste mid-implementation (beat 7):

   > Use the publisher pattern from `OrderEventPublisher` in `orders-service` — same `JmsTemplate.convertAndSend` shape, topic-config-via-`@Value`, try/catch error handling that logs-but-doesn't-rethrow. Apply this to `ShipmentEventPublisher` in `shipping-service`.

7. **Stage the side-by-side comparison capture** for beat 12. Two fresh `claude` sessions in `/Users/johnives/Downloads/Claude Context/idempiere-reimagined/`: one with CoreStory MCP available (grounded), one with the explicit constraint *"do NOT use any tool whose name begins with `mcp__`; use only Read/Grep/Glob/Bash"* (vanilla). Both will receive the same legacy-parity task prompt (the iDempiere failure-tracking philosophy injection — see beat 12 for the prompt text). Pre-stage the prompt text in clipboard. Plan to capture both runs in QuickTime sequentially or side-by-side, then iMovie-edit into a split-screen frame.
8. Clean coffee mug off desk. Phone on silent.

## Take structure

### 1. Open (1 min)
- Show repo file tree. Point at:
  - `AGENTS.md` (universal cross-harness install)
  - `docs/design-spec.md` (parity table)
  - `.claude/skills/` (the bundle)
- VO line: "This is `idempiere-reimagined`, a partially-built Spring Boot reimagining of iDempiere's logistics domain — orders and inventory are full implementations, warehouse is half-built, shipping and notifications are deliberate stubs the agent fills in. Two intelligence stores back it — the legacy iDempiere project in CoreStory, and this repo, also in CoreStory."

### 2. Open the JIRA story (30s)
- Open `docs/jira-stories/SHIP-101-shipping-notification-flow.md` in the IDE
- VO: "Here's the ticket: implement shipping notification flow when orders are shipped. Three notifications — customer email, warehouse log, accounting record. The story has acceptance criteria, an iDempiere parity reference, and called-out out-of-scope items."

### 3. Invoke the skill (30s)
- In Claude Code: `Implement SHIP-101.`
- VO: "I'm invoking `brownfield-feature-implementation`. It calls `dual-store-gap-analysis` for Phase 2 — that's where we query both intel stores."

### 4. Phase 2 fires — high-volume context retrieval (3 min)

This is the beat where the volume-handling story lands. The agent isn't just "querying CoreStory" — it's pulling a wide variety of spec context at runtime, more than fits in any single prompt, and integrating across all of it.

- The skill enumerates CoreStory projects, identifies legacy + target, creates paired conversations.
- Resume conversation 5276 for the legacy briefing — fast (cached).
- Run target queries (~90s per category × 7).
- **VO opens with the framing:** "What you're about to watch is the agent retrieving context from multiple sources at runtime. None of this fits in a single prompt. The skill's job is to give the agent the right slice of a large spec set on demand."
- ON-SCREEN CAPTIONS rotate as queries fire (one caption per source as it's pulled, ~6–10 seconds each):
  - "Pulling: legacy M_InOut document workflow → shipments parity"
  - "Pulling: legacy R_MailText template framework → 3 notification types"
  - "Pulling: target shipments table schema (V1__init.sql)"
  - "Pulling: target NotificationSender port interface"
  - "Pulling: target EventPublisher conventions (orders-service)"
  - "Pulling: target OrderConfirmedEvent listener pattern (inventory-service)"
  - "Pulling: target test conventions (notifications-service ApplicationTests)"
  - "Pulling: target application.yml RabbitMQ topic config"
  - "Pulling: DESIGN_SPEC.md §10 (parity table)"
  - "Pulling: TECHNICAL_SPEC.md §3 (architecture style)"
  - "Pulling: api-contracts/notifications-service.md"
  - "Pulling: SHIP-101 acceptance criteria"
  - "Pulling: SHIP-101 iDempiere parity reference"
- VO mid-beat: "Concrete file paths and line numbers — not training-data guesses. Each query carries the JIRA story as context."
- VO closing the beat: "Thirteen grounding sources retrieved across two intel stores. The agent now has the full surface area it needs to make a plan."

### 5. Gap report renders (45s)
- Opens `docs/gap-reports/SHIP-101-gap-report-final.md` (the staged file is the reference; the agent overwrites with live data).
- Walk through the 7 categories briefly, focus on:
  - **DM** — schema decomposition (legacy single MInOut → target receipts + shipments split)
  - **BL** — three implementation gaps (BL-001, BL-002, BL-003)
  - **RE** — `MMailText.parse()` pattern → `MustacheTemplateRenderer.render()`
- VO: "13 gaps identified. 9 demo-critical. The agent has a sequenced plan."

### 5.5. Context inventory (45s)
- Skill (or wrapper) writes/displays a context-inventory summary alongside the gap report. Either inline at the bottom of the gap report, or as a separate panel/file (`docs/gap-reports/SHIP-101-context-inventory.md`). On-screen, formatted as:

  ```
  Context Inventory — Implementation Plan Grounded Against:

    Legacy intel store (iDempiere, project 457):
      [1] M_InOut document workflow
      [2] R_MailText template framework
      [3] Mail-trigger event flow

    Target intel store (idempiere-reimagined):
      [4] shipments table schema
      [5] NotificationSender port interface
      [6] OrderConfirmedEvent listener pattern (inventory-service)
      [7] EventPublisher conventions (orders-service)
      [8] Test conventions (notifications-service)
      [9] application.yml RabbitMQ topic config

    Repo specs:
      [10] DESIGN_SPEC.md §10 (parity table)
      [11] TECHNICAL_SPEC.md §3 (architecture style)
      [12] api-contracts/notifications-service.md

    JIRA ticket:
      [13] SHIP-101 acceptance criteria + parity reference

    Total: 13 grounding sources retrieved across 2 intel stores.
    Token cost: ~4,200 grounding tokens (vs ~120K full spec set).
  ```

- VO: "Before approving, here's everything the agent pulled to make this plan. Thirteen sources across two intel stores, plus the ticket. The agent retrieved roughly four thousand tokens of grounding from a full spec set of well over a hundred thousand. That ratio is the point — agents don't need everything in working memory, they need the right slice on demand."
- ON-SCREEN CAPTION (subtle, lower-third): "13 grounding sources · 2 intel stores · ~4,200 tokens of relevant context · ~120K total spec set"

### 6. HITL gate (30s)
- Skill prints the gap report + "Ready to implement? Reply 'approve' to continue."
- VO: "Stop Sign 2. The agent doesn't write code until I approve. I review the plan."
- Type: `approve` and Enter.

### 7. Implementation, with one human-in-the-flight moment (2 min)

The point of compressing this beat is: code generation is the boring part of the demo. The interesting moments are the agent integrating *new* context mid-flight. Cross-fade between file captions instead of dwelling on each one.

- Agent starts writing code across:
  - `shipping-service/.../service/ShipmentService.java`
  - `shipping-service/.../api/ShipmentController.java` (replace 501 with real impl)
  - `shipping-service/.../events/ShipmentEventPublisher.java`
  - `notifications-service/.../template/MustacheTemplateRenderer.java`
- ON-SCREEN CAPTIONS cross-fade rapidly: "Touching shipping-service..." → "Touching notifications-service..."
- VO: "Notice — every file is in the target repo. The agent never reaches into the legacy iDempiere repo. The guardrails in `AGENTS.md` forbid it."
- **~30 seconds in, pause the agent. Type the staged user-guidance message into chat:**

  > Use the publisher pattern from `OrderEventPublisher` in `orders-service` — same `JmsTemplate.convertAndSend` shape, topic-config-via-`@Value`, try/catch error handling that logs-but-doesn't-rethrow. Apply this to `ShipmentEventPublisher` in `shipping-service`.

- ON-SCREEN CAPTION: "User guidance injected mid-flight"
- VO: "A senior engineer reviewing mid-implementation would say this. Watch what happens — the agent doesn't restart, doesn't argue, doesn't lose the plan. The skill catches the new context, queries CoreStory for the referenced pattern, and applies it auditably."
- ON-SCREEN CAPTION: "Querying: orders-service OrderEventPublisher pattern"
- Agent queries the target intel store, retrieves `OrderEventPublisher`'s body + design rationale (`@Value` topic config with default fallback, try/catch with rationale comment about transactional decoupling preventing rollback on broker outage, structured log shape using `event.eventType()`/`event.eventId()`), applies the same pattern faithfully to `ShipmentEventPublisher`.
- ON-SCREEN CAPTION: "Applying: OrderEventPublisher pattern → ShipmentEventPublisher"
- VO: "One more grounding source, retrieved on-demand because the user redirected. The audit ledger captures it. That's what guardrail enforcement looks like in practice — the agent stays consistent with established conventions even when the user's redirects could push it off-course. Every retrieval auditable, every choice traceable."
- Agent finishes:
  - `notifications-service/.../channels/EmailNotificationAdapter.java`
  - `notifications-service/.../channels/WarehouseLogAdapter.java`
  - `notifications-service/.../channels/AccountingLogAdapter.java`
  - `notifications-service/.../events/ShipmentNotificationConsumer.java`
  - `frontend/components/orders/ShipOrderButton.tsx`

### 8. Tests run green (1 min)
- Agent runs `mvn -pl shipping-service,notifications-service -am verify`.
- Tests pass: happy path, failure mode, idempotency.
- VO: "All three test classes green — happy path, MailHog-down failure mode, idempotency replay."

### 9. UI demo (1 min — the moneymaker)
- Refresh browser at `http://localhost:3000/orders`. Click any CONFIRMED order.
- "Ship Order" button is now ENABLED (was disabled).
- Click it.
- Switch tab to MailHog (`http://localhost:8025`). Email lands in the inbox. Open it. Show subject + body with substituted variables.
- Switch tab to `/notifications`. Three rows just appeared (EMAIL, WAREHOUSE, ACCOUNTING). Status SENT.
- VO: "Customer email in MailHog. Three notification log rows. End-to-end flow works."

### 10. DESIGN_SPEC update (30s)
- Show the diff in `docs/design-spec.md` — one-line addition to notifications-service capabilities.
- VO: "Skill updated the spec automatically. Future gap analyses see the new capability."

### 11. Recap (30s)
- VO: "What you just watched: the agent pulled sixteen distinct pieces of grounding context — legacy behavior parity, target schema, target conventions, ticket acceptance criteria, your in-flight guidance — and synthesized all of them into a working implementation. None of that fits in a single prompt. CoreStory's job is to feed the agent the right slice of a large spec set on demand. That's what makes this scale as the spec set grows."

### 12. Closing comparison — grounded vs vanilla, honestly (45s)

The honest velocity-and-confidence story made visible. **Empirical baseline (from the 2026-05-10 dry-run on the iDempiere failure-tracking parity task):** grounded completed in ~75s with 5 tool calls; vanilla completed in ~5min with 16 tool calls; *both* produced clean, legacy-parity-aware code with zero hallucinations. The diff is real and measurable — it's just not "vanilla fails ugly." Frame accordingly.

- Capture (or replay from pre-staged QuickTime clips) two fresh `claude` sessions running the same legacy-parity prompt:

  > For the failure-mode handling in `delivery_attempts`, ensure the structure mirrors iDempiere's email-dispatch failure-tracking philosophy. What does iDempiere log when an email-send fails, when does it roll back vs continue, and how do downstream readers (operators, monitoring) discover failures? Apply that same audit-trail philosophy to our catch-block code in `notifications-service`. Cite specific iDempiere classes/methods/lines that informed each design choice.

  Session A (grounded): CoreStory MCP available — agent uses `mcp__corestoryProduct-Marketing-Lab__send_message` against project 457 (legacy intel store, conv 5276 cached briefing).
  Session B (vanilla): explicit constraint *"do NOT use any tool whose name begins with `mcp__`"* — agent must `find`/`grep`/`Read` directly against the 1.4M-line legacy at `/Users/johnives/Downloads/Claude Context/idempiere/`.

- ON-SCREEN CAPTION (split-screen): "Same task. Left: with CoreStory + skill bundle. Right: vanilla agent (file/grep tools only)."
- Show ~15s of each run side-by-side. Both produce working code. The visible diffs to highlight on screen:
  - **Tool calls:** grounded ~5, vanilla ~16 (3× more for vanilla)
  - **Wall time:** grounded ~75s, vanilla ~5 min (4× slower for vanilla)
  - **Citations:** grounded retrieves load-bearing references with rationale ("the philosophical synthesis"); vanilla retrieves *more* references via transitive grep but flags conservative confidence ("did not transitively explore MUserMail or R_RequestUpdates which might have revealed additional conventions")
  - **Audit-trail legibility:** grounded shows one CoreStory query with cited answer; vanilla shows scrolling grep + Read across many files
- Final frame: side-by-side of the two generated catch-blocks. **Both work. The diff is at the workflow level, not the output level.**
- ON-SCREEN CAPTION: "75s · 5 tool calls · audit ledger captures everything   |   5min · 16 tool calls · search-space confidence: low"
- VO: "Same task, both completed. Vanilla took four times longer and three times the tool calls. More importantly: vanilla can only verify what it's already read; the grounded agent knows the search space and produces an auditable ledger of every grounding source. As your codebase grows past iDempiere's 1.4 million lines, that gap widens. As more agents work concurrently across teams, that consistency matters. The Phase 7 audit ledger is the engineering-leader-trustable artifact — every grounding source captured, every decision traceable. That's CoreStory's load-bearing value. Not 'vanilla fails.' This scales, this is auditable, this is what brownfield modernization looks like in production."

## Re-record gates

Stop and re-record if:

- The agent stalls > 30s in any phase (Phase 2 querying excepted — that's expected).
- A CoreStory query returns vague output. (Mitigation: pre-staged gap report + verified conversation 5276 prevent this.)
- The context inventory (beat 5.5) shows fewer than ~10 sources or vague entries — kills the volume-handling story.
- The user-guidance moment (beat 7) doesn't visibly change the agent's behavior — agent must pull the `OrderEventPublisher` pattern and apply it to `ShipmentEventPublisher`, otherwise the runtime-context-integration claim falls flat.
- The UI doesn't refresh visibly when the agent's code lands.
- MailHog doesn't show the email.
- Any 501 error after implementation.
- The closing comparison (beat 12) shows the two runs within 20% of each other on tool calls or wall time — if the gap is too thin, re-capture with the legacy-parity prompt verbatim from the dry-run (which produced 4× speed / 3× call diff). Do not manufacture a "vanilla fails" framing — lean on the actual data. If the gap is genuinely small even on a faithful re-run, drop beat 12 entirely and lean on beat 5.5 (context inventory) + beat 11 (recap) for the value-prop close.

## Post-record

- Trim with QuickTime (File → Edit → Trim).
- Add VO + on-screen captions in iMovie. Captions in lower-third, black with white text, ~3s each.
- Export as 1080p MP4. Target file size < 200MB.

## Live readiness check (must pass before recording)

```bash
# From /Users/johnives/Downloads/Claude Context/idempiere-reimagined
docker compose down -v && docker compose up -d
sleep 60
docker compose ps   # → all 'healthy'
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq .token
# → returns a JWT
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r .token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/orders
# → returns paged orders list
```

If any of the above fails, the system isn't recording-ready.
