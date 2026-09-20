# IBM E-Bookstore Platform
**IBM AI Specialist Cloud FullStack Capstone Project**

A full-stack e-commerce bookstore platform built as part of the IBM AI Specialist Capstone.

---

## Project Structure

```
bookstoreusingBob/
├── ecom-backend/              # Spring Boot REST API (Java 21)
│   ├── src/                   # Application source code
│   ├── frontend/              # Single-page web frontend (HTML/CSS/JS)
│   ├── docker/                # Container entrypoint scripts
│   ├── seed/                  # Database seeding scripts
│   ├── Dockerfile             # Spring Boot app container (UBI9 + OpenJDK 21)
│   ├── Dockerfile.postgres    # PostgreSQL container (UBI9 + PGDG)
│   ├── docker-compose.yml     # Compose file for local deployment
│   ├── .env.example           # Environment variable template
│   └── pom.xml                # Maven build descriptor
└── README.md
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL 16 |
| Container | Podman / Docker (Red Hat UBI9 base images) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Frontend | Vanilla HTML5 / CSS3 / JavaScript |
| Build | Maven 3.9 |

---

## Quick Start (Local — Podman)

### 1. Clone the repository
```bash
git clone https://github.com/kvpNagarajan/bookestoreusingBob.git
cd bookestoreusingBob
```

### 2. Create `.env` from template
```bash
cp ecom-backend/.env.example ecom-backend/.env
# Edit .env and fill in DB_PASSWORD and JWT_SECRET
```

### 3. Build and start containers
```bash
cd ecom-backend
podman compose --env-file .env up --build -d
```

### 4. Seed sample data (optional)
```bash
python ecom-backend/seed/seed.py
```

### 5. Serve the frontend
```bash
python ecom-backend/frontend/serve.py
```

### 6. Open in browser
- **Frontend:** http://localhost:3000
- **Swagger UI:** http://172.27.50.14:8080/swagger-ui/index.html
- **Health:** http://172.27.50.14:8080/actuator/health

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register account |
| POST | `/api/v1/auth/login` | Public | Login → returns JWT |
| GET | `/api/v1/products` | Public | List all products |
| GET | `/api/v1/products/search?keyword=` | Public | Search books |
| GET | `/api/v1/categories` | Public | List categories |
| GET | `/api/v1/cart` | JWT | View cart |
| POST | `/api/v1/cart/items` | JWT | Add to cart |
| POST | `/api/v1/orders/checkout` | JWT | Place order |
| GET | `/api/v1/orders` | JWT | Order history |
| GET | `/api/v1/users/me` | JWT | User profile |

---

## Security

- Passwords hashed with BCrypt (strength 12)
- JWT tokens signed with HS512, expire in 24 hours
- All secrets supplied via environment variables — never hardcoded
- Container images based on Red Hat UBI9 (no root execution)
- CORS restricted to localhost origins only

---

## License

IBM Capstone Project — for educational purposes.
