#!/bin/bash

# ============================================
# PEPs Health Check Script
# ============================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check service health
check_service() {
    local name=$1
    local url=$2
    
    if curl -f -s $url > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $name is healthy"
        return 0
    else
        echo -e "${RED}✗${NC} $name is not responding"
        return 1
    fi
}

# Main health check
echo "Checking PEPs services health..."
echo "================================"

all_healthy=true

# Check proxy
if ! check_service "Proxy" "http://localhost/health"; then
    all_healthy=false
fi

# Check backend
if ! check_service "Backend" "http://localhost:8080"; then
    all_healthy=false
fi

# Check frontend
if ! check_service "Frontend" "http://localhost:4200"; then
    all_healthy=false
fi

echo "================================"

# Docker stats
echo -e "\nDocker Container Status:"
docker-compose ps

echo -e "\nResource Usage:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"

if [ "$all_healthy" = true ]; then
    echo -e "\n${GREEN}All services are healthy!${NC}"
    exit 0
else
    echo -e "\n${RED}Some services are unhealthy${NC}"
    exit 1
fi
