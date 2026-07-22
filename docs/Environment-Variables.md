# Environment Variables

This service **supports pure environment-variable injection by default** — it can start without any external config file, which is ideal for containerized and PaaS deployments. This document explains the naming rules, full list, and minimal runnable example.

> For parameter ownership and defaults, first see [Configuration Parameters](Configuration-Parameters.md). For centralized config management, go to [Nacos](Nacos.md).

## 1. Naming Rules (Spring Relaxed Binding)

Spring Boot's "Relaxed Binding" lets you write config key `a.b.c` as env var `A_B_C`:

- Config key: `spring.datasource.username` → env var: `SPRING_DATASOURCE_USERNAME`
- Config key: `alipay.app-id` → env var: `ALIPAY_APP_ID`
- Config key: `cosid.snowflake.enabled` → env var: `COSID_SNOWFLAKE_ENABLED`

Key points:

1. Lowercase dot `.` becomes uppercase with underscore `_` separator.
2. Dash `-` also maps to underscore (e.g. `alipay.app-id` → `ALIPAY_APP_ID`).
3. Env var names are **all uppercase**.

This project has pre-set `${ENV}` placeholders for some high-frequency params (see "Built-in mappings" below); the rest follow the unified rule above.

## 2. Built-in `${ENV}` Mappings

These params already reference env vars directly in `application.yaml`, making them more intuitive to use:

| Purpose | Env Var | Corresponding Config Key |
|------|----------|------------|
| DB host | `DB_HOST` | assembled into `spring.datasource.url` |
| DB port | `DB_PORT` | assembled into `spring.datasource.url` |
| DB name | `DB_NAME` | assembled into `spring.datasource.url` |
| DB user | `DB_USERNAME` | `spring.datasource.username` |
| DB password | `DB_PASSWORD` | `spring.datasource.password` |
| Redis host | `REDIS_HOST` | `spring.data.redis.host` |
| Redis port | `REDIS_PORT` | `spring.data.redis.port` |
| Redis password | `REDIS_PASSWORD` | `spring.data.redis.password` |
| Nacos address | `NACOS_SERVER_ADDR` | `spring.nacos.config.server-addr` |
| Nacos namespace | `NACOS_NAMESPACE` | `spring.nacos.config.namespace` |
| Nacos username | `NACOS_USERNAME` | `spring.nacos.config.username` |
| Nacos password | `NACOS_PASSWORD` | `spring.nacos.config.password` |
| Nacos group | `NACOS_CONFIG_GROUP` | group in `spring.config.import` |

## 3. Full Environment Variable List

| Env Var | Default | Required | Description |
|----------|--------|------|------|
| `SERVER_PORT` | `9090` | No | HTTP port |
| `SPRING_PROFILES_ACTIVE` | `default` | No | Active Profile |
| `DB_HOST` | `localhost` | ✅ | MySQL host |
| `DB_PORT` | `3306` | ✅ | MySQL port |
| `DB_NAME` | `dextea` | ✅ | DB name |
| `DB_USERNAME` | `root` | ✅ | DB user |
| `DB_PASSWORD` | `root` | ✅ | DB password |
| `REDIS_HOST` | `localhost` | ✅ | Redis host |
| `REDIS_PORT` | `6379` | ✅ | Redis port |
| `REDIS_PASSWORD` | (empty) | ⚠️ per instance | Redis password |
| `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | ❌ | Nacos address (enabled when set) |
| `NACOS_NAMESPACE` | (empty) | ❌ | Namespace |
| `NACOS_USERNAME` | (empty) | ❌ | Username |
| `NACOS_PASSWORD` | (empty) | ❌ | Password |
| `NACOS_CONFIG_GROUP` | `DEFAULT_GROUP` | ❌ | Config group |
| `ALIPAY_SERVER_URL` | `https://openapi.alipay.com` | No | Gateway |
| `ALIPAY_APP_ID` | (empty) | ⚠️ required if using Alipay | App ID |
| `ALIPAY_PRIVATE_KEY` | (empty) | ⚠️ required if using Alipay | App private key (multi-line) |
| `ALIPAY_ALIPAY_PUBLIC_KEY` | (empty) | ⚠️ required if using Alipay | Alipay public key |
| `ALIPAY_SUBJECT` | `德贤茶庄订单` | No | Order title prefix |
| `COSID_NAMESPACE` | `dextea-trade` | No | CosId namespace |
| `COSID_MACHINE_ENABLED` | `true` | No | Machine id enabled |
| `COSID_MACHINE_DISTRIBUTOR_TYPE` | `redis` | No | Machine id distribution |
| `COSID_SNOWFLAKE_ENABLED` | `true` | No | Snowflake enabled |
| `COSID_SNOWFLAKE_PROVIDER_ORDER_NAMESPACE` | `dextea-trade` | No | Order id namespace |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | (not exposed) | No | Expose actuator endpoints |

> Other params listed in [Configuration Parameters](Configuration-Parameters.md) but not shown above (e.g. MyBatis, SpringDoc) can also be injected using the `SPRING_DATASOURCE_*` / `MYBATIS_*` / `SPRINGDOC_*` uppercase form per section 1.

## 4. Minimal Runnable Example

Only DB and Redis are needed to start (Alipay vars can be omitted when not used):

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=dextea
export DB_USERNAME=root
export DB_PASSWORD=secret

export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
# REDIS_PASSWORD=   # leave empty if none

java -jar target/dextea-trade-0.0.1-SNAPSHOT.jar
```

### Enable Alipay Payment

```bash
export ALIPAY_APP_ID=2021xxxxxxxx
export ALIPAY_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----
MIIE...
-----END PRIVATE KEY-----"
export ALIPAY_ALIPAY_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----
MIIB...
-----END PUBLIC KEY-----"
```

> ⚠️ Private/public keys are typically **multi-line**. Always wrap the whole value in **double quotes**; injecting via `--env-file` or an orchestration tool (Docker Compose / K8s Secret) avoids shell escaping issues.

### docker-compose snippet

```yaml
services:
  trade:
    image: dextea-trade:latest
    ports:
      - "9090:9090"
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: dextea
      DB_USERNAME: root
      DB_PASSWORD: ${DB_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      ALIPAY_APP_ID: ${ALIPAY_APP_ID}
      ALIPAY_PRIVATE_KEY: ${ALIPAY_PRIVATE_KEY}
      ALIPAY_ALIPAY_PUBLIC_KEY: ${ALIPAY_ALIPAY_PUBLIC_KEY}
```

## 5. Verify Config Takes Effect

After startup, visit:

- API docs: `http://<host>:9090/docs/ui`
- Health check: `http://<host>:9090/actuator/health` (requires exposing `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` first)

If connection fails, first check the "Required" marker and env var spelling for the corresponding param in [Configuration Parameters](Configuration-Parameters.md).

---

Back to: [Deployment Guide](Deployment.md) ｜ Switch: [Nacos](Nacos.md)
