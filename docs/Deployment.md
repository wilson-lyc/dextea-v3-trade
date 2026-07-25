# Deployment Guide

This document describes the deployment forms, build artifacts, startup methods, health checks, and rollback notes for `dextea-trade`, and explains how the service's **two configuration sources** (environment variables / Nacos) are chosen.

> For the full parameter list, see [Configuration Parameters](Configuration-Parameters.md); for the concrete写法 of each mode, see [Environment Variables](Environment-Variables.md) and [Nacos](Nacos.md).

## 1. Environment & Dependencies

| Dependency | Version / Requirement | Description |
|------|-------------|------|
| JDK | 21 | Required by Spring Boot 3.5 |
| MySQL | 5.7+ / 8.x | Business database |
| Redis | 5+ | Idempotency cache, CosId machine id |
| Nacos | 2.x | **Optional**, only needed when using Nacos config |

## 2. Build

```bash
./mvnw clean package -DskipTests
# Artifact: target/dextea-trade-0.0.1-SNAPSHOT.jar
```

If you also need a Docker image, integrate your team's image build flow at this stage (no Dockerfile is bundled in this repo; a minimal example is in section 4).

## 3. Two Configuration Sources

The service is wired up via `application.yaml` and supports two external configuration sources:

### Option A: Environment Variables (default, recommended for containers)

- No Nacos and no extra config files required.
- All connection info and secrets are injected via environment variables, following the twelve-factor "config separated from code" principle.
- Suitable for Docker / Kubernetes / cloud functions.
- 👉 Writing and list: [Environment Variables](Environment-Variables.md).

### Option B: Nacos Config Center (optional)

- After setting `NACOS_SERVER_ADDR` in the environment, the service attempts to pull `dextea-trade.yaml` (Data ID) from Nacos on startup.
- Suitable for managing config uniformly across multiple instances and environments (dev/test/prod), with dynamic changes.
- Nacos and local/environment variables are **stackable**: Nacos-delivered config takes higher priority; params not delivered fall back to environment variable defaults.
- 👉 Integration: [Nacos](Nacos.md).

> Selection advice: prefer **Option A** for single-instance or containerized deployment; migrate to **Option B** (or combine both) when the number of instances grows and unified management / hot reload is needed.

## 4. Startup Methods

### 4.1 Run the jar directly

```bash
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=dextea \
DB_USERNAME=root DB_PASSWORD=**** \
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 \
ALIPAY_APP_ID=app_id ALIPAY_PRIVATE_KEY="$KEY" ALIPAY_PUBLIC_KEY="$PUB" \
java -jar target/dextea-trade-0.0.1-SNAPSHOT.jar
```

### 4.2 Docker (minimal example)

```dockerfile
FROM eclipse-temurin:21-jre
COPY target/dextea-trade-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

```bash
docker build -t dextea-trade:latest .
docker run -d -p 9090:9090 --env-file .env dextea-trade:latest
```

> The `.env` file holds all environment variables to avoid leaking secrets on the command line. Full variable list: [Environment Variables](Environment-Variables.md).

## 5. Health Check & Readiness

The service includes `spring-boot-starter-actuator`, which can be combined with probes:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- Liveness/readiness probe: `GET /actuator/health`
- When the app has started but a dependency (e.g. MySQL, Redis) is not ready, `health` returns `DOWN`, usable for K8s readiness control.

> Note: if you need to expose actuator endpoints inside a container, deliver the above `management.*` config via environment variables or Nacos, using the same写法 as in the "Spring / Actuator" section of [Configuration Parameters](Configuration-Parameters.md).

## 6. Rollback & Versioning

- Version: `0.0.1-SNAPSHOT`; it is recommended to tag production builds with a fixed version.
- Rollback: keep historical jar / image tags and simply restart the old version; since config is separated from code, no config change is needed for rollback.
- Database: migration scripts are recommended to be managed separately (this repo does not yet include SQL init scripts); before rolling back the app, confirm table schema compatibility.

## 7. Common Deployment Issues

| Symptom | Direction |
|------|------|
| Startup fails on datasource/Redis connection | Check `DB_*` / `REDIS_*` env vars are set and network is reachable |
| Alipay call reports "key error" | Check `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` are not truncated by the shell (always quote with double quotes) |
| Startup hangs on Nacos pull | With no `NACOS_SERVER_ADDR` it should auto-skip via `optional:`; if misconfigured, check address/namespace |
| `machineId` conflict across instances | CosId uses Redis to allocate machine ids; ensure Redis is available and `cosid.namespace` is consistent across instances |

Related parameter definitions: [Configuration Parameters](Configuration-Parameters.md).
