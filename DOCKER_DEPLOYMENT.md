# Docker Deployment Guide - PEPs Project

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Advanced Topics](#advanced-topics)

## Architecture Overview

The PEPs application uses a modern microservices architecture with Docker containers:

```
┌─────────────────────────────────────────────────────────────┐
│                    Nginx Reverse Proxy                      │
│                      (Port 80/443)                          │
│  - Routes /api → Backend                                    │
│  - Routes / → Frontend                                      │
│  - SSL/TLS termination                                      │
│  - Compression and caching                                  │
└────────────┬────────────────────────────┬───────────────────┘
             │                            │
    ┌────────▼────────┐          ┌───────▼────────┐
    │   Frontend      │          │    Backend     │
    │   (Angular)     │          │  (Java/Spring) │
    │   + Nginx       │          │   + Tomcat     │
    └─────────────────┘          └────────┬───────┘
                                          │
                                 ┌────────▼────────┐
                                 │   PostgreSQL    │
                                 │   (External)    │
                                 └─────────────────┘
```

### Services

1. **Proxy (Nginx)**: Reverse proxy handling all incoming traffic
2. **Backend (Java/Spring)**: REST API running on Tomcat
3. **Frontend (Angular)**: SPA served by Nginx
4. **Database (PostgreSQL)**: External database (not containerized by default)
5. **Object Storage (MinIO)**: S3-compatible storage for sound files

## Prerequisites

### Required Software
- Docker Engine 20.10+
- Docker Compose 2.0+
- Git

### System Requirements
- **Development**: 4GB RAM, 2 CPU cores
- **Production**: 8GB RAM, 4 CPU cores

### Check Installation
```bash
docker --version
docker-compose --version
```

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd PEPs
```

### 2. Configure Environment
```bash
# Copy environment template
cp .env.example .env

# Edit configuration (see Configuration section)
nano .env
```

### 3. Deploy
```bash
# Using deployment script (recommended)
./scripts/deploy.sh

# Or manually
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 4. Verify Deployment
```bash
# Run health checks
./scripts/health-check.sh

# Check logs
docker-compose logs -f
```

### 5. Access the Application
- **Frontend**: http://localhost
- **Backend API**: http://localhost/api
- **Health Check**: http://localhost/health

## Configuration

### Environment Variables

Create a `.env` file based on `.env.example`:

```bash
# Proxy Configuration
PROXY_PORT=80
PROXY_SSL_PORT=443

# Backend Configuration
SPRING_PROFILE=
DB_HOST=
DB_PORT=
DB_NAME=
DB_USER=
DB_PASSWORD=
MINIO_ENDPOINT=
MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
MINIO_BUCKET=

# Frontend Configuration
API_URL=
```

### MinIO Configuration

The backend stores sound files in MinIO (S3-compatible object storage). Set in `.env`:

```bash
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=peps-sounds
```

Bucket setup (one-time):
```bash
docker compose up -d minio
mc alias set local http://localhost:9000 "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY"
mc anonymous set private local/$MINIO_BUCKET
```

### Database Configuration

The application expects an external PostgreSQL database. Configure the connection in `.env`:

```bash
DB_HOST=
DB_PORT=
DB_NAME=
DB_USER=
DB_PASSWORD=
```

> [!IMPORTANT]
> Never commit `.env` files with real credentials to version control!

## Deployment

### Development Deployment

For local development with hot reload:

```bash
# Uses docker-compose.yml + docker-compose.override.yml
docker-compose up -d
```

Features:
- Volume mounts for source code
- Debug ports exposed
- Development tools enabled

### Production Deployment

For production with optimized settings:

```bash
# Uses docker-compose.yml + docker-compose.prod.yml
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Features:
- Resource limits
- Health checks
- Restart policies
- Security hardening
- **Nginx Proxy Caching**: Disk-based cache for audio files
- **Gzip/Buffer Optimizations**: Tuned for binary streaming and large requests

### Using Deployment Scripts

#### Bash (Linux/Mac)
```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

#### PowerShell (Windows)
```powershell
.\scripts\deploy.ps1
```

The deployment script will:
1. Check prerequisites
2. Verify environment configuration
3. Build Docker images
4. Start services
5. Run health checks
6. Display deployment status

## Monitoring

### Health Checks

Run the health check script:
```bash
./scripts/health-check.sh
```

Manual health checks:
```bash
# Proxy health
curl http://localhost/health

# Backend health
curl http://localhost:8080/actuator/health

# Frontend health
curl http://localhost:4200/
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f proxy

# Last 100 lines
docker-compose logs --tail=100
```

### Resource Usage

```bash
# Real-time stats
docker stats

# Container status
docker-compose ps
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Problem**: Port 80 or 8080 is already in use

**Solution**:
```bash
# Find process using port
netstat -ano | findstr :80

# Change port in .env
PROXY_PORT=8000
```

#### 2. Build Failures

**Problem**: Docker build fails

**Solution**:
```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache
```

#### 3. Health Check Failures

**Problem**: Services fail health checks

**Solution**:
```bash
# Check logs
docker-compose logs backend

# Restart service
docker-compose restart backend

# Check database connectivity
docker-compose exec backend curl -f http://localhost:8080/actuator/health
```

#### 4. Database Connection Issues

**Problem**: Backend cannot connect to database

**Solution**:
```bash
# Verify database is accessible
ping your_db_host

# Check credentials in .env
cat .env | grep DB_

# Test database connection
docker-compose exec backend bash
# Inside container:
curl -v telnet://your_db_host:5432
```

### Debug Mode

Enable debug logging:

```bash
# Backend debug
docker-compose exec backend bash
tail -f /usr/local/tomcat/logs/catalina.out

# Frontend debug
docker-compose exec frontend sh
cat /var/log/nginx/error.log
```

## Advanced Topics

### SSL/TLS Configuration

To enable HTTPS:

1. Place SSL certificates in `nginx/certs/`:
   - `cert.pem`
   - `key.pem`

2. Update `nginx/conf.d/default.conf`:
```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/certs/cert.pem;
    ssl_certificate_key /etc/nginx/certs/key.pem;
    # ... rest of configuration
}
```

3. Restart proxy:
```bash
docker-compose restart proxy
```

### Scaling Services

Scale frontend for load balancing:

```bash
docker-compose up -d --scale frontend=3
```

### Custom Resource Limits

Edit `docker-compose.prod.yml`:

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
```

### Database in Docker (Optional)

To run PostgreSQL in Docker, uncomment the database service in `docker-compose.yml`:

```yaml
database:
  image: postgres:15-alpine
  # ... configuration
```

### Backup and Restore

#### Backup
```bash
# Backup volumes
docker run --rm -v peps_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz /data
```

#### Restore
```bash
# Restore volumes
docker run --rm -v peps_postgres_data:/data -v $(pwd):/backup alpine tar xzf /backup/postgres_backup.tar.gz -C /
```

### CI/CD Integration

The project includes GitHub Actions workflow for automated builds. See `.github/workflows/docker-build.yml`.

### Performance Tuning

#### Backend JVM Options
Edit `back/PEPs_back/Dockerfile`:
```dockerfile
ENV CATALINA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC"
```

#### Nginx Caching
Edit `nginx/conf.d/default.conf` to adjust cache settings.

## Maintenance

### Update Images

```bash
# Pull latest base images
docker-compose pull

# Rebuild
docker-compose build

# Restart
docker-compose up -d
```

### Clean Up

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (WARNING: deletes data)
docker-compose down -v

# Clean Nginx cache manually
rm -rf /var/cache/nginx/audio_cache/*

# Clean unused images
docker image prune -a
```

## Security Best Practices

> [!CAUTION]
> Follow these security guidelines in production:

1. **Never expose database ports** directly to the internet
2. **Use strong passwords** for all services
3. **Enable SSL/TLS** for production deployments
4. **Regularly update** base images and dependencies
5. **Limit resource usage** to prevent DoS attacks
6. **Use secrets management** for sensitive data
7. **Enable firewall rules** to restrict access
8. **Monitor logs** for suspicious activity

## Support

For issues or questions:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review Docker logs
3. Consult the project README
4. Open an issue on GitHub

## VPS & IoT Specifics

### 1. IoT Security Model
The system uses a hybrid security model to accommodate both web users and IoT devices:
- **Web UI**: Strictly protected via JWT in HttpOnly cookies.
- **IoT Endpoints**: `/api/modules` and `/api/interactions` allow public `POST` requests. This enables ESP32 devices and simulators to register and send interaction data without maintaining a session.

### 2. VPS-Specific Fixes
If deploying to a VPS (e.g., OVH/Oracle), the following optimizations are applied:
- **Base Image**: Uses `eclipse-temurin:17-jre-alpine` to ensure compatibility with various Linux kernels (Fixes Cgroup NPE).
- **Redis Link**: All Redis communication must use `StringRedisSerializer` to prevent binary corruption on high-latency links.
- **Nginx Headers**: Ensure `X-Forwarded-For` and `X-Real-IP` are correctly passed to handle CORS properly on the VPS IP.

---

**Last Updated**: 2026-03-14
**Version**: 1.1.0
