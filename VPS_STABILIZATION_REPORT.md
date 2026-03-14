# VPS Stabilization & Deployment Report - PEPs Project

This report summarizes the critical fixes and optimizations implemented to stabilize the PEPs application on the production VPS environment.

## 1. Authentication & Security Fixes
We resolved the initial lockout issue where the `admin` account was inaccessible due to password hash mismatches.
- **BCrypt Migration**: Generated a verified BCrypt hash and manually synchronized the database.
- **Security WhiteList**: Updated `JwtFilter.java` to allow public access to `/auth/login`.
- **IoT Public Access**: Configured `/api/modules` and `/api/interactions` as public `POST` endpoints to allow IoT devices (ESP32) and simulators to register and send data without requiring JWT cookies.

## 2. Asynchronous Audio Pipeline
The audio upload system was stabilized to prevent data corruption and network decoding errors.
- **Redis Serialization**: Enforced `StringRedisSerializer` across the entire pipeline (`RedisConfig.java` and `RedisWorkerConfig.java`). This ensures all data in Redis is stored as plain text, eliminating the `io.lettuce.core.protocol.RedisCommandTimeoutException` and binary decoding errors.
- **Format Support**: Enhanced `RawAudioWorker.java` to support a wider range of formats beyond MP3/WAV:
  - **OGG**: Detected via `OggS` magic bytes.
  - **M4A/MP4**: Detected via `ftyp` signature.
- **Enhanced Traceability**: Added `soundName` to the Redis payload, allowing workers to log specifically which sound is being processed (visible via `docker logs`).

## 3. IoT & Device Integration
- **CORS Configuration**: Updated `SoundController`, `ModuleController`, and `InteractionController` to include the VPS IP (`http://51.75.126.85`) in the allowed origins.
- **Proxy Routing**: Verified that the Nginx proxy correctly routes requests via the `/api/` prefix to the backend container.

## 4. VPS Infrastructure Optimizations
Specific adjustments were made to ensure the application runs reliably on the VPS kernel and environment.
- **JDK Implementation**: Switched the base Docker image to **Eclipse Temurin (OpenJDK 17)** to resolve a `NullPointerException` in `CgroupSubsystemFactory` caused by the VPS's kernel configuration.
- **Tomcat Performance**: Optimized startup by disabling unnecessary TLD scanning (`-Dorg.apache.catalina.startup.TldConfig.jarsToSkip=*.jar`).
- **Container Health**: Stabilized Docker healthchecks by switching to `OPTIONS` requests, preventing monitoring failures from being flagged as unauthenticated errors.

## 5. Maintenance & Logging
- **Standardized Logging**: Replaced all `System.out.println` and `e.printStackTrace()` with **SLF4J/Logback** logging. This allows for professionally structured logs that can be easily monitored and rotated.
- **Code Integrity**: Corrected Generic type erasure issues in `RedisWorkerConfig` that were causing build failures in the production environment.

---
**Status**: Fully Operational
**Environment**: Production VPS (`51.75.126.85`)
**Build Date**: March 2026
