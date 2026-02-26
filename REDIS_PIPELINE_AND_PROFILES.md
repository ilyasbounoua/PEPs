# Redis Streams and Dual-Role Architecture

## 1. Overview
The PEPs application utilizes a single Java codebase compiled to a single `.war` file, but deploys two instances (or "replicas") of this codebase with distinct roles natively via Spring Profiles.
1. `backend-web`: Triggers user-facing requests and general API traffic.
2. `backend-audio`: Dedicated worker for background audio processing and serving hardware devices (ESP32).

## 2. Nginx Routing
The `nginx/conf.d/default.conf` configures `limit_req` and routes:
- `/api/audio/*` -> Always routes to `backend_audio:8080`.
- `/api/*` -> Routes strictly to `backend_web:8080`.

## 3. Spring Profiles
Our application handles role-division using Spring's `@Profile` annotation:
- `-Dspring.profiles.active=web`: Ignores the Redis Stream consumers.
- `-Dspring.profiles.active=audio-worker`: Loads `RedisWorkerConfig` and initializes the Redis Streams pipelines.

## 4. Redis Streams Pipeline

We have implemented two fundamental pipelines relying on Redis Streams for resilience and asynchronous capability:

### Pipeline 1: Audio Upload (User/Microphone to MinIO)
1. **Source**: An endpoint handles the file and pushes a dictionary to `upload-audio-stream`.
2. **Worker 1 (`RawAudioWorker`)**: Picks up the task via Consumer Group `audio-processors`, validates/compresses, pushes to `storage-upload-stream`, and sends `XACK`.
3. **Worker 2 (`StorageWorker`)**: Reads `storage-upload-stream` via Consumer Group `storage-handlers`, uploads bytes directly to MinIO, and sends `XACK`.

### Pipeline 2: Audio Retrieval (ESP32 requests from MinIO)
1. **Source**: An ESP32 hits `/api/audio/download/{id}`. The controller pushes a request to `retrieval-request-stream`.
2. **Worker 3 (`StorageReadWorker`)**: Picks up from `storage-readers`, downloads raw audio from MinIO, forwards bytes to `device-delivery-stream`, and sends `XACK`.
3. **Worker 4 (`DevicePrepWorker`)**: Picks up from `device-formatters`, transcodes audio natively for the ESP32 DAC limitations, caches it in a standard Redis Key (e.g. `String` or `Hash`), and replies with an `XACK`. The HTTP request is then fulfilled using this cached blob.
