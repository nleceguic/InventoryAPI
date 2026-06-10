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

Tests use an H2 in-memory database — no PostgreSQL instance required.

| Suite | Type | Count |
|---|---|---|
| `ProductServiceTest` | Unit (Mockito) | 6 |
| `AuthServiceTest` | Unit (Mockito) | 3 |
| `ProductControllerTest` | Integration (@SpringBootTest + MockMvc) | 7 |

Total: **16 tests** (+ 1 smoke test).
