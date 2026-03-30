# Redis + MinIO Audio Pipeline

This document describes the asynchronous audio processing pipeline built with **Redis Streams** (worker queue) and **MinIO** (S3-compatible object storage).

---

## Architecture Overview

```
                        UPLOAD (Pipeline 1)
──────────────────────────────────────────────────────────────────────
Client                POST /api/audio/upload?soundId={id}
                      + multipart file
                               │
                               ▼
                      RedisAudioController
                      (encodes file as Base-64, queues job)
                               │
                  Redis Stream: "upload-audio-stream"
                               │
                               ▼
                      RawAudioWorker           [audio-worker profile]
                      (validates / processes)
                               │
                  Redis Stream: "storage-upload-stream"
                               │
                               ▼
                      StorageWorker            [audio-worker profile]
                      ├── uploads file to MinIO (peps-sounds bucket)
                      └── updates Sound.chemin in PostgreSQL

                        DOWNLOAD (Pipeline 2)
──────────────────────────────────────────────────────────────────────
Client/ESP32          POST /api/audio/download/{soundId}
                               │
                               ▼
                      RedisAudioController
                      (returns 202 + jobId immediately)
                               │
                  Redis Stream: "retrieval-request-stream"
                               │
                               ▼
                      StorageReadWorker        [audio-worker profile]
                      ├── looks up Sound.chemin in PostgreSQL
                      ├── downloads bytes from MinIO
                      └── stores Base-64 result in Redis
                             key: "ready-audio:{jobId}" (TTL 60s)

Client/ESP32          GET /api/audio/download/status/{jobId}
                      ├── 202 PROCESSING → poll again
                      ├── 200 READY      → contains audioBase64
                      └── 500 ERROR      → contains detail message

──────────────────────────────────────────────────────────────────────
                        CACHING LAYER (Nginx)
──────────────────────────────────────────────────────────────────────
To reduce latency and Backend/MinIO load, an Nginx Caching layer is 
implemented at the proxy level:
1. **Proxy Cache**: Frequently accessed sounds are cached for 60m.
2. **Stale Serving**: If the backend is down, Nginx can serve stale cache.
3. **Verification**: Check the `X-Cache-Status` header (HIT/MISS).
```

---

## Services Involved

| Service | Role |
|---|---|
| `peps-backend-audio` | Runs the audio Spring profile (`audio-worker`). Hosts `RedisAudioController` + all workers |
| `peps-redis` | Queue backbone. Streams act as durable job queues with Consumer Groups |
| `peps-minio` | S3-compatible file storage. Audio files stored in bucket `peps-sounds` |
| `peps-database` | PostgreSQL — `Sound.chemin` stores the MinIO object key after upload |

---

## Environment Variables (.env)

```bash
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
MINIO_BUCKET=peps-sounds

REDIS_HOST=peps-redis
REDIS_PORT=6379
REDIS_PASSWORD=peps_redis_pass
```

---

## MinIO Console

Access the MinIO web UI at: **http://localhost:9001**

- **User:** `minioadmin`
- **Password:** `minioadmin123`

The bucket `peps-sounds` is created automatically on first backend startup.

---

## How to Test

### 1. Start the stack

```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

### 2. Login to get a session cookie

```bash
curl -c cookies.txt -s -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"admin"}'
```

### 3. Upload an audio file (requires an existing soundId in the DB)

```bash
curl -b cookies.txt -X POST \
  "http://localhost/api/audio/upload?soundId=1" \
  -F "file=@/path/to/your/audio.mp3"
```

Response:
```json
{ "jobId": "abc-123", "soundId": "1", "message": "Upload job queued successfully." }
```

### 4. Check the audio was saved in MinIO

Visit **http://localhost:9001** → Browse Bucket → `peps-sounds`

Or check the DB:
```bash
docker exec peps-database psql -U test_user -d peps_db_test \
  -c "SELECT idsound, nom, chemin FROM peps.sound WHERE idsound = 1;"
```

### 5. Download the audio

```bash
# Queue the download
curl -b cookies.txt -X POST http://localhost/api/audio/download/1
# → { "jobId": "xyz-456", "message": "Download job queued..." }

# Poll for the result
curl -b cookies.txt http://localhost/api/audio/download/status/xyz-456
# → { "status": "READY", "audioBase64": "..." }
```

---

## Redis Streams Reference

| Stream Key | Producer | Consumer | Purpose |
|---|---|---|---|
| `upload-audio-stream` | `RedisAudioController` | `RawAudioWorker` | Raw file received from client |
| `storage-upload-stream` | `RawAudioWorker` | `StorageWorker` | Validated audio ready to store |
| `retrieval-request-stream` | `RedisAudioController` | `StorageReadWorker` | Download request from client |
| `device-delivery-stream` | `StorageReadWorker` | `DevicePrepWorker` | Raw bytes ready to format for ESP32 |

---

## Worker Profiles

All workers use `@Profile("audio-worker")`. This means they **only activate** on the `backend-audio` container, not on `backend-web`. This is controlled via the `SPRING_PROFILES_ACTIVE` environment variable in `docker-compose.yml`.

To check which replica is running which profile:
```bash
docker exec peps-backend-audio env | grep SPRING
docker exec peps-backend-web env | grep SPRING
```
