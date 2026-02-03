#!/bin/bash

# ============================================
# PEPs Docker Deployment Script
# ============================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose.yml"
COMPOSE_PROD_FILE="docker-compose.prod.yml"
HEALTH_CHECK_RETRIES=30
HEALTH_CHECK_INTERVAL=10

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

check_env_file() {
    if [ ! -f .env ]; then
        log_warn ".env file not found, creating from .env.example"
        if [ -f .env.example ]; then
            cp .env.example .env
            log_info "Please configure .env file before deployment"
            exit 0
        else
            log_error ".env.example not found"
            exit 1
        fi
    fi
}

build_services() {
    log_info "Building Docker images..."
    
    log_info "Building backend..."
    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD_FILE build backend
    
    log_info "Building frontend..."
    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD_FILE build frontend
    
    log_info "Building proxy..."
    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD_FILE build proxy
    
    log_info "All images built successfully"
}

start_services() {
    log_info "Starting services..."
    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD_FILE up -d
    log_info "Services started"
}

wait_for_health() {
    local service=$1
    local url=$2
    local retries=$HEALTH_CHECK_RETRIES
    
    log_info "Waiting for $service to be healthy..."
    
    while [ $retries -gt 0 ]; do
        if curl -f -s $url > /dev/null 2>&1; then
            log_info "$service is healthy"
            return 0
        fi
        
        retries=$((retries-1))
        echo -n "."
        sleep $HEALTH_CHECK_INTERVAL
    done
    
    log_error "$service failed to become healthy"
    return 1
}

health_checks() {
    log_info "Running health checks..."
    
    # Wait for backend
    if ! wait_for_health "Backend" "http://localhost:8080"; then
        log_error "Backend health check failed"
        rollback
        exit 1
    fi
    
    # Wait for frontend
    if ! wait_for_health "Frontend" "http://localhost:4200"; then
        log_error "Frontend health check failed"
        rollback
        exit 1
    fi
    
    # Wait for proxy
    if ! wait_for_health "Proxy" "http://localhost/health"; then
        log_error "Proxy health check failed"
        rollback
        exit 1
    fi
    
    log_info "All health checks passed"
}

rollback() {
    log_warn "Rolling back deployment..."
    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD_FILE down
    log_info "Rollback completed"
}

show_status() {
    log_info "Deployment Status:"
    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD_FILE ps
    
    log_info "\nService URLs:"
    echo "  - Frontend: http://localhost"
    echo "  - Backend API: http://localhost/api"
    echo "  - Health Check: http://localhost/health"
}

# Main deployment flow
main() {
    log_info "Starting PEPs deployment..."
    
    check_prerequisites
    check_env_file
    build_services
    start_services
    health_checks
    show_status
    
    log_info "Deployment completed successfully!"
}

# Run main function
main
