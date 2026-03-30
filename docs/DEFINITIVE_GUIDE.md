# PEPs — Definitive Deployment Guide

**Version:** 2.0 (Mar 2026) | Java 17 · Spring MVC · Tomcat 9 · Angular 17 · PostgreSQL 15 · Redis 7 · MinIO · Nginx

> [!NOTE]
> This guide replaces `QUICKSTART.md`, `DEPLOYMENT_GUIDE.md`, and `DOCKER_DEPLOYMENT.md`.

---

## Environment Configuration (`.env`)

Edit the `.env` file in the project root before any deployment. This is the single source of truth.

```bash
# Proxy
PROXY_PORT=80
PROXY_SSL_PORT=443

# Database — use 'database' in Docker, 'localhost' for native dev
DB_HOST=database
DB_PORT=5432
DB_NAME=peps_db
DB_USER=peps_admin
DB_PASSWORD=your_password

# Auto-created admin user (via AdminInitializer on startup)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_admin_password

# Redis
REDIS_HOST=peps-redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# MinIO
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
MINIO_BUCKET=peps-sounds

# Frontend
API_URL=http://localhost/api
```

> [!CAUTION]
> Never commit `.env` to version control. It is in `.gitignore`.

---

## Scenario A — Native Local Development (Windows)

For fast backend debugging without building Docker images.

### Requirements
- Java 17+ with `JAVA_HOME` set
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL 15 running locally

### Step 1 — Set Windows environment variables (one-time setup)

The Spring backend reads credentials from Windows environment variables, **not** from `.env`. Run this once in PowerShell:

```powershell
[Environment]::SetEnvironmentVariable('DB_HOST', 'localhost', 'User')
[Environment]::SetEnvironmentVariable('DB_PORT', '5432', 'User')
[Environment]::SetEnvironmentVariable('DB_NAME', 'peps_db', 'User')
[Environment]::SetEnvironmentVariable('DB_USER', 'peps_admin', 'User')
[Environment]::SetEnvironmentVariable('DB_PASSWORD', 'your_password', 'User')
[Environment]::SetEnvironmentVariable('REDIS_HOST', 'localhost', 'User')
[Environment]::SetEnvironmentVariable('REDIS_PORT', '6379', 'User')
[Environment]::SetEnvironmentVariable('REDIS_PASSWORD', 'your_redis_password', 'User')
[Environment]::SetEnvironmentVariable('MINIO_ENDPOINT', 'http://localhost:9000', 'User')
[Environment]::SetEnvironmentVariable('MINIO_ACCESS_KEY', 'minioadmin', 'User')
[Environment]::SetEnvironmentVariable('MINIO_SECRET_KEY', 'minioadmin123', 'User')
[Environment]::SetEnvironmentVariable('MINIO_BUCKET', 'peps-sounds', 'User')
[Environment]::SetEnvironmentVariable('ADMIN_USERNAME', 'admin', 'User')
[Environment]::SetEnvironmentVariable('ADMIN_PASSWORD', 'admin', 'User')
```

> [!IMPORTANT]
> Restart your terminal after setting these variables.

### Step 2 — Start infrastructure (Redis + MinIO only)

```powershell
docker compose up -d redis minio
```

### Step 3 — Run the backend

```powershell
cd back/PEPs_back
mvn tomcat7:run-war -DskipTests
```

### Step 4 — Run the frontend

```powershell
cd front/pepsfront
npm install && npm start
```

Access the app at `http://localhost:4200`.

---

## Scenario B — Docker on Windows

> [!WARNING]
> **Known Windows issue:** Docker Compose gives priority to Windows system-level environment variables over `.env`. If you have `DB_HOST=localhost` set system-wide (needed for native dev), it gets passed to the container instead of `database`.
>
> **Fix:** use `docker-compose.dev.yml` as an override. It explicitly sets the correct Docker container names.

### Compose files

| File | Purpose |
|---|---|
| `docker-compose.yml` | Base config — used by VPS and CI/CD as-is |
| `docker-compose.dev.yml` | Windows override — fixes `DB_HOST` and `REDIS_HOST` |
| `docker-compose.prod.yml` | Production resource limits |

### Commands

```powershell
# Start (always use both files on Windows)
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d

# Stop (keep data)
docker compose -f docker-compose.yml -f docker-compose.dev.yml stop

# Stop and remove containers
docker compose -f docker-compose.yml -f docker-compose.dev.yml down

# Full reset — WARNING: deletes the database
docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v
```

### Access

| Service | URL |
|---|---|
| Application | `http://localhost` |
| Backend API | `http://localhost:8080` |
| MinIO Console | `http://localhost:9001` |

---

## Scenario C — VPS / Production

### Requirements
- Docker Engine 20.10+
- Docker Compose 2.0+
- Ports 80 and 443 open in the firewall

### Deployment

```bash
# 1. Clone the repository
git clone <repo-url> && cd PEPs

# 2. Configure environment with strong passwords
nano .env
# Ensure DB_HOST=database and REDIS_HOST=peps-redis

# 3. Deploy with production resource limits
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d

# 4. Verify all containers are healthy
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### Update in production

```bash
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

---

## Admin Initialization

The admin user is **not seeded in SQL**. It is created automatically on startup by `AdminInitializer`:

- If `ADMIN_USERNAME` does **not** exist → user is created with `ADMIN_PASSWORD`
- If it exists but the password changed in `.env` → hash is updated automatically
- If it exists and matches → no action (logged as info)

To change the admin password: update `ADMIN_PASSWORD` in `.env` and restart the backend.

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---|---|---|
| Backend `unhealthy` | DB not ready when backend started | `down && up -d` again; the `pg_isready` healthcheck ensures ordering |
| `DB_HOST=localhost` in container | Windows system env overrides `.env` | Use `-f docker-compose.dev.yml` |
| 404 on `/api/...` | Spring context failed to initialize | Check `docker logs peps-backend-web` for the exact JPA error |
| Login "incorrect" | Admin user was not created | Spring context must load without errors first |
| Port 8080 in use | Maven local server is running | Stop `mvn tomcat7:run-war` before starting Docker |

### Useful commands

```bash
# Inspect logs
docker logs peps-backend-web --tail 100

# Verify env vars inside container
docker exec peps-backend-web env | grep DB_HOST
# Must show: DB_HOST=database

# Clean everything and rebuild from scratch
docker compose down -v && docker compose ... up --build -d
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/login` | Authenticate (returns HttpOnly JWT cookie) |
| `GET` | `/auth/me` | Verify active session |
| `GET` | `/dashboard` | Global statistics |
| `GET` | `/interactions` | Full interaction history |
| `GET` | `/modules` | List all modules |
| `PUT` | `/modules/{id}` | Update module config |
| `GET` | `/sounds` | List audio files |
| `POST` | `/sounds` | Upload audio (mp3, wav, ogg) |
| `DELETE` | `/sounds/{id}` | Delete audio file |

---

*Last updated: March 2026 · Version 2.0*
