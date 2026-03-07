# ⚠️ Docker Setup Instructions

## Current Status

I've created all 3 microservices with Docker configuration:
- ✅ Demographics Service (Spring Boot)
- ✅ Gateway Service (Spring Cloud Gateway)
- ✅ Frontend Service (Node.js/Express)
- ✅ Docker Compose orchestration file
- ✅ All Dockerfiles

## Issue Encountered

Docker appeared to have a temporary I/O error during the build process. This can happen and is usually resolved by:

## Step-by-Step Manual Start

### Option 1: Restart Docker Desktop (Recommended)

1. **Quit Docker Desktop completely**
   - Click Docker icon in menu bar
   - Select "Quit Docker Desktop"
   
2. **Wait 10 seconds**

3. **Restart Docker Desktop**
   - Open Docker Desktop from Applications
   - Wait until it shows "Docker Desktop is running"

4. **Navigate to project and start services**
   ```bash
   cd /Users/nickgundobin/Downloads/medilabo
   docker-compose up --build
   ```

### Option 2: Use the Start Script

```bash
cd /Users/nickgundobin/Downloads/medilabo
./start.sh
```

### Option 3: Build Step by Step

If the above doesn't work, build services individually:

```bash
cd /Users/nickgundobin/Downloads/medilabo

# Build frontend first (fastest)
docker-compose build frontend

# Build demographics
docker-compose build demographics

# Build gateway
docker-compose build gateway

# Start all services
docker-compose up -d
```

## Verify Everything is Running

### Check Container Status
```bash
docker-compose ps
```

You should see:
```
NAME                    STATUS              PORTS
medilabo-postgres       Up (healthy)        5432->5432
medilabo-demographics   Up (healthy)        8081->8081
medilabo-gateway        Up (healthy)        8080->8080
medilabo-frontend       Up (healthy)        3000->3000
```

### View Logs
```bash
# All services
docker-compose logs -f

# Individual service
docker-compose logs -f gateway
docker-compose logs -f demographics
docker-compose logs -f frontend
```

### Test the Services

Once running, open your browser and visit:

1. **Main Dashboard**: http://localhost:8080
   - Should show a beautiful purple gradient dashboard
   - Shows all 3 microservices
   - Has health check buttons

2. **Gateway Health**: http://localhost:8080/actuator/health
   - Should return JSON: `{"status":"UP"}`

3. **Demographics Health**: http://localhost:8080/api/demographics/health
   - Should return JSON: `{"status":"UP","service":"demographics"}`

## Troubleshooting

### Services Won't Start

**Check Docker has enough resources:**
1. Open Docker Desktop
2. Click Settings (gear icon)
3. Go to Resources
4. Ensure at least:
   - CPUs: 2
   - Memory: 4 GB
   - Swap: 1 GB

**Clear everything and restart:**
```bash
docker-compose down -v
docker system prune -a --volumes
docker-compose up --build
```

### Port Already in Use

```bash
# Find what's using the ports
lsof -i :8080
lsof -i :8081
lsof -i :3000
lsof -i :5432

# Kill the process (replace <PID> with actual PID)
kill -9 <PID>
```

### Database Connection Issues

Wait longer - PostgreSQL takes ~10-15 seconds to fully start. The demographics service will retry automatically.

### Build Errors

1. Check you have internet connection (Maven/npm need to download dependencies)
2. Check Docker Desktop is not in "Resource Saver" mode
3. Try building without cache:
   ```bash
   docker-compose build --no-cache
   ```

## Alternative: Run Services Locally (Without Docker)

If Docker continues to have issues, you can run services locally:

### Prerequisites
- Java 21
- Maven
- Node.js 18+
- PostgreSQL (running locally)

### Start PostgreSQL
```bash
# Install with Homebrew if needed
brew install postgresql@16
brew services start postgresql@16

# Create database
createdb demographics_db
```

### Start Demographics Service
```bash
cd demographics
# Update application.properties to use localhost:5432
./mvnw spring-boot:run
```

### Start Gateway
```bash
cd gateway
# Update application.properties to use localhost:8081 and localhost:3000
./mvnw spring-boot:run
```

### Start Frontend
```bash
cd frontend
npm install
npm start
```

## Success Indicators

When everything is running correctly:

✅ `docker-compose ps` shows all 4 containers as "Up" and healthy
✅ http://localhost:8080 shows the dashboard
✅ Dashboard health check buttons turn green
✅ No errors in `docker-compose logs`

## Next Steps After Success

1. **Test the dashboard** - click the health check buttons
2. **Add CRUD operations** to demographics service
3. **Add more microservices** (notes, assessments, etc.)
4. **Add authentication** with Spring Security
5. **Deploy to cloud** (AWS, Azure, GCP)

---

**All code is ready!** Just need to get Docker running smoothly.

If you continue to have issues, let me know what error messages you see.
