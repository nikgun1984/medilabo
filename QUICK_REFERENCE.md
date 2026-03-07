# Medilabo Quick Reference

## Start Everything
```bash
./start.sh
```
or
```bash
docker-compose up --build
```

## Access URLs
- Frontend Dashboard: http://localhost:8080
- Gateway Health: http://localhost:8080/actuator/health
- Demographics Health: http://localhost:8080/api/demographics/health

## Common Commands

### View All Running Services
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
docker-compose logs -f postgres
```

### Stop Everything
```bash
docker-compose down
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

### Restart a Service
```bash
docker-compose restart <service-name>
```

### Rebuild a Service
```bash
docker-compose up --build <service-name>
```

### Enter a Container
```bash
docker-compose exec <service-name> /bin/sh
```

## Service Ports
- Gateway: 8080
- Demographics: 8081
- Frontend: 3000
- PostgreSQL: 5432

## Project Structure
```
medilabo/
├── demographics/      # Spring Boot microservice
├── gateway/          # Spring Cloud Gateway
├── frontend/         # Node.js Express frontend
├── docker-compose.yml
├── start.sh
└── README.md
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080
lsof -i :8081
lsof -i :3000

# Kill process
kill -9 <PID>
```

### Clean Docker Cache
```bash
docker system prune -a --volumes
```

### Rebuild from Scratch
```bash
docker-compose down -v
docker system prune -a
docker-compose up --build
```
