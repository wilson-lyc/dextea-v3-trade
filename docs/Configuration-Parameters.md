# Configuration Parameters

This table summarizes all configurable parameters of `dextea-trade`, categorized by component. Each parameter is marked with its **configuration source** (environment variable / Nacos / both), **default value**, and **required or not**.

> - Naming rules and full list of environment variables: [Environment Variables](Environment-Variables.md).
> - Nacos integration, Data ID, and delivery format: [Nacos](Nacos.md).
> - "Spring relaxed binding" below means: config key `a.b.c` corresponds to env var `A_B_C`; the two are equivalent and either can be used.

## 1. Service Basics

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `server.port` | `SERVER_PORT` | `9090` | No | HTTP port |
| `spring.application.name` | `SPRING_APPLICATION_NAME` | `dextea-trade` | No | App name; also used as Nacos Data ID and CosId namespace |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | `default` | No | Active Profile |

## 2. Data Source (MySQL)

`${ENV}` placeholders are enabled by default, so env vars inject directly; Nacos can deliver the full `spring.datasource.*`.

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `spring.datasource.url` | assembled from `DB_*` | `jdbc:mysql://localhost:3306/dextea?...` | ✅ | JDBC url (assembled below) |
| host (assembly) | `DB_HOST` | `localhost` | ✅ | DB host |
| port (assembly) | `DB_PORT` | `3306` | ✅ | DB port |
| name (assembly) | `DB_NAME` | `dextea` | ✅ | DB name |
| `spring.datasource.username` | `DB_USERNAME` | `root` | ✅ | DB user |
| `spring.datasource.password` | `DB_PASSWORD` | `root` | ✅ | DB password |
| `spring.datasource.driver-class-name` | — | `com.mysql.cj.jdbc.Driver` | No | Driver class |

> Note: timezone in the URL is fixed to `Asia/Shanghai`, encoding `utf8`, `useSSL=false`. To adjust, override `spring.datasource.url` directly in Nacos.

## 3. Redis

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `spring.data.redis.host` | `REDIS_HOST` | `localhost` | ✅ | Redis host |
| `spring.data.redis.port` | `REDIS_PORT` | `6379` | ✅ | Redis port |
| `spring.data.redis.password` | `REDIS_PASSWORD` | (empty) | ⚠️ per instance | Leave empty if no password; required if set |

## 4. Nacos (Config Center, optional)

Enabled after setting `NACOS_SERVER_ADDR`; when not set, it auto-skips via the `optional:` prefix without affecting startup.

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `spring.nacos.config.server-addr` | `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | ❌ | Nacos address |
| `spring.nacos.config.namespace` | `NACOS_NAMESPACE` | (empty) | ❌ | Namespace ID (env isolation) |
| `spring.nacos.config.username` | `NACOS_USERNAME` | (empty) | ❌ | Auth username |
| `spring.nacos.config.password` | `NACOS_PASSWORD` | (empty) | ❌ | Auth password |
| group in `spring.config.import` | `NACOS_CONFIG_GROUP` | `DEFAULT_GROUP` | ❌ | Config group |

## 5. Alipay

`alipay.*` supports Spring relaxed binding, so both Nacos `alipay.app-id` and env var `ALIPAY_APP_ID` work.

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `alipay.server-url` | `ALIPAY_GATEWAY` | `https://openapi.alipay.com` | No | Gateway (change for sandbox) |
| `alipay.app-id` | `ALIPAY_APP_ID` | (empty) | ⚠️ required if using Alipay | Open platform app ID |
| `alipay.private-key` | `ALIPAY_PRIVATE_KEY` | (empty) | ⚠️ required if using Alipay | App private key (multi-line, quote it) |
| `alipay.alipay-public-key` | `ALIPAY_PUBLIC_KEY` | (empty) | ⚠️ required if using Alipay | Alipay public key |
| `alipay.subject` | `ALIPAY_SUBJECT` | `德贤茶庄订单` | No | Order title prefix |
| `alipay.product-code` | `ALIPAY_PRODUCT_CODE` | `JSAPI_PAY` | No | Payment product code |
| `alipay.force-amount` | `ALIPAY_FORCE_AMOUNT` | `0.01` | No | 开发/测试环境强制使用的固定订单金额（元）；非空时创建交易会把总额覆盖为该值，避免真实扣款。生产环境置空以使用真实金额 |
| `alipay.notify-url` | `ALIPAY_NOTIFY_URL` | (empty) | No | 支付宝异步支付回调地址（notify_url）；为空则不设置，非空时创建交易传给支付宝 |

## 6. CosId (Distributed ID)

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `cosid.namespace` | `COSID_NAMESPACE` | `dextea-trade` | No | Namespace |
| `cosid.machine.enabled` | `COSID_MACHINE_ENABLED` | `true` | No | Enable machine id |
| `cosid.machine.distributor.type` | `COSID_MACHINE_DISTRIBUTOR_TYPE` | `redis` | No | Machine id distribution (redis) |
| `cosid.snowflake.enabled` | `COSID_SNOWFLAKE_ENABLED` | `true` | No | Enable Snowflake |
| `cosid.snowflake.provider.order.namespace` | `COSID_SNOWFLAKE_PROVIDER_ORDER_NAMESPACE` | `dextea-trade` | No | Order id generator namespace |

## 7. MyBatis

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `mybatis.mapper-locations` | — | `classpath*:mapper/*.xml` | No | Mapper XML location |
| `mybatis.type-aliases-package` | — | `cn.dextea.trade.entity` | No | Alias package |
| `mybatis.configuration.map-underscore-to-camel-case` | — | `true` | No | Underscore to camel case |

## 8. SpringDoc (API Docs)

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `springdoc.api-docs.path` | — | `/docs/json` | No | OpenAPI JSON path |
| `springdoc.swagger-ui.path` | — | `/docs/ui` | No | Swagger UI path |
| `springdoc.packages-to-scan` | — | `cn.dextea.trade.controller` | No | Scan package |
| `springdoc.paths-to-match` | — | `/api/**` | No | Match path |

## 9. Actuator (Ops Probes)

Only the dependency is included by default; you must deliver `management.endpoints.web.exposure.include` to expose HTTP endpoints (see [Deployment §5](Deployment.md)).

| Config Key | Env Var | Default | Required | Description |
|--------|----------|--------|------|------|
| `management.endpoints.web.exposure.include` | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | (not exposed) | No | Exposed endpoints, e.g. `health,info` |

---

### Required Quick Reference

- **Database**: `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- **Redis**: `REDIS_HOST` `REDIS_PORT` (and `REDIS_PASSWORD` if set)
- **Alipay (when using payment)**: `ALIPAY_APP_ID` `ALIPAY_PRIVATE_KEY` `ALIPAY_PUBLIC_KEY`
- **Nacos**: all optional; skipped if not configured

Next: 👉 [Environment Variables](Environment-Variables.md) or 👉 [Nacos](Nacos.md)
