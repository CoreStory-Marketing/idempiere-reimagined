# Recording runbook — `brownfield-feature-implementation` demo

> Step-by-step for QuickTime capture. Target length: 12–14 min. The framing emphasizes how much grounding context the agent retrieves and integrates at runtime, not just that it produces working code.

## Pre-stage (5 min before recording)

1. Open VSCode pinned to `/Users/johnives/Downloads/Claude Context/idempiere-reimagined/`.
2. Open Terminal panel; start `docker compose up` and wait until ALL containers report `healthy`. If any fail, halt — do not record until clean.
3. Open browser tabs:
   - `http://localhost:3000` — frontend (log in as admin)
   - `http://localhost:8025` — MailHog UI (must be empty)
4. Open Claude Code in the IDE pane.
5. Verify CoreStory project 457 reachable: `mcp__corestoryProduct-Marketing-Lab__list_projects` should return both projects (legacy 457 + target).
6. **Stage the user-guidance message** in a notes app or clipboard, ready to paste mid-implementation (beat 7):

   > Use the dedupe pattern from `inventory-service` for the notification handlers — we don't want duplicate sends on consumer redelivery.

7. **Have the ungrounded-comparison clip ready** for the closing sidebar (beat 12). If pre-recorded, save at `assets/ungrounded-comparison.mov` (30–45s of the same SHIP-101 prompt run against an agent *without* CoreStory MCP + the skill bundle — output is generic, lacks parity references, ignores conventions). Cue it on a second screen or in a separate browser tab. If recording it live as the closing beat, have the "no-CoreStory" Claude Code session pre-staged.
8. Clean coffee mug off desk. Phone on silent.

## Take structure

### 1. Open (1 min)
- Show repo file tree. Point at:
  - `AGENTS.md` (universal cross-harness install)
  - `docs/design-spec.md` (parity table)
  - `.claude/skills/` (the bundle)
- VO line: "This is `idempiere-reimagined`, a 40%-built Spring Boot reimagining of iDempiere's logistics domain. Two intelligence stores back it — the legacy iDempiere project in CoreStory, and this repo, also in CoreStory."

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

  > Use the dedupe pattern from `inventory-service` for the notification handlers — we don't want duplicate sends on consumer redelivery.

- ON-SCREEN CAPTION: "User guidance injected mid-flight"
- VO: "A senior engineer would catch this constraint mid-implementation. Watch what happens — the agent doesn't restart, doesn't argue, doesn't lose the plan. It pulls one more piece of context."
- ON-SCREEN CAPTION: "Pulling: inventory-service dedupe pattern (target intel store)"
- Agent queries the target intel store, finds the dedupe pattern (unique constraint + idempotency key check), applies it to the notification handlers it hasn't yet written.
- ON-SCREEN CAPTION: "Integrating: dedupe pattern → notification handlers"
- VO: "Sixteen grounding sources now, not thirteen. Three of those came from runtime — the agent retrieving on demand as the implementation evolved. That's what scales as the spec set grows."
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

### 12. Closing comparison — grounded vs ungrounded (30s)

The "materially better than without CoreStory" claim made visible.

- Cut to the pre-staged ungrounded clip (`assets/ungrounded-comparison.mov`) OR run the same SHIP-101 prompt live in a no-CoreStory Claude Code session.
- Show 10–15 seconds of the ungrounded output: generic implementation suggestions, no parity reference, ignores existing conventions, no awareness of the dedupe pattern, hallucinated file paths.
- Cut back to the grounded result on screen.
- ON-SCREEN CAPTION (split-screen if possible): "Same prompt. Left: with CoreStory + skill bundle. Right: without."
- VO: "Same ticket, no CoreStory. The agent guesses at conventions, hallucinates file paths, ignores the dedupe pattern, doesn't know there's a legacy parity reference to honor. The skill is what closes that gap. That's the value at runtime — and it scales with whatever you put behind it."

## Re-record gates

Stop and re-record if:

- The agent stalls > 30s in any phase (Phase 2 querying excepted — that's expected).
- A CoreStory query returns vague output. (Mitigation: pre-staged gap report + verified conversation 5276 prevent this.)
- The context inventory (beat 5.5) shows fewer than ~10 sources or vague entries — kills the volume-handling story.
- The user-guidance moment (beat 7) doesn't visibly change the agent's behavior — agent must pull the dedupe pattern and apply it to the notification handlers, otherwise the runtime-context-integration claim falls flat.
- The UI doesn't refresh visibly when the agent's code lands.
- MailHog doesn't show the email.
- Any 501 error after implementation.
- The closing comparison (beat 12) shows ungrounded output that's *too good* — if the no-CoreStory agent happens to produce a plausible answer, the contrast doesn't land. Re-shoot the ungrounded clip with a more naive prompt, or pick a different ungrounded model that demonstrates the gap more clearly.

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
