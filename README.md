# Profat

A small analytics and telemetry service for websites: track page visits, custom actions, and view per-service summaries (visits per day, countries, mobile vs desktop). Intended to run locally or as a backend microservice; no authentication out of the box.

## Project structure

- **Server** – Spring Boot 4 (Java) REST API with SQLite. Handles service registration, event logging, and analytics summaries.
- **profat-client** – JavaScript/TypeScript client library for the API (browser or Node). See [profat-client/README.md](profat-client/README.md).

## Quick start (server)

**Requirements:** Java 25+, Maven

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:9002/api/v1`. The SQLite database is created at `./data/profat.db` by default.

**Configure the database path:** set `spring.datasource.url` in `src/main/resources/application.properties` (e.g. `jdbc:sqlite:/path/to/profat.db`) or override at runtime:

```bash
./mvnw spring-boot:run -Dspring.datasource.url=jdbc:sqlite:/your/path/profat.db
```

## Running with Docker

Build and run the server in a container with the SQLite database stored on the host:

```bash
# Build the image
docker build -t profat .

# Run with a volume so the DB file is on the host (e.g. ./data on host → /data in container)
docker run -p 9002:9002 -v "$(pwd)/data:/data" profat
```

The app inside the container uses `SPRING_DATASOURCE_URL=jdbc:sqlite:/data/profat.db` by default, so `profat.db` is created in the mounted directory and is readable/writable from the host.

To use a different host directory:

```bash
docker run -p 9002:9002 -v /path/on/host:/data profat
```

To override the DB path without a volume:

```bash
docker run -p 9002:9002 -e SPRING_DATASOURCE_URL=jdbc:sqlite:/some/path/profat.db profat
```

## API overview

| Method | Path | Description |
|--------|------|-------------|
| POST   | `/api/v1/services` | Register a service (app name) |
| GET    | `/api/v1/services` | List services |
| POST   | `/api/v1/services/{id}/events` | Log an event (action + payload) |
| GET    | `/api/v1/services/{id}/events` | List events (paginated, optional search) |
| GET    | `/api/v1/services/{id}/actions` | List distinct action names |
| GET    | `/api/v1/services/{id}/analytics/summary` | Analytics summary (visits/day, countries, device breakdown) |

Every event is an **action** with a **payload** (JSON). The predefined action `page_visit` uses a conventional payload (`pageUrl`, `country`, `isMobile`, etc.) and is used for the analytics summary. All timestamps are ISO 8601.

## Quick start (client)

From the repo:

```bash
cd profat-client
npm install
npm run build
```

Then in your app or page:

```ts
import { createClient } from "profat-client";

const client = createClient({ baseUrl: "http://localhost:9002/api/v1" });
await client.logPageVisit(serviceId, {
  pageUrl: "/",
  isMobile: /Mobi|Android/i.test(navigator.userAgent),
});
```

For script-tag usage and full API, see [profat-client/README.md](profat-client/README.md).

## CORS

If your frontend is served from a different origin than the Profat server, enable CORS on the Spring Boot app for that origin (e.g. via `WebMvcConfigurer` or `@CrossOrigin`).

## License

See repository or project metadata.
