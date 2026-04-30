# Profat

A small analytics and telemetry service for websites: track page visits, custom actions, and view per-service summaries (visits per day, countries, mobile vs desktop). Intended to run locally or as a backend microservice. The REST API under `/api/v1` requires the `X-Profat-Key` header unless `profat.api-key` is left blank.

## Project structure

- **Server** – Spring Boot 4 (Java) REST API with SQLite. Handles service registration, event logging, and analytics summaries.
- **profat-client** – JavaScript/TypeScript client library for the API (browser or Node). See [profat-client/README.md](profat-client/README.md).

## Quick start (server)

**Requirements:** Java 25+, Maven

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:9002/api/v1`. Open `http://localhost:9002/profat` (or `/index.html`) for the built-in API tester. **Do not commit API keys.** For local development, set `profat.api-key` in gitignored `application-local.properties` (see below) and enter the same value in the tester’s **API key** field (header `X-Profat-Key`). You can also use the `PROFAT_API_KEY` environment variable (maps to `profat.api-key`). With no key configured, the API accepts requests without `X-Profat-Key` (convenient for local open dev). **Production** (`SPRING_PROFILES_ACTIVE=prod`) sets `profat.api-key-required=true`, so the process exits at startup unless a key is provided via environment or config. The SQLite database is created at `./data/profat.db` by default.

### Local configuration (developers)

Shared defaults live in `src/main/resources/application.properties`. For machine-specific settings (static site paths, service IDs, and so on), copy the sample and fill in your values:

1. Copy `src/main/resources/application-local.properties.example` to `src/main/resources/application-local.properties` (that file is gitignored).
2. Edit that file: set `profat.api-key` if you want authenticated REST locally, and adjust any static-site paths or service IDs from the sample.
3. Run with the Spring **`local`** profile so those settings load. The easiest way is the Maven **`dev`** profile, which passes `local` to Spring Boot:

```bash
./mvnw -Pdev spring-boot:run
```

On Windows (PowerShell or cmd): `mvnw.cmd -Pdev spring-boot:run`.

Equivalent without Maven: `SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run`, or `-Dspring-boot.run.profiles=local`, or your IDE run configuration.

Production builds use `application-prod.properties` when `SPRING_PROFILES_ACTIVE=prod` (the Docker image sets this). You must supply `PROFAT_API_KEY` (or `profat.api-key`) when starting the container or the app will fail to start.

**Configure the database path:** set `spring.datasource.url` in `src/main/resources/application.properties` or in `application-local.properties`, or override at runtime:

```bash
./mvnw spring-boot:run -Dspring.datasource.url=jdbc:sqlite:/your/path/profat.db
```

## Backup and restore db

scp -i ~/.ssh/id_rsa admin@<ip>:/home/admin/profat/data/profat.db ./profat.bak.sqlite
scp -i ~/.ssh/id_rsa ./profat.bak.sqlite admin@<ip>:/home/admin/profat/data/profat.db

## Running with Docker

Build and run the server in a container with the SQLite database stored on the host:

```bash
# Build the image
docker build -t revirtualis/profat .
docker tag revirtualis/profat:latest 442979135069.dkr.ecr.us-east-1.amazonaws.com/revirtualis/profat:latest
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 442979135069.dkr.ecr.us-east-1.amazonaws.com
docker push 442979135069.dkr.ecr.us-east-1.amazonaws.com/revirtualis/profat:latest

# Run with a volume so the DB file is on the host (e.g. ./data on host → /data in container).
# Production profile requires an API key (do not commit it; pass at run time):
docker run -p 9002:9002 -e PROFAT_API_KEY="your-secret" -v "/home/admin/profat/data:/data" --restart=on-failure revirtualis/profat

```

The app inside the container uses `SPRING_DATASOURCE_URL=jdbc:sqlite:/data/profat.db` by default, so `profat.db` is created in the mounted directory and is readable/writable from the host.

To use a different host directory:

```bash
docker run -it -p 9002:9002 --ipc=host --mount type=bind,source="$(cygpath -w "$(pwd)")/data",target=/data revirtualis/profat
```

To override the DB path without a volume:

```bash
docker run -p 9002:9002 -e SPRING_DATASOURCE_URL=jdbc:sqlite:/some/path/profat.db revirtualis/profat
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

const client = createClient({
  baseUrl: "http://localhost:9002/api/v1",
  apiKey: process.env.PROFAT_API_KEY ?? "your-profat-api-key",
});
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
