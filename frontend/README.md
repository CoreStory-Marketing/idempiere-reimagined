# iDempiere Reimagined — Frontend

Next.js 14 admin UI for the `idempiere-reimagined` Spring Boot logistics
platform. Pairs with the api-gateway and the five backend services
(orders / inventory / warehouse / shipping / notifications).

> **Don't polish.** Per the SOW, function over fashion until the demo arc
> runs end-to-end.

## Stack

- Next.js 14 (App Router) + React 18, strict TypeScript
- Tailwind CSS 3 + minimal in-house shadcn-style components
- TanStack Query v5 (server state) + Zustand (client UI state)
- `lucide-react` icons
- Pure-SVG charts (no chart library)

## Pages

| Demo-critical | Background-richness | Placeholders |
|---|---|---|
| `/` Dashboard | `/customers`, `/customers/[id]` | `/warehouse/picks` |
| `/orders` + `/orders/[id]` | `/products`, `/products/[id]` | `/shipping`, `/shipping/[id]` |
| `/inventory` | `/admin/price-lists` | `/reports` |
| `/warehouse/receipts` | `/admin/tax-rates` | |
| `/notifications` | `/admin/warehouses`, `/admin/warehouses/[id]` | |
|  | `/admin/carriers` | |
|  | `/admin/email-templates`, `/admin/email-templates/[id]` | |
|  | `/admin/settings` | |
|  | `/inventory/movements` | |
|  | `/vendors` | |
|  | `/purchasing/orders` | |

## Auth

- `/login` — splash with "Log in as admin" button
- POST → `/api/auth/login` (Next route handler) → upstream `/auth/login`
  → JWT stored in HTTP-only cookie (`idempiere_jwt`)
- Middleware redirects unauthenticated routes to `/login`

## API proxy

All browser → backend traffic flows through `/api/proxy/[...path]`.
The route handler attaches the JWT cookie as `Authorization: Bearer …`
and forwards method/headers/body to `${NEXT_PUBLIC_API_BASE_URL}/...`.
This keeps the frontend CORS-free and the JWT out of `localStorage`.

## Feature flag — `shipment.ship`

The "Ship Order" button on `/orders/[id]` is gated by the
`useFeatureEnabled('shipment.ship')` hook, which polls
`GET /shipments/health` every 10 s. When the endpoint returns 200,
the button enables; otherwise it stays disabled with the tooltip
"Pending shipping-service implementation". The recorded demo shows
the button flipping to enabled the moment the agent's SHIP-101
implementation lands.

## Mock fallback (dev only)

Set `MOCK_ENABLED=true` in `.env.local` to fall back to in-memory
mock data when the backend is unreachable. **Don't enable for the
demo recording — the demo uses the real backend.**

## Scripts

```bash
npm install
npm run dev        # http://localhost:3000
npm run build      # production build (must pass cleanly)
npm run start      # run the production build
npm run typecheck  # strict tsc --noEmit
```

## Environment

Copy `.env.local.example` → `.env.local`:

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
MOCK_ENABLED=false
```

In Docker Compose, the gateway is reachable at
`http://api-gateway:8080` from the frontend container.

## File map

```
app/
├── layout.tsx                  # root (providers only)
├── globals.css
├── login/page.tsx              # splash, no chrome
├── (app)/                      # authed pages — sidebar + topbar
│   ├── layout.tsx
│   ├── page.tsx                # dashboard
│   ├── orders/
│   ├── inventory/
│   ├── warehouse/
│   ├── shipping/               # placeholder until SHIP-101
│   ├── notifications/          # 5s polling
│   ├── customers/
│   ├── products/
│   ├── vendors/
│   ├── purchasing/orders/
│   ├── admin/                  # 7 admin pages
│   └── reports/
└── api/
    ├── auth/login/route.ts
    ├── auth/logout/route.ts
    └── proxy/[...path]/route.ts
components/
├── ui/                # Button, Card, Table, Badge, Input, …
├── layout/            # Sidebar, Topbar, PageHeader
├── orders/            # OrderTable, OrderTimeline, ShipOrderButton
├── inventory/         # StockTable, LowStockBadge
├── notifications/     # NotificationLogTable, ChannelIcon
└── charts/            # BarChart, Sparkline (pure SVG)
lib/
├── api/               # typed fetch wrapper + per-domain modules
├── hooks/             # useOrders, useFeatureEnabled, …
├── stores/            # Zustand client state
├── types/             # domain interfaces (mirror backend DTOs)
└── utils/             # format, statusColor, cn
middleware.ts          # auth redirect
```

## Wiring notes

- The dashboard counts widget aggregates client-side from the per-resource
  list endpoints, since the backend doesn't yet expose a
  `GET /dashboard/counts` aggregator. When the backend lands one, swap
  `getDashboardCounts` in `lib/api/dashboard.ts` for a single call.
- `Promise.allSettled` is used wherever the dashboard reads multiple
  endpoints — a degraded shipping-service shouldn't blank the page.
- `OrderStatus` and `DocumentStatus` enums must stay aligned with
  `domain-common` and the orders / warehouse Postgres CHECK constraints.

## Known TODOs

- Receipt creation form is a stub dialog (lines/locator picker not built).
- Pagination on customers / products / vendors is client-side; switch to
  paginated endpoints when the backend exposes them.
- The email-template detail view shows source + a 501 placeholder for the
  rendered preview until SHIP-101 lands.
- No optimistic mutations yet — every mutation invalidates and refetches.
