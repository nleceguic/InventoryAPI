# InventoryAPI

[![CI](https://github.com/nleceguic/InventoryAPI/actions/workflows/ci.yml/badge.svg)](https://github.com/nleceguic/InventoryAPI/actions/workflows/ci.yml)

REST API for inventory management built with Spring Boot 3.3. Implements JWT authentication, role-based access control (ADMIN / USER), and full CRUD for categories and products. Designed as a portfolio project demonstrating enterprise Java patterns.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 + JWT (jjwt 0.12) |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL 17 (prod) · H2 in-memory (tests) |
| Migrations | Flyway 10 |
| Docs | Springdoc OpenAPI 2.5 (Swagger UI) |
| Build | Maven 3.9 |
| Containers | Docker · Docker Compose |
| CI | GitHub Actions |

## Package Structure

```
com.nleceguic.inventory
├── config/          JwtAuthenticationFilter, SecurityConfig
├── controller/      AuthController, CategoryController, ProductController, UserController
├── dto/             Request/Response DTOs with Bean Validation
├── exception/       GlobalExceptionHandler (@RestControllerAdvice)
├── model/           User, Category, Product entities + Role enum
├── repository/      UserRepository, CategoryRepository, ProductRepository
└── service/         AuthService, CategoryService, JwtService, ProductService, UserDetailsServiceImpl
```

## API Endpoints

### Authentication — `/api/auth` (public)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register a new user (role USER) |
| POST | `/api/auth/login` | Login and receive JWT |

### Categories — `/api/categories`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/categories` | Any | List all categories |
| GET | `/api/categories/{id}` | Any | Get category by ID |
| POST | `/api/categories` | ADMIN | Create category |
| PUT | `/api/categories/{id}` | ADMIN | Update category |
| DELETE | `/api/categories/{id}` | ADMIN | Delete category |

### Products — `/api/products`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/products` | Any | List active products |
| GET | `/api/products/{id}` | Any | Get product by ID |
| GET | `/api/products/sku/{sku}` | Any | Get product by SKU |
| POST | `/api/products` | Any | Create product |
| PUT | `/api/products/{id}` | Any | Update product |
| PATCH | `/api/products/{id}/stock` | Any | Adjust stock quantity |
| DELETE | `/api/products/{id}` | ADMIN | Deactivate product |

### Users — `/api/users`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/users/me` | Any | Get authenticated user info |

> All endpoints except `/api/auth/**` require a valid JWT in the `Authorization: Bearer <token>` header.

Interactive documentation is available at **`http://localhost:8080/swagger`** once the application is running.

## Running with Docker Compose

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080` once the postgres healthcheck passes and migrations run.

To stop and remove volumes:

```bash
docker compose down -v
```

**Environment variables** (override in `docker-compose.yml` or via `.env`):

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/inventory_dev` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | *(set in compose file)* | HS256 signing secret — change in production |

## Local Development

### Prerequisites

- JDK 21 (Eclipse Temurin recommended)
- Maven 3.9+
- PostgreSQL 17 running on `localhost:5432` with database `inventory_dev`

### Run

```bash
mvn spring-boot:run
```

The application starts on port 8080. Flyway migrations run automatically on startup.

### Configuration

Default datasource credentials (`src/main/resources/application.yml`): `postgres / postgres`.
Override via environment variables or a local `application-local.yml` profile.

## Tests

```bash
mvn test
```

Tests use an H2 in-memory database — no PostgreSQL instance required. The test profile
now runs the real Flyway migrations (`V1`–`V5`) against H2 instead of letting Hibernate
generate the schema, so the repository-level tests below exercise the same SQL that runs
in production.

| Suite | Type | Count |
|---|---|---|
| `ProductServiceTest` | Unit (Mockito) | 6 |
| `AuthServiceTest` | Unit (Mockito) | 3 |
| `ProductControllerTest` | Integration (@SpringBootTest + MockMvc) | 7 |
| `CategoryControllerTest` | Integration (@SpringBootTest + MockMvc) | 7 |
| `AuthControllerIntegrationTest` | Integration (@SpringBootTest + MockMvc, no mocks) | 1 |
| `UserRepositoryTest` | Persistence (@DataJpaTest + Flyway/H2) | 5 |
| `CategoryRepositoryTest` | Persistence (@DataJpaTest + Flyway/H2) | 3 |
| `ProductRepositoryTest` | Persistence (@DataJpaTest + Flyway/H2) | 5 |

Total: **37 tests** (+ 1 smoke test).

## Design Decisions

### H2 vs. Testcontainers for integration tests

**Context.** The test profile (`application-test.yml`) points at an H2 in-memory
database instead of the PostgreSQL 17 used in production, and disables Flyway
(`flyway.enabled: false`) in favor of `ddl-auto: create-drop` — the schema
tests run against is generated from the JPA entity mappings, not from the
`db/migration` SQL files.

**Alternative considered.** Testcontainers with a real Postgres container per
test run. This is the more faithful option — it exercises the exact engine
used in production instead of a substitute — at the cost of needing Docker
available wherever tests run, plus container pull/startup time on every run.

**Why H2.** No Docker dependency for `mvn test` — this matters for a
portfolio project someone might clone and run without a Postgres/Docker setup
on hand. Tests start in-process and boot fast, and CI doesn't need a services
block.

**Honest trade-off.** The usual risk with this choice is behavioral
divergence from Postgres — different handling of specific column types,
Postgres-only functions, or constraint semantics that H2 doesn't replicate
exactly. Checking this codebase for that risk turned up something more basic:
**none of the current tests actually exercise the database through H2.**
`ProductServiceTest` and `AuthServiceTest` are pure Mockito unit tests with no
Spring context at all. `ProductControllerTest` boots a full `@SpringBootTest`
context (which is why H2 needs to be wired up in the first place — JPA
autoconfiguration wants a datasource), but `ProductService` is `@MockBean`,
so no test method ever reaches the repository layer. H2 currently exists to
let the Spring context start, not to validate persistence behavior.

That also means the `CHECK (role IN ('ADMIN', 'USER'))` constraint from
`V1__create_users_table.sql` is never in play during tests — Flyway is
skipped, and Hibernate's `create-drop` doesn't derive a check constraint from
`@Enumerated(EnumType.STRING)`. So today the H2-vs-Postgres question is
mostly moot: there's no JSONB, no native `@Query`, no Postgres-specific SQL
anywhere in `src/main`, and no test that would even notice if H2 and Postgres
disagreed. The trade-off is real but currently prospective, not something
this codebase has actually hit.

**When to migrate.** Testcontainers earns its cost once there's an actual
repository-level test suite (`@DataJpaTest` or similar) asserting on things
that can differ between engines: constraint enforcement (like the `role`
check above), unique-violation behavior, or any native query using
Postgres-specific syntax or types. Until repository tests exist, H2 is
validating "does the context wire up," and Testcontainers wouldn't change
that.

**Update — repository tests added.** The gap described above is now partly
closed. `UserRepositoryTest`, `CategoryRepositoryTest`, and
`ProductRepositoryTest` are `@DataJpaTest` suites that hit H2 for real: save
and reload each entity, exercise the custom repository queries
(`findByEmail`, `findByUsername`, `findByName`, `findBySku`,
`existsBySku`, `findByActiveTrue`), and — the specific case called out
above — insert a raw row with an invalid `role` value and confirm H2 rejects
it with a `DataIntegrityViolationException`.

To make that last check meaningful, the test profile (`application-test.yml`)
now runs Flyway (`flyway.enabled: true`) against H2 instead of relying on
`ddl-auto: create-drop`, with Hibernate set to `ddl-auto: validate` — the
same mode used against Postgres in `application.yml`. So the schema tests
run against is `V1`–`V5` executed for real, not a Hibernate-inferred
approximation, and the `CHECK (role IN ('ADMIN', 'USER'))` constraint from
`V1__create_users_table.sql` is now actually in play. Running those
migrations — including `BIGSERIAL` and `NOW()`, both Postgres-flavored
syntax — against H2 without any Postgres-compatibility mode worked without
changes, which resolved the main open question from the original analysis
above (whether H2 and Postgres would disagree on the SQL these migrations
use).

This still isn't the Testcontainers alternative discussed above — H2 is
still a different engine from the production Postgres, so silent
divergences on more exotic SQL (a native `@Query`, a Postgres-only type)
remain possible in principle. But the specific, concrete gap this project
had — no test ever reaching the database, and the `role` check never being
exercised — is closed. Testcontainers would still be the move if this
codebase grows Postgres-specific SQL that H2 can't faithfully emulate.
