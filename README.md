# WASAC/REG Unified Utility Billing System

Spring Boot backend for unified water and electricity utility billing (WASAC/REG).

## Stack

- Java 17, Spring Boot 3.5
- PostgreSQL, Spring Data JPA
- JWT authentication, springdoc-openapi (Swagger)
- Lombok, ModelMapper

## Prerequisites

- JDK 17+
- Maven 3.9+
- PostgreSQL 14+

## Database Setup

```bash
psql -U postgres -f database-setup.sql
```

Update credentials in `src/main/resources/application.properties` if needed.

After the first application run (tables created by Hibernate), apply triggers:

```bash
psql -U postgres -f database-triggers.sql
```

## Run

```bash
cd wasac-reg-unified-billing-system
mvn spring-boot:run
```

Or compile only:

```bash
mvn compile
```

## Swagger UI

http://localhost:8080/swagger-ui.html

## Default Users (seeded on startup)

| Role     | Email              | Password      |
|----------|--------------------|---------------|
| ADMIN    | ruyangearnold@gmail.com | Admin@123     |
| OPERATOR | operator@wasac.rw  | Operator@123  |
| FINANCE  | finance@wasac.rw   | Finance@123   |
| CUSTOMER | customer@wasac.rw  | Customer@123  |

## API Overview

| Module        | Base Path                    |
|---------------|------------------------------|
| Auth          | `/api/v1/auth`               |
| Customers     | `/api/v1/customers`          |
| Meters        | `/api/v1/meters`             |
| Meter Readings| `/api/v1/meter-readings`     |
| Tariff Config | `/api/v1/config`             |
| Bills         | `/api/v1/bills`              |
| Payments      | `/api/v1/payments`           |
| Notifications | `/api/v1/notifications`    |

## Typical Flow

1. Login as operator → record meter reading
2. Generate bill from reading
3. Customer records payment → finance approves
4. DB trigger sends notification on bill insert and full payment

See [ARCHITECTURE.md](ARCHITECTURE.md) for ERD and system flow.
