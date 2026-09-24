<<<<<<< HEAD
# Digital Wallet

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue?logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-red?logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

> A production-grade **Spring Boot backend** for a digital wallet and digital banking platform — supporting user authentication, wallet management, money transfers, deposits/withdrawals, transaction history, OTP verification, and role-based access control (RBAC).

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Authentication Flow (JWT)](#authentication-flow-jwt)
- [Database Migration](#database-migration)
- [Example API Endpoints](#example-api-endpoints)
- [Development Notes](#development-notes)
- [Future Improvements](#future-improvements)
- [License](#license)

---

## Overview

**Digital Wallet** is a monolithic Spring Boot backend that provides core banking and e-wallet capabilities. It exposes a secure REST API for customer onboarding, wallet lifecycle management, and financial transactions (deposit, withdraw, transfer). The system enforces financial integrity through **double-entry bookkeeping**, **pessimistic locking**, and **idempotency keys**.

The application is built with **Java 21**, **Spring Boot 3.2.4**, **Spring Security** (OAuth2 Resource Server + JWT), **Spring Data JPA**, **Flyway**, and **PostgreSQL**. It also integrates **Redis** for rate limiting and **JavaMail** for email-based OTP and password reset flows.

> **Assumption:** The project is currently a monolith. The architecture is layered (Controller → Service → Repository) and can be extracted into microservices in the future.

---

## Features

- **User Authentication**
  - Register (auto-creates a default wallet)
  - Login with password
  - Quick login via trusted device
  - Refresh token rotation
  - Logout / logout-all (revoke refresh tokens)
  - Forgot password & reset password via email token
- **Wallet Management**
  - Create multiple wallets per user (checking, savings, business, etc.)
  - View wallet list, detail, and balance
  - Activate / freeze a wallet
- **Money Transfer**
  - Transfer between wallets (with balance & limit checks)
  - Deposit funds
  - Withdraw funds
- **Transaction History**
  - Paginated transaction history
  - Transaction detail with ledger entries
- **Role-Based Access Control (RBAC)**
  - Roles: `ADMIN`, `USER`, `STAFF`, `MANAGER`
  - Method-level security via `@PreAuthorize`
- **Admin Management APIs**
  - List users (filter by status/role, paginated)
  - View user detail with wallet summary
  - Change user status / role
- **Security & Reliability**
  - JWT (HMAC-SHA256) access + refresh tokens
  - BCrypt password encoding
  - Rate limiting (Redis)
  - Login attempt tracking & account lockout
  - Idempotency keys to prevent duplicate transactions
  - Double-entry ledger with `balance_before` / `balance_after` audit trail
- **OTP**
  - Send & verify OTP via email (user activation)

---

## Tech Stack

| Category | Technology |
|:---|:---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + OAuth2 Resource Server (JWT) |
| JWT | Nimbus JOSE + JJWT |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL 14 |
| Cache / Rate Limit | Redis (Spring Data Redis) |
| Migration | Flyway |
| API Documentation | springdoc-openapi (Swagger UI) |
| DTO Mapping | MapStruct |
| Boilerplate | Lombok |
| Mail | JavaMail (SMTP) |
| Build Tool | Maven |
| Container | Docker / Docker Compose |

---

## Project Structure

```
digital-wallet/
├── compose.yaml                          # Docker Compose: PostgreSQL + Redis
├── .env.example                          # Sample environment variables
├── pom.xml                               # Maven build configuration
├── mvnw / mvnw.cmd                       # Maven wrapper
└── src/
    ├── main/
    │   ├── java/com/nhatbui/digital_wallet/
    │   │   ├── DigitalWalletApplication.java        # Application entry point
    │   │   ├── config/                              # Spring configuration
    │   │   │   ├── SecurityConfig.java              # Security filter chain, JWT decoder
    │   │   │   ├── SecurityUtils.java               # Current-user utility
    │   │   │   ├── SwaggerSecurityConfig.java       # Swagger security scheme
    │   │   │   └── SystemWalletInitializer.java     # System wallet bootstrap
    │   │   ├── controller/                          # REST controllers
    │   │   │   ├── AuthController.java
    │   │   │   ├── OtpController.java
    │   │   │   ├── UserController.java
    │   │   │   ├── WalletController.java
    │   │   │   ├── TransactionController.java
    │   │   │   └── AdminController.java
    │   │   ├── dto/
    │   │   │   ├── request/                         # Request DTOs (auth, user, wallet)
    │   │   │   └── response/                        # Response DTOs (auth, common, user, wallet, ...)
    │   │   ├── entity/                              # JPA entities
    │   │   ├── enums/                               # Domain enums (Currency, Status, Role, ...)
    │   │   ├── exception/                           # AppException, ErrorCode, GlobalExceptionHandler
    │   │   ├── generator/                           # Customer ID & wallet number generators
    │   │   ├── mapper/                              # MapStruct mappers
    │   │   ├── repository/                          # Spring Data JPA repositories
    │   │   ├── service/                             # Service interfaces
    │   │   └── service/impl/                        # Service implementations
    │   └── resources/
    │       ├── application.yaml                     # Base configuration
    │       ├── application-local.yaml               # Local profile (Swagger, debug logging)
    │       ├── application-prod.yaml                # Production profile (Swagger off, WARN logs)
    │       └── db/
    │           └── V1__init_schema.sql              # Flyway initial migration
    └── test/                                        # Unit & integration tests
```

---

## Getting Started

### Prerequisites

- **JDK 21** (Temurin or Oracle)
- **Maven 3.9+** (or use the bundled `mvnw` wrapper)
- **Docker** & **Docker Compose** (to run PostgreSQL & Redis)
- **Git**

### Clone repository

```bash
git clone https://github.com/nhatbuicv/Digital-Banking.git
cd digital-wallet
```

### Configure environment variables

Copy the sample env file and fill in your values:

```bash
cp .env.example .env
```

> On Windows (cmd): `copy .env.example .env`

Edit `.env` and provide at minimum `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SIGNER_KEY`. See [Environment Variables](#environment-variables) for the full list.

### Start PostgreSQL with Docker Compose

The `compose.yaml` provisions PostgreSQL 14 and Redis 7:

```bash
docker compose up -d
```

Verify the containers are running:

```bash
docker compose ps
```

### Run Flyway migrations

Flyway runs **automatically** on application startup (`spring.flyway.enabled=true`). The initial schema is defined in `src/main/resources/db/V1__init_schema.sql`.

To run migrations manually via the Maven plugin:

```bash
./mvnw flyway:migrate
```

> On Windows: `mvnw.cmd flyway:migrate`

### Run the application

```bash
./mvnw spring-boot:run
```

Alternatively, build and run the packaged JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/Digital_Wallet-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`.

---

## Environment Variables

| Variable | Description | Required | Default |
|:---|:---|:---:|:---|
| `DB_URL` | JDBC URL for PostgreSQL (e.g. `jdbc:postgresql://localhost:5432/digital_wallet`) | ✅ | — |
| `DB_USERNAME` | PostgreSQL username | ✅ | — |
| `DB_PASSWORD` | PostgreSQL password | ✅ | — |
| `JWT_SIGNER_KEY` | Secret key used to sign JWTs (HMAC-SHA256, min. 256 bits) | ✅ | — |
| `MAIL_USERNAME` | Gmail account used to send emails (OTP, password reset) | ✅ | — |
| `MAIL_PASSWORD` | Gmail App Password | ✅ | — |
| `ESMS_API_KEY` | eSMS API key for SMS OTP (optional) | ❌ | — |
| `ESMS_SECRET_KEY` | eSMS secret key for SMS OTP (optional) | ❌ | — |
| `SPRING_PROFILES_ACTIVE` | Spring profile (`local` or `prod`) | ❌ | `local` |

> **Note:** A minimal `.env.example` ships with the repo containing `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SIGNER_KEY`. The remaining variables (`DB_URL`, `MAIL_*`, `ESMS_*`) are assumed to be provided in your local `.env` or runtime environment.

---

## API Documentation

The project uses **springdoc-openapi** to generate interactive API documentation.

| Resource | URL |
|:---|:---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

> Swagger UI is **enabled in the `local` profile** and **disabled in `prod`** for security reasons.

### Authentication in Swagger

1. Click the **Authorize** 🔓 button in Swagger UI.
2. Paste your JWT access token (without the `Bearer ` prefix — the UI adds it automatically).
3. Click **Authorize** to apply the token to all secured endpoints.

---

## Authentication Flow (JWT)

The system uses **stateless JWT authentication** with an access/refresh token pair, backed by Spring Security's OAuth2 Resource Server.

```
┌────────┐   POST /auth/login      ┌──────────────┐   verify password (BCrypt)  ┌─────────────┐
│ Client │ ──────────────────────▶ │AuthController│ ──────────────────────────▶ │ AuthService │
└────────┘                         └──────────────┘                             └──────┬──────┘
                                                                                       │
                                                          ┌────────────────────────────┘
                                                          ▼
                                            ┌──────────────────────────────┐
                                            │ Issue Access Token (1h)      │
                                            │ Issue Refresh Token (24h)    │
                                            │ Persist RefreshToken + Device│
                                            └──────────────┬───────────────┘
                                                           │
┌────────┐    200 OK { accessToken, refreshToken, ... }    │
│ Client │ ◀───────────────────────────────────────────────┘
└────┬───┘
     │
     │  GET /wallets  (Authorization: Bearer <accessToken>)
     ▼
┌──────────────────┐   JWT decoder (Nimbus)   ┌────────────────┐
│ Security Filter  │ ───────────────────────▶ │JwtAuthConverter│ → ROLE_USER
└──────────────────┘                          └────────────────┘
     │
     ▼  @PreAuthorize("isAuthenticated()")
┌──────────────────┐
│ WalletController │
└──────────────────┘
```

**Key points:**

- **Access token** — short-lived (1 hour, configurable via `jwt.access-token-duration`), sent in the `Authorization: Bearer <token>` header.
- **Refresh token** — long-lived (24 hours, configurable via `jwt.refresh-token-duration`), used to obtain new access tokens via `POST /auth/refresh`.
- **Signing** — HMAC-SHA256 with `jwt.signerKey`.
- **Verification** — Spring Security OAuth2 Resource Server decodes and validates the JWT automatically.
- **Roles** — extracted from the `role` claim and mapped to `ROLE_<ROLE>` authorities.
- **Public endpoints** — `/auth/register`, `/auth/login`, `/auth/quick-login`, `/auth/refresh`, `/auth/device-session/**`, `/auth/otp/send`, `/auth/otp/verify`.

---

## Database Migration

Migrations are managed by **Flyway** and stored under `src/main/resources/db/`.

| File | Description |
|:---|:---|
| `V1__init_schema.sql` | Initial schema — `users`, `wallets`, `transactions`, `ledger_entries`, `refresh_tokens`, `device_sessions`, `password_reset_tokens`, `provinces` |

**Behavior:**

- `spring.flyway.enabled=true` — migrations run automatically on startup.
- `baseline-on-migrate=true` — baselines existing databases at version 1.
- `validate-on-migrate=true` — validates applied migrations against the local files.
- Hibernate is set to `ddl-auto=validate` — Flyway owns the schema; Hibernate only validates entities.

**Run manually:**

```bash
./mvnw flyway:migrate
```

**Create a new migration:**

```bash
# Follow the naming convention: V<version>__<description>.sql
# Example: V2__add_audit_columns.sql
```

---

## Example API Endpoints

### Authentication (`/auth`)

| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| `POST` | `/auth/register` | Register a new user (creates a default wallet) | ❌ |
| `POST` | `/auth/login` | Login with email & password | ❌ |
| `POST` | `/auth/quick-login` | Quick login via a trusted device | ❌ |
| `POST` | `/auth/refresh` | Refresh an access token | ❌ |
| `POST` | `/auth/logout` | Revoke a refresh token (current device) | ✅ |
| `POST` | `/auth/logout-all` | Revoke all refresh tokens (all devices) | ✅ |
| `POST` | `/auth/forgot-password` | Request a password reset email | ❌ |
| `POST` | `/auth/reset-password` | Reset password using a reset token | ❌ |
| `GET` | `/auth/device-session/{deviceId}` | Get device session info | ✅ |
| `DELETE` | `/auth/device-session/{deviceId}` | Delete a device session | ✅ |

### OTP (`/auth/otp`)

| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| `POST` | `/auth/otp/send` | Send an OTP to an email | ❌ |
| `POST` | `/auth/otp/verify` | Verify an OTP and activate the user | ❌ |

### User (`/users`)

| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| `GET` | `/users/me` | Get the current user's profile | ✅ |
| `PUT` | `/users/me` | Update the current user's profile | ✅ |
| `PUT` | `/users/me/password` | Change the current user's password | ✅ |

### Wallet (`/wallets`)

| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| `GET` | `/wallets` | List the current user's wallets | ✅ |
| `GET` | `/wallets/{walletNumber}/detail` | Get wallet details by wallet number | ✅ |
| `POST` | `/wallets` | Create a new wallet | ✅ |
| `PATCH` | `/wallets/{walletId}/activate` | Activate a wallet | ✅ |
| `PATCH` | `/wallets/{walletId}/freeze` | Freeze a wallet | ✅ |
| `GET` | `/wallets/{walletNumber}/balance` | Get wallet balance | ✅ |

### Transaction (`/transactions`)

| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| `POST` | `/transactions/transfer` | Transfer money between wallets | ✅ |
| `POST` | `/transactions/deposit` | Deposit funds into a wallet | ✅ |
| `POST` | `/transactions/withdraw` | Withdraw funds from a wallet | ✅ |

### Admin (`/admin/users`)

| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| `GET` | `/admin/users` | List users (paginated, filter by `status` / `role`) | `ADMIN` |
| `GET` | `/admin/users/{uid}` | Get user detail with wallet summary | `ADMIN` |
| `PATCH` | `/admin/users/{uid}/status?status=` | Change a user's status | `ADMIN` |
| `PATCH` | `/admin/users/{uid}/role?role=` | Change a user's role | `ADMIN` |
| `GET` | `/admin/audit-logs` | List admin audit logs (filter by `actorUid`, `action`, `targetType`, `targetUid`, `fromDate`, `toDate`) | `ADMIN` |

### Sample Request — Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "P@ssw0rd!"
  }'
```

### Sample Response — Login

```json
{
  "code": 1000,
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "deviceSession": {
      "deviceId": "device-uuid",
      "deviceName": "Chrome - Windows"
    }
  }
}
```

---

## Development Notes

### Build & Test

```bash
# Run all tests (unit + Testcontainers integration tests, needs Docker running)
./mvnw test

# Only the Postgres + Redis smoke test
./mvnw test -Dtest=PostgresRedisSmokeTest

# Build without tests
./mvnw clean package -DskipTests
```

> Integration tests use **Testcontainers** (real PostgreSQL + Redis via
> `@ServiceConnection`, see `AbstractIntegrationTest`). Details and troubleshooting:
> [`docs/local-test.md`](docs/local-test.md).

### Profiles

| Profile | Swagger | Logging | Use case |
|:---|:---|:---|:---|
| `local` (default) | Enabled | `DEBUG` | Development |
| `prod` | Disabled | `WARN` / `INFO` | Production |

### Conventions

- **Branch naming:** `feature/`, `fix/`, `chore/`
- **Commit convention:** `feat(module): message`, `fix(module): message`
- **Architecture:** Package-by-layer (Controller → Service → Repository)
- **Transactions:** Always annotate write operations with `@Transactional`
- **Error handling:** Throw `AppException` with a corresponding `ErrorCode` — never throw raw exceptions
- **DTO mapping:** Use MapStruct mappers; never expose entities directly in API responses

### Error Response Format

All errors are centralized via `GlobalExceptionHandler` (`@RestControllerAdvice`):

```json
{
  "code": 1002,
  "success": false,
  "message": "You do not have permission to access this resource",
  "data": null
}
```

| Error code range | Module |
|:---|:---|
| `9000–9999` | System / Common |
| `1001–1019` | Auth / Security |
| `1020–1099` | User |
| `2001–2099` | Wallet |
| `4001–4099` | Transaction |
| `5001–5099` | Location |

---

## Future Improvements

- [ ] Add transaction history & detail endpoints (`GET /transactions/history`, `GET /transactions/{id}`)
- [ ] Introduce scheduled jobs for expiring tokens and stale transactions
- [ ] Add asynchronous notification service (email/SMS via message queue)
- [ ] Introduce OpenTelemetry tracing and Prometheus metrics
- [ ] Add CI/CD pipeline with GitHub Actions (build, test, Docker image)
- [ ] Containerize the application with a multi-stage Dockerfile
- [ ] Extract auth and wallet modules into microservices when scale demands
- [x] Add integration tests with Testcontainers (PostgreSQL + Redis) — see `docs/local-test.md`
- [ ] Support multi-currency conversion with exchange-rate service

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

> **Assumption:** No `LICENSE` file currently exists in the repository. The MIT license is assumed as a sensible default for an open-source project. Replace with your preferred license if needed.
=======
# Digital-Banking
>>>>>>> origin/main
