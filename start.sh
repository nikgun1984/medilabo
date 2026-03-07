#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Medilabo Microservices Startup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}Docker is not running. Please start Docker Desktop first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker is running${NC}"
echo ""

# Build and start services
echo -e "${BLUE}Building and starting all services...${NC}"
echo ""

docker-compose up --build -d

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Services Starting...${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "Waiting for services to be ready..."
echo ""

# Wait for services to be healthy
sleep 10

echo -e "${GREEN}✓ Services are starting up${NC}"
echo ""
echo -e "${BLUE}Access your application:${NC}"
echo -e "  • Frontend Dashboard: ${GREEN}http://localhost:8080${NC}"
echo -e "  • API Gateway:        ${GREEN}http://localhost:8080${NC}"
echo -e "  • Demographics API:   ${GREEN}http://localhost:8080/api/demographics/health${NC}"
echo -e "  • Gateway Health:     ${GREEN}http://localhost:8080/actuator/health${NC}"
echo ""
echo -e "${BLUE}View logs:${NC}"
echo -e "  docker-compose logs -f"
echo ""
echo -e "${BLUE}Stop services:${NC}"
echo -e "  docker-compose down"
echo ""
