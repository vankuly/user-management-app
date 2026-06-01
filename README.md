# User Management App

A full-stack web application built with **Kotlin**, **Spring Boot**, **Vaadin 24**, and **PostgreSQL**.

## Demonstration [images & video](DEMO.md)

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Language   | Kotlin 2.0 (JVM 21)                 |
| Build      | Gradle (Kotlin DSL)                 |
| UI         | Vaadin 24 + KaribuDSL               |
| Security   | Spring Security (session-based)     |
| Persistence| Spring Data JPA / Hibernate         |
| DB         | PostgreSQL 16                       |
| Migrations | Flyway                              |
| Container  | Docker + Docker Compose             |

---

## Quick Start (one command)

```bash
docker-compose up
```

The first run builds the application image (including the Vaadin production bundle) and starts PostgreSQL. This takes **3–5 minutes** on a clean machine. Subsequent starts are fast.

Once up, open **http://localhost:8080** in your browser.

---

## Default Credentials

| Role  | Email               | Password  |
|-------|---------------------|-----------|
| Admin | admin@example.com   | admin123  |
| User  | user@example.com    | user123   |

Seed users (500) all have password **`password`**.

---

## Features

### Authentication
- Session-based login via Spring Security
- Passwords stored as BCrypt hashes (cost factor 10)
- Redirect to dashboard on success; login error shown on failure

### Roles
| Feature                  | USER | ADMIN |
|--------------------------|------|-------|
| View user list           | ✅   | ✅    |
| Search / sort / paginate | ✅   | ✅    |
| Create user              | ❌   | ✅    |
| Edit user                | ❌   | ✅    |
| Delete user              | ❌   | ✅    |
| View audit log           | ❌   | ✅    |

### Dashboard
- Filterable by **name** and **email** (live, server-side)
- Sortable by name, email, created at, updated at
- Server-side **pagination** via Vaadin's lazy `DataProvider`

### Audit Log (Admin only)
- Every create / update / delete action is logged with actor, target user, timestamp, and a diff summary

---

## Running Locally (without Docker)

1. Start PostgreSQL on `localhost:5432` with database `usermgmt`, user `postgres`, password `postgres`.
2. Run the app:

```bash
./gradlew bootRun
```

3. Open **http://localhost:8080**.

---

## Running Tests

```bash
./gradlew test
```

Tests use an in-memory H2 database (PostgreSQL compatibility mode). Flyway is disabled for tests; Hibernate creates the schema from JPA entities.

---

## Project Structure

```
src/main/kotlin/com/example/usermgmt/
├── Application.kt               # Spring Boot entry point
├── config/
│   ├── SecurityConfig.kt        # VaadinWebSecurity, BCrypt bean
│   └── DataInitializer.kt       # Ensures default accounts exist
├── domain/
│   ├── User.kt                  # JPA entity + DTOs
│   └── AuditLog.kt              # Audit entity + DTO
├── repository/
│   ├── UserRepository.kt        # JPA repo with filter query
│   └── AuditLogRepository.kt
├── service/
│   ├── UserService.kt           # CRUD + UserDetailsService
│   └── AuditLogService.kt
└── ui/
    ├── LoginView.kt             # @AnonymousAllowed login page
    ├── MainLayout.kt            # AppLayout shell (nav, logout)
    ├── DashboardView.kt         # User list with filter/sort/page
    ├── UserDialog.kt            # Create/Edit dialog
    ├── ConfirmDialog.kt         # Generic confirmation dialog
    └── AuditLogView.kt          # Admin audit trail
```

---

## Design Decisions & Trade-offs

- **Vaadin over REST+SPA** – Vaadin keeps UI and server logic co-located, which aligns with the KaribuDSL requirement and minimises boilerplate. A REST API layer was not required but the service layer is decoupled enough that adding one is straightforward.
- **Flyway for migrations** – Version-controlled schema changes that run automatically on startup, including the 500-user seed.
- **DataInitializer for default accounts** – The two default accounts are created programmatically (rather than in the Flyway seed) so passwords are always hashed with the real `BCryptPasswordEncoder`, not a hardcoded hash.
- **Server-side DataProvider** – The Grid uses a lazy `DataProvider` backed by Spring Data's `Pageable`, so no more than one page of data is loaded at a time regardless of total row count.
- **H2 (PostgreSQL mode) for tests** – Avoids the need for a running database in CI/local test runs. The enum `user_role` type is mapped as `VARCHAR` in H2 via the `EnumType.STRING` JPA annotation.
