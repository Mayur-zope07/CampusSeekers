# CampusSeekers - Backend Foundation (Phase 1)

This project contains the production-ready backend foundation for **CampusSeekers**, an Admission Intelligence & Decision Support Platform. 

## Tech Stack
*   **Java 21**
*   **Spring Boot 3.3.2**
*   **Maven**
*   **PostgreSQL 16**
*   **Spring Security & stateless JWT**
*   **Flyway Database Migrations**
*   **Hibernate / Spring Data JPA**
*   **Lombok**
*   **MapStruct**
*   **Springdoc OpenAPI (Swagger UI)**
*   **Spring Boot Actuator**
*   **Docker & Docker Compose**

---

## Project Structure

```
com.campusseekers
├── config            # Core configuration classes (JPA Auditing, OpenAPI setup)
├── security
│   ├── config        # Spring Security Filter Chain & Password Encryptors
│   ├── filter        # OncePerRequest JWT Interceptor filter
│   ├── jwt           # JWT generation, token parser, EntryPoints & AccessDenied Handlers
│   └── service       # CustomUserDetailsService loading users from DB
├── controller        # Controller mapping handlers (public & secured)
├── dto               # Common payload classes (ApiResponse, User details)
├── entity            # Database Entity mappings (BaseEntity, User)
├── repository        # JPA data access repositories (UserRepository)
├── service           # Core Business services interfaces
├── mapper            # MapStruct converter interfaces
├── exception         # Custom exception hierarchy & GlobalExceptionHandler mapping errors
├── validation        # Reusable constraint validators (@ValidPassword annotation)
├── constants         # App-wide sharing configuration properties (AppConstants)
└── util              # General helper methods (SecurityUtils context details getter)
```

---

## Required Environment Variables

When running in production/containerized environments, specify the following variables:

| Variable | Description | Default (Local Dev) |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Spring Boot configuration profile (`dev`, `prod`) | `dev` |
| `DB_HOST` | Hostname of the PostgreSQL database instance | `localhost` |
| `DB_PORT` | Port of the PostgreSQL database | `5432` |
| `DB_NAME` | Database schema name | `campusseekers` |
| `DB_USER` | Database connection username | `postgres` |
| `DB_PASSWORD` | Database connection password | `postgres` |
| `JWT_SECRET` | HMAC-SHA signing token secret (Minimum 256-bit) | *(64-character hardcoded default)* |
| `JWT_EXPIRATION` | Token valid duration in milliseconds | `86400000` (24 hours) |

---

## Execution Instructions

### Option 1: Running Locally (Bare Metal)

#### Prerequisites
1. Install Java 21 JDK.
2. Install Maven 3.8+.
3. Running PostgreSQL instance (default credentials: `postgres`/`postgres`, schema `campusseekers`).

#### Build & Compilation
Navigate to the `backend/` folder and build the application:
```bash
cd backend
mvn clean compile
```

#### Run Spring Boot Application
Run using the Spring Boot plugin:
```bash
mvn spring-boot:run
```

Once running, the application exposes:
*   **API Base Path**: `http://localhost:8080`
*   **Swagger API Docs**: `http://localhost:8080/swagger-ui.html`
*   **Spring Actuator Health Endpoint**: `http://localhost:8080/actuator/health`

---

### Option 2: Running via Docker Compose (Recommended)

To start both PostgreSQL and the Spring Boot backend inside containers:

```bash
# In the workspace root containing docker-compose.yml
docker-compose up --build -d
```

To view application runtime logs:
```bash
docker-compose logs -f app
```

To stop containers and keep database volumes intact:
```bash
docker-compose down
```

To delete persistent volume data and restart clean:
```bash
docker-compose down -v
```

---

## Database Migrations (Flyway)

Migrations execute automatically when the application starts. 
*   **Migration location**: `backend/src/main/resources/db/migration/`
*   **Baseline Migration**: `V1__Initial_Schema.sql` builds the initial `users` table supporting roles mapping.

---

## Authentication & Security Setup

*   **Public Endpoints** (No authentication required):
    *   `/auth/**` (Registration, Token creation controllers)
    *   `/v3/api-docs/**` & `/swagger-ui/**` & `/swagger-ui.html` (Swagger documentation UI)
    *   `/actuator/health` & `/actuator/info` (Monitoring tools)
*   **Secured Endpoints** (Requires JWT header):
    *   Any other URL endpoint must include the Header: `Authorization: Bearer <token>`
    *   Requests failing validation return standard structured HTTP `401 Unauthorized` or `403 Forbidden` JSON details.
