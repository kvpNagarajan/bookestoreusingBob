# E-Bookstore Platform — Backend API
**IBM AI Specialist Capstone Project**

A production-ready Spring Boot REST API for an e-commerce bookstore platform, developed following the workflow described in the capstone specification (Slide 12).

---

## Architecture

```
ecom-backend/
├── src/main/java/com/ibm/capstone/ecom/
│   ├── config/          # OpenAPI / Swagger config
│   ├── controller/      # REST controllers (Auth, Product, Category, Cart, Order, User)
│   ├── dto/
│   │   ├── request/     # Incoming request payloads
│   │   └── response/    # Outgoing response payloads
│   ├── entity/          # JPA entities (User, Product, Category, Order, Cart, Payment, ...)
│   ├── exception/       # Global exception handler + custom exceptions
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT provider, filter, Spring Security config
│   └── service/         # Business logic interfaces + implementations
└── src/main/resources/
    ├── application.properties   # Configuration (uses env vars for secrets)
    └── schema.sql               # PostgreSQL DDL + seed data
```

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language  | Java 21   |
| Framework | Spring Boot 3.3.5 |
| Database  | PostgreSQL |
| Security  | Spring Security + JWT (jjwt 0.12.6) |
| API Docs  | SpringDoc OpenAPI (Swagger UI) |
| Build     | Maven |

---

## Prerequisites

1. **Java 21** — [Download](https://adoptium.net/)
2. **Maven 3.9+**
3. **PostgreSQL** (community edition) — [Download](https://www.postgresql.org/download/)

---

## Database Setup

```sql
-- Connect as postgres superuser and run:
CREATE DATABASE ecomdb;
CREATE USER ecomuser WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE ecomdb TO ecomuser;
\c ecomdb
GRANT ALL ON SCHEMA public TO ecomuser;
```

Then apply the schema:
```bash
psql -U ecomuser -d ecomdb -f src/main/resources/schema.sql
```

---

## Running the Application

### 1. Set required environment variables

**Windows PowerShell:**
```powershell
$env:DB_URL       = "jdbc:postgresql://localhost:5432/ecomdb"
$env:DB_USERNAME  = "ecomuser"
$env:DB_PASSWORD  = "your_secure_password"
$env:JWT_SECRET   = "your-256-bit-secret-key-minimum-32-characters"
```

**Linux / macOS:**
```bash
export DB_URL="jdbc:postgresql://localhost:5432/ecomdb"
export DB_USERNAME="ecomuser"
export DB_PASSWORD="your_secure_password"
export JWT_SECRET="your-256-bit-secret-key-minimum-32-characters"
```

> **Security note:** Never hardcode credentials. Use environment variables or a secrets manager.

### 2. Build and run

```bash
cd ecom-backend
mvn clean install
mvn spring-boot:run
```

The API starts at: `http://localhost:8080`

### 3. Swagger UI

Open in browser: `http://localhost:8080/swagger-ui.html`

---

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register a new account |
| POST | `/api/v1/auth/login` | Login — returns JWT |

### Products (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | List all products (paginated) |
| GET | `/api/v1/products/search?keyword=` | Search by title/author |
| GET | `/api/v1/products/latest` | Landing page featured books |
| GET | `/api/v1/products/{id}` | Product details |
| GET | `/api/v1/products/{id}/related` | Related products |

### Categories (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/categories` | All categories |
| GET | `/api/v1/categories/{id}/products` | Products in category |

### Cart (authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cart` | View cart |
| POST | `/api/v1/cart/items` | Add item to cart |
| PUT | `/api/v1/cart/items/{productId}` | Update quantity |
| DELETE | `/api/v1/cart/items/{productId}` | Remove item |
| DELETE | `/api/v1/cart` | Clear cart |

### Orders (authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders/checkout` | Place an order |
| GET | `/api/v1/orders` | Order history |
| GET | `/api/v1/orders/{id}` | Order details |
| POST | `/api/v1/orders/{id}/cancel` | Cancel order (within 48 hrs) |

### User Profile (authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get profile |
| GET | `/api/v1/users/me/addresses` | List addresses |
| POST | `/api/v1/users/me/addresses` | Add address |
| DELETE | `/api/v1/users/me/addresses/{id}` | Delete address |

---

## Git Workflow (Step 9 from Capstone Slide 12)

```bash
git init
git checkout -b feature/api-implementation
git add .
git commit -m "Implement Spring Boot API for e-commerce"
git push origin feature/api-implementation
# Then create a Pull Request on GitHub
```

---

## Security Notes

- Passwords hashed with BCrypt (strength 12)
- JWT tokens expire in 24 hours
- Server binds to `127.0.0.1` only (never `0.0.0.0`)
- No credentials in source code — all via environment variables
- Payment records store only masked transaction references
- TLS should be configured at the reverse-proxy layer in production
