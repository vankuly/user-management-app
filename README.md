# User Management App

A small Kotlin/Spring Boot app for managing users, with a Vaadin UI and Postgres behind it. I built it to play with Vaadin's Flow + the KaribuDSL Kotlin bindings instead of the usual REST-plus-SPA split, so the whole thing — UI and server logic — lives in one codebase.

## Demonstration [images & video](DEMO.md)

## What's under the hood

- Kotlin 2.0 on JVM 21, Gradle (Kotlin DSL)
- Spring Boot 3.3, Spring Security (session-based)
- Vaadin 24 + KaribuDSL for the UI
- Spring Data JPA / Hibernate over PostgreSQL 16
- Flyway for migrations
- Docker Compose to wire it all together

## Running it

The fast path is Docker Compose:

```bash
docker-compose up
```

Heads up: the first build compiles the Vaadin production bundle, so expect 3–5 minutes on a cold machine. After that it's quick. Once it's up, the app is at http://localhost:8080.

If you'd rather run it straight from Gradle, you'll need a local Postgres on `localhost:5432` (db `usermgmt`, user/pass `postgres`/`postgres`), then:

```bash
./gradlew bootRun
```

### Logging in

Two accounts are created on first startup:

- **admin@example.com** / `admin123` (full access)
- **user@example.com** / `user123` (read-only)

There are also 500 seeded users for testing the grid/pagination — they all use the password `password`.

## How permissions work

Regular users can browse, search, sort and page through the user list. That's it. Everything that mutates data — creating, editing, deleting users, and viewing the audit log — is admin-only, enforced both in the UI and at the method level via `@RolesAllowed`. One guard worth calling out: nobody can delete the account they're currently logged in as.

The dashboard filters by name and email live (server-side, not client filtering), sorts on any column, and pages through results using a lazy Spring Data `Pageable` so only one page is ever in memory regardless of table size.

Every create/update/delete an admin performs gets written to the audit log in the same transaction as the change itself — actor, target, timestamp, and a short diff of what changed.

## Tests & CI

```bash
./gradlew test
```

Tests run against an in-memory H2 database in Postgres-compatibility mode, so there's no database to stand up first. Flyway is turned off for tests and Hibernate builds the schema straight from the JPA entities.

Every push and pull request against `main` runs the build and full test suite on GitHub Actions — see [`.github/workflows/ci.yml`](.github/workflows/ci.yml). The workflow sets up JDK 21, runs `./gradlew build`, and uploads the HTML test report as an artifact so failures are easy to dig into. It also cancels stale runs when you push again to the same branch.

## Configuration

Everything is configured through environment variables, so you don't need to touch `application.properties` to point the app at a different database or port. Each one has a sensible default baked in (the value after `:` below), which is what you get when the variable isn't set.

| Variable                 | Default                                        | What it does |
|--------------------------|------------------------------------------------|--------------|
| `DB_URL`                 | `jdbc:postgresql://localhost:5432/usermgmt`    | JDBC URL for the Postgres database |
| `DB_USERNAME`            | `postgres`                                      | Database user |
| `DB_PASSWORD`            | `postgres`                                      | Database password |
| `PORT`                   | `8080`                                          | HTTP port the app listens on |
| `VAADIN_PRODUCTION_MODE` | `false`                                         | When `true`, builds/serves the optimized Vaadin production bundle instead of the dev bundle. The Docker image sets this to `true` at build time |

A typical override looks like:

```bash
DB_URL=jdbc:postgresql://my-host:5432/mydb DB_USERNAME=app DB_PASSWORD=secret PORT=9090 ./gradlew bootRun
```

If you're running the app container under Compose, set the same variables in its `environment:` block (the commented-out `app` service in `docker-compose.yml` shows the pattern, pointing `DB_URL` at the `db` service host).

The `db` service in `docker-compose.yml` is configured separately through the standard Postgres image variables — `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`. If you change those, remember to update the app's `DB_*` variables to match.

## Layout

```
src/main/kotlin/com/example/usermgmt/
├── Application.kt                  # entry point
├── config/
│   ├── SecurityConfig.kt           # VaadinWebSecurity + BCrypt
│   └── DataInitializer.kt          # seeds the two default accounts
├── domain/                         # User / AuditLog entities + DTOs
├── repository/                     # Spring Data repos
├── service/
│   ├── UserService.kt              # user CRUD + business rules
│   ├── DatabaseUserDetailsService.kt  # Spring Security lookup
│   └── AuditLogService.kt
└── ui/                             # Vaadin views and dialogs
```

## A few decisions worth explaining

**Vaadin instead of a REST API + frontend framework.** Keeping the UI and server logic together cut out a lot of boilerplate, and it's what let me lean on KaribuDSL. The service layer is decoupled enough that bolting a REST layer on later wouldn't be a big lift.

**Default accounts come from code, not the seed migration.** `DataInitializer` creates the admin/user accounts on startup so their passwords get hashed by the real `BCryptPasswordEncoder` rather than baked in as a hardcoded hash. It's idempotent, so it's safe on every boot.

**Auth lookup lives in its own class.** `DatabaseUserDetailsService` handles the Spring Security side so `UserService` stays focused on CRUD and business rules — the two concerns can change independently.

**H2 for tests, Postgres for real.** H2 in Postgres mode keeps CI and local test runs dependency-free. The `user_role` enum maps cleanly to `VARCHAR` there thanks to `EnumType.STRING`.
