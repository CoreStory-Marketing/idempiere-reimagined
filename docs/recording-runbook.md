# Recording runbook — `brownfield-feature-implementation` demo

> Step-by-step for QuickTime capture. Target length: 10–12 min. The framing emphasizes (a) the **scale** of the legacy reference (1.43M LoC) established up front, (b) the auditable trail of every grounding source the agent consults — a receipt engineering leaders can review, and (c) the **human-baseline time compression**: weeks of senior engineering work compressed to hours via this methodology.
>
> **Framing constraints (locked 2026-05-10 per Anand's feedback):**
>
> - **Audience is UPS/NTT teams**, not sophisticated AI users. Frame value against their *current human-driven baseline*, not against other AI tools.
> - **Do NOT compare grounded vs vanilla agent.** That's a debate for AI-savvy audiences. The right comparison is "what a human team takes" vs "what we just delivered."
> - **Lead with scale.** 1.43M-line legacy + partial new-system build = exactly UPS's situation. Establish this in beat 1.
> - **CoreStory's product is the methodology** — the skill bundle, the dual-store grounding, the audit ledger, the HITL gate, the guardrails. Together they enable AI-driven modernization at velocity that human teams can't match.

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

7. Clean coffee mug off desk. Phone on silent.

## Take structure

### 1. Open — establish scale + situation (1 min)
- Optional cold-open shot: `find . -name "*.java" | xargs wc -l | tail -1` on the legacy iDempiere checkout — show `1,434,756 total` flash on screen.
- Cut to `idempiere-reimagined` repo file tree. Point at:
  - `AGENTS.md` (universal cross-harness install)
  - `docs/design-spec.md` (parity table)
  - `.claude/skills/` (the bundle)
- VO line: **"This is iDempiere — a 1.43-million-line Java codebase that's been the open-source backbone of enterprise ERP for over a decade. And this is `idempiere-reimagined` — a partially-built Spring Boot reimagining of its logistics domain that we're going to extend today. Both codebases are ingested in CoreStory — legacy as project 457, target as project 458. The situation you're looking at — a large legacy reference, a partial new implementation, and a feature backlog to ship — is exactly the brownfield modernization pattern this methodology is built for."**

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
- **VO opens with the framing:** "The skill is doing what a senior engineer would do *before* writing code — querying the legacy system for behavior parity, scanning the target codebase for conventions, surfacing exactly what's missing. Watch the receipts build up as it goes."
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
- VO mid-beat: "Every retrieval comes back with concrete file paths and line numbers — the agent isn't guessing from training data. It's grounding against the actual codebase."
- VO closing the beat: "This is the discovery work a human engineer would do over a week of ramp-up on a codebase this size. The methodology compresses it to a few minutes — and unlike a human, the agent leaves a complete audit trail."

### 5. Gap report renders (45s)
- Opens `docs/gap-reports/SHIP-101-gap-report-final.md` (the staged file is the reference; the agent overwrites with live data).
- Walk through the 7 categories briefly, focus on:
  - **DM** — schema decomposition (legacy single MInOut → target receipts + shipments split)
  - **BL** — three implementation gaps (BL-001, BL-002, BL-003)
  - **RE** — `MMailText.parse()` pattern → `MustacheTemplateRenderer.render()`
- VO: "Fourteen gaps identified. Ten demo-critical, including a database migration. The agent has a sequenced plan, ready for review."

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

- VO: "Before approving, here's the receipt — every legacy class, every target convention, every spec section the agent grounded against. Nothing's a guess. An engineering leader can review this artifact, trace any decision back to its source. That's what makes AI-generated work auditable at enterprise scale — and it's something a human team's git history alone can't replicate."
- ON-SCREEN CAPTION (subtle, lower-third): "Every source cited · Engineering-leader-reviewable · Traceable to file:line"

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
- VO: "A senior engineer redirecting the implementation mid-flight is one of the highest-friction moments on a human team — re-onboarding, context-switching, sometimes losing the thread. The methodology absorbs it cleanly: the agent retrieves the referenced pattern, applies it consistently, and the audit trail captures the new context. That's guardrail enforcement under realistic conditions."
- Agent finishes:
  - `notifications-service/.../channels/EmailNotificationAdapter.java`
  - `notifications-service/.../channels/WarehouseLogAdapter.java`
  - `notifications-service/.../channels/AccountingLogAdapter.java`
  - `notifications-service/.../events/ShipmentNotificationConsumer.java`
  - `frontend/components/orders/ShipOrderButton.tsx`

### 8. Tests pass + deploy to local stack (2 min)
- Agent runs `mvn -pl shipping-service,notifications-service -am verify`.
- Tests pass: happy path, failure mode, idempotency.
- ON-SCREEN CAPTION: "Tests green — happy path · failure mode (MailHog down) · idempotency replay"
- Agent runs `docker compose build shipping-service notifications-service` then `docker compose up -d shipping-service notifications-service` to deploy the implementation to the running stack. (This is non-optional — the running containers still have the old stub code until rebuilt; without this beat 9 won't work.)
- ON-SCREEN CAPTION: "Deploying to local Docker stack..."
- Wait ~30s for services to become healthy (Spring Boot + Flyway V3 migration runs here).
- VO: "Tests green. The agent now deploys to the local stack — same commit-to-deploy pattern any modern team uses. In about ten seconds the frontend's health probe will pick it up."

### 9. UI demo (1 min — the moneymaker)
- After deploy completes and the frontend's `/shipments/health` poll succeeds (~10s after services healthy), refresh browser at `http://localhost:3000/orders`. Click any CONFIRMED order.
- "Ship Order" button is now ENABLED (was disabled).
- Click it.
- Switch tab to MailHog (`http://localhost:8025`). Email lands in the inbox. Open it. Show subject + body with substituted variables.
- Switch tab to `/notifications`. Three rows just appeared (EMAIL, WAREHOUSE, ACCOUNTING). Status SENT.
- VO: "Customer email in MailHog. Three notification log rows. End-to-end flow works."

### 10. DESIGN_SPEC update (30s)
- Show the diff in `docs/design-spec.md` — one-line addition to notifications-service capabilities.
- VO: "Skill updated the spec automatically. Future gap analyses see the new capability."

### 11. Recap — the human-baseline math (45s)

This is where the value lands. The audience isn't comparing CoreStory to other AI tools — they're comparing it to their *current human-driven baseline*. Make that comparison explicit.

- VO: "What you just watched is one feature. A typical enterprise feature like this — multi-service event flow, database migration, failure handling, tests, audit-trail-ready spec update, all integrated against a 1.4-million-line legacy reference — takes a senior engineer two to three weeks of focused work. Code review, gap analysis, ramp-up on conventions, cross-service coordination, all of it. We did it in [N minutes].

  Scale that across a feature backlog. An enterprise modernization team — ten engineers, eight sprints, delivering a quarter's worth of feature parity against a legacy reference of this size — that's four to six months of work. With this methodology, that same backlog compresses from months to days. Every feature delivered against the existing target conventions. Every implementation grounded in the legacy spec. Every artifact engineering-leader-reviewable.

  That's the product. The skill bundle, the dual-store grounding, the audit ledger, the guardrails — together, this is a complete methodology for AI-driven modernization at velocity human teams can't match."

- ON-SCREEN CAPTION (final beat, ~5s): "One feature: weeks → minutes · One backlog: months → days · Audit trail: complete"

## Re-record gates

Stop and re-record if:

- The agent stalls > 30s in any phase (Phase 2 querying excepted — that's expected).
- A CoreStory query returns vague output. (Mitigation: pre-staged gap report + verified conversation 5276 prevent this.)
- The context inventory (beat 5.5) shows fewer than ~10 sources or vague entries — kills the volume-handling story.
- The user-guidance moment (beat 7) doesn't visibly change the agent's behavior — agent must pull the `OrderEventPublisher` pattern and apply it to `ShipmentEventPublisher`, otherwise the runtime-context-integration claim falls flat.
- The UI doesn't refresh visibly when the agent's code lands.
- MailHog doesn't show the email.
- Any 501 error after implementation.
- Beat 11 VO references a wall-time number that wasn't actually captured. The "weeks-to-hours" framing must cite the **real** elapsed time from this recording, not an estimate.

## Debug & reset (between takes, or for a fresh Claude session debugging mid-recording)

### Known failure modes + fast fixes

| Symptom | Cause | Fast fix |
|---|---|---|
| Agent finishes implementation but **no context inventory ledger** rendered (beat 5.5 or beat 7-final missing) | Skill says it's required, but the agent sometimes skips it | Prompt: *"render the context inventory."* |
| Agent says "tests pass" but **UI still shows disabled Ship Order button** | Agent skipped the deploy step (Phase 5 step 17) — running containers still have stub code | Prompt: *"deploy to the local Docker stack."* Or manually: `docker compose build shipping-service notifications-service && docker compose up -d shipping-service notifications-service` |
| `mvn verify` fails with **`TypeTag :: UNKNOWN` / Lombok crash** | Maven running on JDK 24 instead of 17 | `export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home` then retry |
| **`curl ... \| jq -r .token` returns `null`** in your shell | Shell ate the line continuation (`\ ` instead of `\<newline>`) so curl posted no body | Run the curl on a single line, or use `'single quotes'` for the headers |
| Frontend `/login` shows **"Upstream auth failed: fetch failed"** | `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` resolves to the container itself from server-side Next.js code | Already fixed via `BACKEND_INTERNAL_URL: http://api-gateway:8080` in `docker-compose.yml`; if you regress this, restore both env vars + rebuild the frontend image |
| **`POST /orders` returns 500** | Pre-existing JPA auditing `OffsetDateTime` bug | Already fixed via `DateTimeProvider` bean in `JpaAuditingConfig` on all 5 services; if regression, check the bean exists and `@EnableJpaAuditing(dateTimeProviderRef = ...)` references it |
| **Receipt-related warehouse tests fail** when running `mvn verify` | Mockito default-method stub bug + JmsConfig bean collision | Already fixed; if regression, see `c906280` commit |
| `docker compose ps` shows a service **stuck in `Restarting`** | Usually Flyway migration error or schema mismatch | `docker logs idempiere-<service>-service --tail 100` and look for the actual exception |
| **Agent reaches into legacy iDempiere repo** | Skill guardrail failed | Stop the agent. Read `AGENTS.md` + `.claude/skills/brownfield-feature-implementation/SKILL.md` to confirm "refuse to modify legacy" still says so; if intact, prompt the agent to re-read the skill before continuing |

### Full reset between recording takes (~3 min)

```bash
# 1. Discard the agent's working-tree changes — `--include-untracked` catches the V3 migration the agent generates.
#    `git stash` is reversible: `git stash pop` recovers the agent's work if you want to inspect.
git stash --include-untracked

# 2. Stop containers + wipe DB volumes (V99 seed will re-apply cleanly on next `up`).
docker compose down -v

# 3. Rebuild the services the agent modifies, from the now-clean source.
#    Skipping this means the next take's containers still have the previous take's agent-generated code baked in.
docker compose build shipping-service notifications-service

# 4. Bring everything back up.
docker compose up -d

# 5. Wait ~60s, then verify clean "before" state.
sleep 60
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}' | jq -r .token)

# Expect: total=2 (the two seeded CONFIRMED orders)
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/orders?page=0&size=5" | jq '.total'

# Expect: HTTP 501 (shipping is back to stub)
curl -s -o /dev/null -w "%{http_code}\n" -X POST \
  -H "Authorization: Bearer $TOKEN" http://localhost:8080/shipments/1/ship

# Expect: empty inbox
curl -s 'http://localhost:8025/api/v2/messages?limit=5' | jq '.total'
```

If all three checks come back `2`, `501`, `0` — you're back to recording-ready state.

### What a fresh Claude session needs to know

If you spawn a new Claude Code session to debug something mid-recording, it'll auto-load the memory pointer to `Project-Status-idempiere-reimagined.md` (in the working dir, outside the repo). That doc has every fix, every decision, current commit SHA, the JDK 17 requirement, and the framing. The fresh session also has full read access to this runbook, `AGENTS.md`, `CLAUDE.md`, and `.claude/skills/brownfield-feature-implementation/SKILL.md`. No additional handoff needed.

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
