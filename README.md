# Medilabo Microservices Architecture

<img src="https://raw.githubusercontent.com/nikgun1984/medilabo/main/app_ui.png" alt="Medilabo UI" width="780" />

This project consists of three microservices orchestrated with Docker Compose:

## Architecture Overview

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │  JWT Bearer Token
       ▼
┌─────────────────────┐
│   API Gateway       │ (Port 8080)
│ Spring Cloud Gateway│
│  + JWT Auth Filter  │
│  + /auth/login      │
└──────┬──────────────┘
       │  X-Auth-User header
       ├──────────────────────┬───────────────────┐
       │                      │                   │
       ▼                      ▼                   ▼
┌─────────────┐      ┌──────────────┐    ┌──────────────┐
│Demographics │      │    Notes     │    │  Frontend    │
│  Service    │      │   Service    │    │   Service    │
│ (Port 8081) │      │ (Port 8082)  │    │ (Port 3000)  │
└──────┬──────┘      └──────┬───────┘    └──────────────┘
       │                    │
       ▼                    ▼
┌─────────────┐      ┌──────────────┐
│ PostgreSQL  │      │   MongoDB    │
│ (Port 5432) │      │ (Port 27017) │
└─────────────┘      └──────────────┘
```

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
  - REST API
  - Database persistence
  - Health check endpoint

### 3. Frontend Service (Port 3000)
- **Technology**: React 18 + Vite + TypeScript
- **Purpose**: Serves the web interface
- **Features**:
  - Patient management UI (list, add, edit)
  - React Router for client-side navigation
  - React Hook Form for form handling
  - Axios for API communication
  - Tailwind CSS for styling
  - Served via Nginx in production (Docker)

### 4. PostgreSQL Database (Port 5432)
- **Technology**: PostgreSQL 16
- **Purpose**: Data persistence for demographics service
- **Credentials**:
  - Database: `demographics_db`
  - User: `medilabo`
  - Password: `medilabo123`

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
- Start PostgreSQL database
- Start Demographics service
- Start Frontend service (Nginx)
- Start API Gateway

### 2. Access the Application

- **Frontend Dashboard**: http://localhost:3000 (redirects to login page)
- **Login**: Use `doctor` / `doctor123` or `admin` / `admin123`
- **API Gateway**: http://localhost:8080
- **Auth Endpoint**: `POST http://localhost:8080/auth/login`
- **Demographics API**: http://localhost:8080/api/demographics/health (requires JWT)
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

After starting the stack for the first time, seed the database with sample patients:

```bash
docker exec -i medilabo-postgres psql -U medilabo -d demographics_db < scripts/insert_patients.sql
```

This inserts 4 test patients (TestNone, TestBorderline, TestInDanger, TestEarlyOnset) into the `patients` table. Without this step the patient list will appear empty even though the service is healthy.

> **Note**: The seed script is idempotent — it uses `CREATE TABLE IF NOT EXISTS`, but the `INSERT` statements will duplicate rows if run more than once. Re-run only on a fresh database or after `docker-compose down -v`.

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
Browser → nginx (port 3000) → Gateway (port 8080) → demographics/notes
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
- `GET http://localhost:3000/patients/add` - Add patient
- `GET http://localhost:3000/patients/:id/edit` - Edit patient

## Environment Variables

You can customize the services by setting environment variables in `docker-compose.yml`:

### Demographics Service
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### PostgreSQL
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

## Networking

All services communicate through the `medilabo-network` Docker bridge network:
- Service discovery by container name
- Isolated from host network
- Internal DNS resolution

## Health Checks

All services include health checks:
- **PostgreSQL**: `pg_isready` command
- **Demographics**: HTTP check on `/api/demographics/health`
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

## Next Steps

1. ~~**Add Authentication**: Implement JWT-based authentication in the gateway~~ ✅
2. **Add Service Discovery**: Integrate Eureka for dynamic service discovery
3. **Add Monitoring**: Add Prometheus and Grafana for monitoring
4. **Add Logging**: Centralize logs with ELK stack
5. **Add CI/CD**: Set up automated builds and deployments
6. **Add Risk Assessment Service**: Create the diabetes risk assessment microservice
7. **Add Tests**: Expand frontend test coverage with Vitest

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
