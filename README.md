# Medilabo Microservices Architecture

<img src="https://raw.githubusercontent.com/nikgun1984/medilabo/main/app_ui.png" alt="Medilabo UI" width="780" />

This project consists of five microservices and two databases orchestrated with Docker Compose:

## Architecture Overview

```
┌──────────────────┐
│  Client (Browser) │  ← React app runs HERE after initial page load
└───────┬──────────┘
        │  http://localhost:3000
        ▼
┌──────────────────────────────────┐
│  Frontend Service — Nginx        │ (Port 3000)
│                                  │
│  /                → serves static React files (HTML, JS, CSS)
│  /api/**, /auth/** → reverse-proxies to Gateway ──────┐
└──────────────────────────────────┘                     │
                                                         │
        ┌────────────────────────────────────────────────┘
        │  http://gateway:8080 (Docker internal)
        ▼
┌─────────────────────┐
│   API Gateway       │ (Port 8080)
│ Spring Cloud Gateway│
│  + JWT Auth Filter  │
│  + /auth/login      │
└──────┬──────────────┘
       │  X-Auth-User header
       ├──────────────────┬──────────────────┐
       │                  │                  │
       ▼                  ▼                  ▼
┌────────────┐     ┌──────────┐      ┌────────────┐
│Demographics│     │  Notes   │      │ Assessment │
│  Service   │     │ Service  │      │  Service   │
│(Port 8081) │     │(Port 8082)│     │(Port 8083) │
└─────┬──────┘     └────┬─────┘      └──┬────┬───┘
      │                  │               │    │
      ▼                  ▼               │    │  queries at runtime
┌────────────┐     ┌──────────┐          │    │  (no own database)
│ PostgreSQL │     │ MongoDB  │     ┌────┘    └────┐
│(Port 5432) │     │(Port     │     │              │
│            │     │  27017)  │     ▼              ▼
└────────────┘     └──────────┘  Demographics    Notes
                                  Service       Service
```

### How it works

1. **Browser → Nginx (port 3000)**: The user visits `localhost:3000`. Nginx serves the
   static React bundle (HTML, JS, CSS). The React app now runs **in the browser**.
2. **Browser → Nginx → Gateway**: When the React app makes API calls (`/api/**`, `/auth/**`),
   those requests go to Nginx, which **reverse-proxies** them to the Gateway inside Docker.
3. **Gateway → Backend Services**: The Gateway validates the JWT, adds an `X-Auth-User`
   header, and routes to Demographics, Notes, or Assessment.
4. **Assessment → Demographics + Notes**: The Assessment service has no database — it queries
   the other two services internally to compute diabetes risk levels.

## Services

### 1. API Gateway (Port 8080)
- **Technology**: Spring Cloud Gateway + Spring Security
- **Purpose**: Routes all requests to appropriate microservices and handles JWT authentication
- **Features**:
  - JWT token issuance (`POST /auth/login`)
  - JWT validation on all `/api/**` requests
  - Forwards authenticated user via `X-Auth-User` header to downstream services
  - Request routing
  - CORS configuration
  - Load balancing ready
  - Health monitoring

### 2. Demographics Service (Port 8081)
- **Technology**: Spring Boot 4.0.2 + JPA
- **Database**: PostgreSQL
- **Purpose**: Manages patient demographic information
- **Features**:
  - CRUD REST API for patients
  - Database persistence
  - Health check endpoint

### 3. Notes Service (Port 8082)
- **Technology**: Spring Boot 4.0.2 + Spring Data MongoDB
- **Database**: MongoDB
- **Purpose**: Stores and retrieves physician notes for each patient
- **Features**:
  - CRUD REST API for notes (`/api/notes/**`)
  - Notes linked to patients via `patId`
  - Preserves original formatting (line breaks, etc.)
  - No length limit on note content
  - Health check via Spring Actuator

### 4. Assessment Service (Port 8083)
- **Technology**: Spring Boot 4.0.2 (no database)
- **Purpose**: Computes diabetes risk levels for patients
- **Features**:
  - Queries Demographics and Notes services at runtime
  - Returns one of four risk levels: **None**, **Borderline**, **In Danger**, **Early Onset**
  - Risk calculation based on trigger terms, patient age, and gender
  - Health check via Spring Actuator
- **Trigger terms**: Hemoglobin A1C, Microalbumin, Height, Weight, Smoking, Abnormal, Cholesterol, Dizziness, Relapse, Reaction, Antibody

### 5. Frontend Service (Port 3000)
- **Technology**: React 18 + Vite + TypeScript
- **Purpose**: Serves the web interface
- **Features**:
  - Patient management UI (list, add, edit)
  - **Patient detail view** with notes history and risk assessment badge
  - Add notes directly from the patient detail page
  - React Router for client-side navigation
  - React Hook Form for form handling
  - Axios for API communication
  - Tailwind CSS for styling
  - Served via Nginx in production (Docker)

### 6. PostgreSQL Database (Port 5432)
- **Technology**: PostgreSQL 16
- **Purpose**: Data persistence for demographics service
- **Credentials**:
  - Database: `demographics_db`
  - User: `medilabo`
  - Password: `medilabo123`

### 7. MongoDB Database (Port 27017)
- **Technology**: MongoDB 7
- **Purpose**: Data persistence for notes service
- **Credentials**:
  - Database: `notes_db`
  - User: `medilabo`
  - Password: `medilabo123`
  - Auth source: `admin`

## Prerequisites

- Docker Desktop installed and running
- Java 21 (for local development)
- Maven (for local development)
- Node.js 18+ (for local development)

## Quick Start

### 1. Build and Start All Services

```bash
cd /Users/nickgundobin/Downloads/medilabo
docker-compose up --build
```

This will:
- Build Docker images for all services
- Start PostgreSQL and MongoDB databases
- Start Demographics, Notes, and Assessment services
- Start Frontend service (Nginx)
- Start API Gateway

### 2. Access the Application

- **Frontend Dashboard**: http://localhost:3000 (redirects to login page)
- **Login**: Use `doctor` / `doctor123` or `admin` / `admin123`
- **API Gateway**: http://localhost:8080
- **Auth Endpoint**: `POST http://localhost:8080/auth/login`
- **Demographics API**: http://localhost:8080/api/demographics/health (requires JWT)
- **Notes API**: http://localhost:8080/api/notes/ (requires JWT)
- **Assessment API**: http://localhost:8080/api/assessment/risk/{patId} (requires JWT)
- **Gateway Health**: http://localhost:8080/actuator/health

### 3. Stop All Services

```bash
docker-compose down
```

### 4. Stop and Remove Volumes (Clean Restart)

```bash
docker-compose down -v
```

## Database Seeding

After starting the stack for the first time, seed the databases with sample data:

### PostgreSQL – Patient records

```bash
docker exec -i medilabo-postgres psql -U medilabo -d demographics_db < scripts/insert_patients.sql
```

This inserts 4 test patients (TestNone, TestBorderline, TestInDanger, TestEarlyOnset) into the `patients` table.

### MongoDB – Physician notes

```bash
docker exec -i medilabo-mongodb mongosh -u medilabo -p medilabo123 \
  --authenticationDatabase admin notes_db < scripts/insert_notes.js
```

This inserts sample physician notes for each test patient.

> **Note**: The seed scripts are idempotent for table/collection creation, but the `INSERT` / `insertMany` statements will duplicate rows if run more than once. Re-run only on a fresh database or after `docker-compose down -v`.

## Development

### Run Individual Services Locally

#### Demographics Service
```bash
cd demographics
./mvnw spring-boot:run
```

#### Gateway Service
```bash
cd gateway
./mvnw spring-boot:run
```

#### Frontend Service
```bash
cd frontend
npm install
npm run dev
```

### Frontend Scripts

| Script | Command | Description |
|--------|---------|-------------|
| Dev server | `npm run dev` | Start Vite dev server with HMR |
| Build | `npm run build` | Type-check + production build |
| Lint | `npm run lint` | Lint with Biome |
| Format | `npm run format` | Format with Biome |
| Check | `npm run check` | Lint + format + fix with Biome |
| Preview | `npm run preview` | Preview production build |

## JWT Authentication

The application uses **JWT (JSON Web Token)** authentication. All API requests must include a valid JWT token.

### How It Works

1. **Login**: The user submits credentials to `POST /auth/login` on the Gateway
2. **Token Issued**: The Gateway validates credentials and returns a signed JWT
3. **Authenticated Requests**: The frontend stores the JWT in `localStorage` and attaches it as an `Authorization: Bearer <token>` header on every API request
4. **Gateway Validation**: The Gateway's `JwtAuthenticationFilter` validates the token on all `/api/**` requests
5. **User Forwarding**: On valid tokens, the Gateway forwards the username to downstream services via the `X-Auth-User` header
6. **401 Handling**: If the token is missing/expired/invalid, the Gateway returns a `401 Unauthorized` JSON response, and the frontend redirects to the login page

### Demo Credentials

| Username | Password | Role |
|----------|----------|------|
| `doctor` | `doctor123` | Physician |
| `admin` | `admin123` | Administrator |

### Auth API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|:------------:|
| `POST` | `/auth/login` | Authenticate and receive JWT | ❌ |
| `GET` | `/actuator/health` | Gateway health check | ❌ |
| `*` | `/api/**` | All API endpoints | ✅ |

### JWT Configuration

| Property | Default | Environment Variable |
|----------|---------|---------------------|
| Secret key | Base64-encoded HMAC key | `JWT_SECRET` |
| Token TTL | 24 hours (86400000 ms) | `JWT_EXPIRATION_MS` |

### Request Flow

```
Browser ──→ Nginx (port 3000) ──proxy──→ Gateway (port 8080) ──→ demographics/notes/assessment
                                              │
                                         JWT validated
                                         X-Auth-User header added
```

## Code Quality

The frontend uses **[Biome](https://biomejs.dev/)** for linting and formatting, replacing ESLint and Prettier with a single fast tool.

```bash
cd frontend

# Lint all files
npm run lint

# Format all files
npm run format

# Run both lint + format and auto-fix
npm run check
```

Biome is configured in `frontend/biome.json` with:
- Single quotes, no semicolons, 2-space indent, 100-char line width
- Recommended rules for correctness, performance, security, and style
- Automatic import organisation

## Docker Commands

### View Running Containers
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f demographics
docker-compose logs -f gateway
docker-compose logs -f frontend
```

### Restart a Service
```bash
docker-compose restart demographics
```

### Rebuild a Service
```bash
docker-compose up --build demographics
```

## API Endpoints

### Demographics Service (via Gateway)
- `GET http://localhost:8080/api/demographics/health` - Health check

### Gateway Actuator
- `GET http://localhost:8080/actuator/health` - Gateway health
- `GET http://localhost:8080/actuator/gateway/routes` - View all routes

### Frontend
- `GET http://localhost:3000/` - Patient list
- `GET http://localhost:3000/patients/new` - Add patient
- `GET http://localhost:3000/patients/:id/view` - View patient details, notes & risk assessment
- `GET http://localhost:3000/patients/:id/edit` - Edit patient

## Environment Variables

You can customize the services by setting environment variables in `docker-compose.yml`:

### Demographics Service
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Notes Service
- `SPRING_DATA_MONGODB_URI`

### Assessment Service
- `DEMOGRAPHICS_URL` — base URL of Demographics service (e.g. `http://demographics:8081`)
- `NOTES_URL` — base URL of Notes service (e.g. `http://notes:8082`)

### Gateway
- `JWT_SECRET` — Base64-encoded HMAC signing key
- `JWT_EXPIRATION_MS` — token time-to-live in milliseconds (default `86400000`)

### PostgreSQL
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

### MongoDB
- `MONGO_INITDB_ROOT_USERNAME`
- `MONGO_INITDB_ROOT_PASSWORD`
- `MONGO_INITDB_DATABASE`

## Networking

All services communicate through the `medilabo-network` Docker bridge network:
- Service discovery by container name
- Isolated from host network
- Internal DNS resolution

## Health Checks

All services include health checks:
- **PostgreSQL**: `pg_isready` command
- **MongoDB**: `mongosh` ping command
- **Demographics**: HTTP check on `/api/demographics/health`
- **Notes**: HTTP check on `/actuator/health`
- **Assessment**: HTTP check on `/actuator/health`
- **Gateway**: HTTP check on `/actuator/health`
- **Frontend**: HTTP check on `/health` (Nginx)

## Troubleshooting

### Service Won't Start
1. Check logs: `docker-compose logs <service-name>`
2. Verify port availability: `lsof -i :<port-number>`
3. Ensure Docker has enough resources

### Database Connection Issues
1. Wait for PostgreSQL to be fully ready (check health status)
2. Verify connection string in demographics service
3. Check credentials match

### Gateway Cannot Reach Services
1. Verify all services are in the same network
2. Check service names in gateway configuration
3. Ensure services are healthy

### Build Failures
1. Clear Docker cache: `docker-compose build --no-cache`
2. Remove old images: `docker system prune -a`
3. Check Dockerfile syntax

## Project Structure

```
medilabo/
├── demographics/           # Demographics microservice
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── notes/                 # Notes microservice (MongoDB)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── assessment/            # Assessment microservice (no database)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── gateway/               # API Gateway + JWT Authentication
│   ├── src/
│   │   └── main/java/com/medilabo/gateway/
│   │       ├── controller/    # AuthController (login endpoint)
│   │       └── security/      # JWT filter, config, util
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── frontend/              # Frontend microservice
│   ├── src/
│   │   ├── api/           # Axios API clients (with JWT interceptors)
│   │   ├── components/    # Shared React components (Layout, ProtectedRoute)
│   │   ├── context/       # AuthContext (JWT state management)
│   │   ├── pages/         # Route-level page components (incl. LoginPage)
│   │   └── types/         # TypeScript type definitions
│   ├── biome.json         # Biome linter/formatter config
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── nginx.conf         # Nginx config for production
│   ├── Dockerfile
│   └── .dockerignore
├── scripts/               # SQL/JS seed scripts
├── docker-compose.yml     # Orchestration configuration
└── README.md              # This file
```

## Green Code Principles

This project follows **Green Software Engineering** practices to minimise energy consumption, resource usage, and carbon footprint. Every CPU cycle, network call, and byte stored costs electricity — the principles below reduce that cost.

### 1. Lightweight Docker Images

All Dockerfiles use **Alpine-based** or **JRE-only** images to keep image sizes small, which means less disk I/O, faster pulls, and lower memory usage at runtime.

| Service | Base image | Why it's green |
|---------|-----------|----------------|
| Frontend (build) | `node:18-alpine` | ~50 MB vs ~350 MB for `node:18` |
| Frontend (serve) | `nginx:alpine` | ~7 MB — no Node.js runtime in production |
| PostgreSQL | `postgres:16-alpine` | Minimal OS layer |
| Java services | `eclipse-temurin:21-jre` | JRE only — no compiler/dev tools shipped |

### 2. Multi-Stage Docker Builds

Every Dockerfile uses a **two-stage build**: a heavy build image compiles the code, then only the compiled artefact is copied into a minimal runtime image. Build tools (Maven, npm, tsc) are discarded entirely.

```
Stage 1: maven + JDK → compiles .jar      ← thrown away
Stage 2: JRE only    → runs .jar           ← shipped
```

This reduces final image size by **60–80%**, meaning less storage, less network transfer, and less memory.

### 3. No Redundant Runtime — Static Frontend

The frontend uses **Nginx to serve static files** (HTML, JS, CSS) instead of running a Node.js process. After the Vite build, there is no JavaScript runtime in the container — just Nginx serving pre-built files. This uses a fraction of the memory and CPU of a Node.js server.

### 4. Database-Free Assessment Service

The Assessment microservice has **no database of its own**. It queries Demographics and Notes at runtime, avoiding:
- Duplicate data storage
- Synchronisation overhead
- An additional database container consuming memory and CPU at idle

### 5. Docker Health Checks

All services declare health checks in `docker-compose.yml`. Docker automatically restarts **unhealthy** containers rather than requiring over-provisioned replicas "just in case." This keeps the running footprint minimal.

### 6. `.dockerignore` Files

Each service includes a `.dockerignore` that excludes `target/`, IDE files, test output, and other development artefacts from the Docker build context. This reduces:
- Build context size (less data sent to the Docker daemon)
- Build time (fewer files to copy)
- Layer cache invalidation (unrelated file changes don't trigger rebuilds)

### 7. Efficient Dependency Installation

- **Java services**: Maven downloads dependencies in a separate layer (`COPY pom.xml` first, then `RUN mvn package`), so dependencies are cached and only re-downloaded when `pom.xml` changes.
- **Frontend**: Uses `npm ci` (clean install) which is faster and more deterministic than `npm install`, and the `package-lock.json` layer is cached independently.

### 8. Vite Tree-Shaking & Code Splitting

Vite's production build automatically **tree-shakes** unused code and **splits** bundles, so the browser downloads only the JavaScript it actually needs. Smaller bundles = less network transfer = less energy on both client and server.

### 9. Single-Origin Architecture (Nginx Reverse Proxy)

All API traffic flows through Nginx on the same origin (`localhost:3000`), eliminating **CORS preflight requests**. Without this, every API call from the browser would first send an `OPTIONS` request — doubling the network traffic for no functional benefit.

### 10. Scoped Logging

Services use appropriate log levels (INFO in production, not DEBUG). Excessive logging wastes I/O bandwidth, disk space, and CPU cycles for string formatting — especially in high-throughput paths.

### Summary

| Principle | Where applied |
|-----------|--------------|
| Lightweight images | All Dockerfiles (Alpine / JRE-only) |
| Multi-stage builds | All Dockerfiles |
| No runtime overhead | Frontend — Nginx serves static files, no Node.js |
| No redundant storage | Assessment — no database, queries on demand |
| Auto-recovery | Docker health checks on every service |
| Small build context | `.dockerignore` in each service |
| Cached dependencies | Maven layer caching, `npm ci` |
| Minimal bundle size | Vite tree-shaking + code splitting |
| No preflight waste | Single-origin via Nginx reverse proxy |
| Scoped logging | INFO-level in production |

> **Reference**: [Green Software Foundation — SCI Specification](https://greensoftware.foundation)

## Next Steps

1. ~~**Add Authentication**: Implement JWT-based authentication in the gateway~~ ✅
2. ~~**Add Notes Service**: Create MongoDB-backed physician notes microservice~~ ✅
3. ~~**Add Risk Assessment Service**: Create the diabetes risk assessment microservice~~ ✅
4. **Add Service Discovery**: Integrate Eureka for dynamic service discovery
5. **Add Monitoring**: Add Prometheus and Grafana for monitoring
6. **Add Logging**: Centralize logs with ELK stack
7. **Add CI/CD**: Set up automated builds and deployments
8. **Add Tests**: Expand frontend test coverage with Vitest

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend Framework | Spring Boot 4.0.2 |
| API Gateway | Spring Cloud Gateway 2024.0.1 |
| Authentication | JWT (jjwt 0.12.6) + Spring Security |
| Relational Database | PostgreSQL 16 |
| Document Database | MongoDB 7 |
| Frontend | React 18 + Vite + TypeScript |
| Styling | Tailwind CSS |
| Forms | React Hook Form |
| HTTP Client | Axios |
| Linter / Formatter | Biome 1.9.4 |
| Production Server | Nginx |
| Containerisation | Docker |
| Orchestration | Docker Compose |
| Java Version | 21 |
| Build Tool | Maven |

## License

This project is part of the Medilabo application suite.
