# Profat JavaScript Client

A small JavaScript/TypeScript client for the [Profat](https://github.com/your-org/profat) analytics API. Use it in the browser or Node to register services, log events (including page visits), and fetch analytics.

## Installation

From the repo (no publish required):

```bash
cd profat-client
npm install
npm run build
```

Or link from the monorepo root: `npm install ./profat-client`

## Development

- Build: `npm run build` (uses [tsdown](https://tsdown.dev/))
- Format: `npm run format` (Prettier)
- Check formatting: `npm run format:check`

## Usage

### ESM / TypeScript

```ts
import { createClient } from "profat-client";

const client = createClient({
  baseUrl: "http://localhost:9002/api/v1",
  apiKey: "same value as server profat.api-key",
});

// Create a service (or use an existing service ID)
const service = await client.createService({ name: "My Website" });
const serviceId = service.id;

// Log a page visit (predefined action)
await client.logPageVisit(serviceId, {
  pageUrl: "/dashboard",
  country: "US",
  isMobile: false,
  userAgent: navigator.userAgent,
});

// Log a custom action
await client.logEvent(serviceId, {
  action: "signup",
  payload: { plan: "pro" },
});

// List events (paginated, optional search)
const page = await client.listEvents(serviceId, { page: 0, size: 20, search: "signup" });

// List action names
const actions = await client.listActions(serviceId);

// Analytics summary (optional date range)
const summary = await client.getAnalyticsSummary(serviceId, {
  from: "2025-01-01",
  to: "2025-01-31",
});
```

### Script tag (browser)

Build first (`npm run build`), then copy `dist/index.iife.js` to your project and:

```html
<script src="/path/to/index.iife.js"></script>
<script>
  const client = window.Profat.createClient({
    baseUrl: "http://localhost:9002/api/v1",
    apiKey: "same value as server profat.api-key",
  });
  client.logPageVisit("your-service-id", {
    pageUrl: location.pathname,
    isMobile: /Mobi|Android/i.test(navigator.userAgent),
  });
</script>
```

### TypeScript types

Types are included in the package. If you use TypeScript, import types as needed:

```ts
import {
  createClient,
  type ProfatConfig,
  type ServiceResponse,
  type EventCreateRequest,
  type AnalyticsSummary,
} from "profat-client";
```

## API

- **createClient(config)** – `{ baseUrl: string; apiKey?: string }` → `ProfatClient` (set `apiKey` when the server uses `profat.api-key`)
- **client.createService(request)** – `{ name: string }` → `ServiceResponse`
- **client.listServices()** – → `ServiceResponse[]`
- **client.logEvent(serviceId, event)** – `{ action, payload? }` → `EventResponse`
- **client.logPageVisit(serviceId, payload)** – page_visit payload → `EventResponse`
- **client.listEvents(serviceId, options?)** – `{ page?, size?, search? }` → `Page<EventListItem>`
- **client.listActions(serviceId)** – → `string[]`
- **client.getAnalyticsSummary(serviceId, options?)** – `{ from?, to? }` → `AnalyticsSummary`

Errors from the API (4xx/5xx) throw **ProfatError** with `status`, `statusText`, and `body`.

## CORS

If your page is served from a different origin than the Profat API (e.g. `https://mysite.com` calling `http://localhost:9002`), the server must allow that origin. Enable CORS on the Profat Spring Boot app for your front-end origin, or run the page from the same host and use a relative `baseUrl` (e.g. `/api/v1`).
