# idempiere-reimagined

`idempiere-reimagined` is the order→shipment slice of a notional enterprise logistics platform — ~30K LoC across 5 services demonstrating dual-store gap analysis. The slice is intentionally bounded so the demo runs end-to-end in a tight footprint. The same patterns scale to your full target system at any size.

This is a **clean-room** Spring Boot reimagining of the logistics domain modeled in [iDempiere](https://github.com/idempiere/idempiere). No iDempiere code is reused. Apache 2.0 licensed.

## What it demonstrates

- Modular Spring Boot 3.4 microservices: orders, inventory, warehouse, shipping, notifications
- API gateway with JWT auth (Spring Cloud Gateway)
- Event-driven inter-service messaging (Apache Artemis JMS)
- Per-service Postgres (no shared DB)
- Next.js 14 admin frontend (App Router + Tailwind + shadcn/ui)
- Docker Compose runtime (one command up)

## Build state

| Service | State |
|---|---|
| `orders-service` | **Full** — state machine, event emission, tests |
| `inventory-service` | **Full** — event consumer, reservation logic, tests |
| `warehouse-service` | **Half** — receiving works end-to-end, picking is stubbed |
| `shipping-service` | **Stub** — schema + DTOs + repos only |
| `notifications-service` | **Stub** — port + listener + templates seeded |

The shipping and notifications stubs are the deliberate target of the brownfield-feature-implementation demo (see `docs/jira-stories/SHIP-101-shipping-notification-flow.md`).

## Quick start

```bash
docker compose up
# orders-service: http://localhost:8081
# inventory-service: http://localhost:8082
# warehouse-service: http://localhost:8083
# shipping-service: http://localhost:8084 (stubbed — returns 501)
# notifications-service: http://localhost:8085 (stubbed)
# api-gateway: http://localhost:8080
# frontend: http://localhost:3000
# Artemis console: http://localhost:8161 (admin/admin)
# MailHog UI: http://localhost:8025
```

## Repository layout

```
idempiere-reimagined/
├── domain-common/                 # Shared DTOs, events, ports
├── api-gateway/                   # Spring Cloud Gateway + JWT
├── orders-service/                # FULL
├── inventory-service/             # FULL
├── warehouse-service/             # HALF
├── shipping-service/              # STUB
├── notifications-service/         # STUB
├── frontend/                      # Next.js 14
├── docs/
│   ├── design-spec.md             # DESIGN_SPEC
│   ├── technical-spec.md          # TECHNICAL_SPEC
│   ├── api-contracts/             # Per-service OpenAPI summaries
│   ├── jira-stories/              # SHIP-101 (recorded), INV-202 + ORD-303 (backups)
│   ├── grounded-vs-ungrounded.md  # Side-by-side prompt example
│   └── guardrails-mapping.md      # 6-rule enforcement matrix
├── .claude/skills/                # 2-skill bundle
└── .github/copilot-instructions.md
```

## Two intelligence stores back this repo

- **Legacy:** iDempiere — CoreStory project ID 457 (Product-Marketing-Lab org). For parity reference only. **Never modify the legacy repo.**
- **Target:** This repo — a CoreStory project ID is published in `AGENTS.md` after ingestion completes.

The skill bundle (`.claude/skills/brownfield-feature-implementation` + `dual-store-gap-analysis`) queries both stores during the seven-category gap analysis, then implements the JIRA story end-to-end in the target only.

## Spec docs

- `docs/design-spec.md` — application-level capabilities, services, APIs, schemas, events, parity table
- `docs/technical-spec.md` — architecture style, package structure, frameworks, conventions, NFRs
- `docs/api-contracts/<service>.md` — per-service OpenAPI summaries

## License

Apache 2.0 — see `LICENSE`.
