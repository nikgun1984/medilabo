# Medilabo Microservices Architecture

This project consists of three microservices orchestrated with Docker Compose:

## Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│   API Gateway       │ (Port 8080)
│ Spring Cloud Gateway│
└──────┬──────────────┘
       │
       ├──────────────────────┐
       │                      │
       ▼                      ▼
┌─────────────┐      ┌──────────────┐
│Demographics │      │  Frontend    │
│  Service    │      │   Service    │
│ (Port 8081) │      │ (Port 3000)  │
└──────┬──────┘      └──────────────┘
       │
       ▼
┌─────────────┐
│ PostgreSQL  │
│ (Port 5432) │
└─────────────┘
```

## Services

### 1. API Gateway (Port 8080)
- **Technology**: Spring Cloud Gateway
- **Purpose**: Routes all requests to appropriate microservices
- **Features**: 
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
- **Technology**: Node.js + Express
- **Purpose**: Serves the web interface
- **Features**:
  - Dashboard UI
  - Service health monitoring
  - Static file serving

### 4. PostgreSQL Database (Port 5432)
- **Technology**: PostgreSQL 16
- **Purpose**: Data persistence for demographics service
- **Credentials**:
  - Database: demographics_db
  - User: medilabo
  - Password: medilabo123

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
- Start Frontend service
- Start API Gateway

### 2. Access the Application

- **Frontend Dashboard**: http://localhost:8080
- **API Gateway**: http://localhost:8080
- **Demographics API**: http://localhost:8080/api/demographics/health
- **Gateway Health**: http://localhost:8080/actuator/health

### 3. Stop All Services

```bash
docker-compose down
```

### 4. Stop and Remove Volumes (Clean Restart)

```bash
docker-compose down -v
```

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
npm start
```

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
- `GET http://localhost:8080/` - Main dashboard
- `GET http://localhost:3000/health` - Frontend health (direct)

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
- **PostgreSQL**: pg_isready command
- **Demographics**: HTTP check on /api/demographics/health
- **Gateway**: HTTP check on /actuator/health
- **Frontend**: HTTP check on /health

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
├── gateway/               # API Gateway
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
├── frontend/             # Frontend microservice
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── Dockerfile
│   └── .dockerignore
├── docker-compose.yml    # Orchestration configuration
└── README.md            # This file
```

## Next Steps

1. **Add More Endpoints**: Expand the Demographics service with CRUD operations
2. **Add Authentication**: Implement JWT-based authentication in the gateway
3. **Add Service Discovery**: Integrate Eureka for dynamic service discovery
4. **Add Monitoring**: Add Prometheus and Grafana for monitoring
5. **Add Logging**: Centralize logs with ELK stack
6. **Add CI/CD**: Set up automated builds and deployments
7. **Add More Microservices**: Create additional services for notes, assessments, etc.

## Technology Stack

- **Backend Framework**: Spring Boot 4.0.2
- **Gateway**: Spring Cloud Gateway 2024.0.0
- **Database**: PostgreSQL 16
- **Frontend**: Node.js 18 + Express
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Java Version**: 21
- **Build Tool**: Maven

## License

This project is part of the Medilabo application suite.
