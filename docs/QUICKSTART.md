# PEPs Application - Monitoring and Management System

**Version:** 1.2 (Mar 2026)

PEPs is a web-based monitoring system for intelligent modules, with real-time statistics, historical data analysis, and audio management.

## Technology Stack
- **Backend:** Java 17, Spring MVC, Apache Tomcat 9+
- **Frontend:** Angular 17+, Angular Material
- **Database:** PostgreSQL 12+
- **Object Storage:** MinIO (S3-compatible)

---

## Quick Start Guide

> [!IMPORTANT]
> [!IMPORTANT]
> **For a complete configuration in Docker, Local, or VPS, please check the [Definitive Deployment Guide](file:///c:/Users/srodr/Desktop/NA_I2/INFOSI/PGROU/PROJET/PEPs/DEPLOYMENT_GUIDE.md).**

### 1. Database Setup
```bash
# 1. Start PostgreSQL
pg_ctl start

# 2. Create tables
psql -U postgres -d postgres -f "sql/00_schema.sql"
psql -U postgres -d postgres -f "sql/01_create_tables.sql"

# 3. Optional test data
psql -U postgres -d postgres -f "sql/99_restore_data.sql"
```

### 2. MinIO Setup (Audio Storage)
1. Start MinIO (recommended through Docker Compose):
```bash
docker compose --env-file .env.dev up -d minio
```
2. Open MinIO Console: `http://localhost:9001`.
3. Sign in using `.env.dev` values:
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
4. Ensure bucket `peps-sounds` exists (or set `MINIO_BUCKET` to another name).
5. Keep bucket policy private. With `mc` CLI:
```bash
mc alias set local http://localhost:9000 <MINIO_ACCESS_KEY> <MINIO_SECRET_KEY>
mc anonymous set private local/peps-sounds
```

### 3. Backend Setup (Java)
1. Navigate to `back/PEPs_back`.
2. Build:
```bash
mvn clean install
```
3. Deploy `target/PEPs_back-0.1.war` to your Tomcat `webapps` folder.
4. Ensure app context path is `/PEPs_back`.
5. Export MinIO env vars for the backend runtime:
- `MINIO_ENDPOINT` (example: `http://localhost:9000`)
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET` (example: `peps-sounds`)

### 4. Frontend Setup (Angular)
```bash
cd front/pepsfront
npm install
npm start
```

Access frontend at `http://localhost:4200`.

**Default Credentials:**
- **Password:** `admin`

---

## API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/dashboard` | Global statistics (interactions, active modules). |
| **GET** | `/interactions` | Full history of module interactions. |
| **GET** | `/modules` | List all modules and their statuses. |
| **PUT** | `/modules/{id}` | Update config (volume, IP, mode). |
| **GET** | `/sounds` | List available audio files. |
| **POST** | `/sounds` | Upload new audio (multipart: mp3, wav, ogg, m4a). |
| **GET** | `/sounds/{id}/file` | Stream audio file from MinIO through backend. |
| **DELETE** | `/sounds/{id}` | Delete audio object and DB record. |

---

## Development Validation

Use this sequence after startup:
1. Upload an audio file from the UI (`Sounds` page).
2. Confirm an object appears in MinIO bucket (`peps-sounds`).
3. Play the uploaded sound in the UI (`GET /sounds/{id}/file`).
4. Delete the sound and verify object deletion in MinIO.

---

## Troubleshooting

- **CORS errors:** Ensure frontend runs on port `4200`.
- **Upload failed:** Check backend MinIO env values and verify MinIO is reachable from backend container (`http://minio:9000` in Docker).
- **403 from MinIO:** Verify access key/secret key match backend environment.
- **404 on sound file:** Confirm the object key stored in DB still exists in the bucket.
- **DB connection issues:** Check `persistence.xml` and database credentials.
