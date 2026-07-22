# Nacos Configuration

`dextea-trade` supports **Nacos as a config center** via `spring-alibaba-nacos-config`. After setting `NACOS_SERVER_ADDR`, the service pulls app config from Nacos on startup; when not set, it silently skips via the `optional:` prefix, **without affecting local/env-var startup**.

> For env-var writing, see [Environment Variables](Environment-Variables.md); for parameter ownership and defaults, see [Configuration Parameters](Configuration-Parameters.md).

## 1. Enable

Just set the Nacos address in the environment (or startup args) to enable:

```bash
export NACOS_SERVER_ADDR=127.0.0.1:8848
export NACOS_NAMESPACE=      # namespace ID, empty = public
export NACOS_USERNAME=       # fill when auth is enabled
export NACOS_PASSWORD=
export NACOS_CONFIG_GROUP=DEFAULT_GROUP   # config group
```

`application.yaml` already includes the import statement:

```yaml
spring:
  config:
    import:
      - optional:nacos:${spring.application.name}.yaml?group=${NACOS_CONFIG_GROUP:DEFAULT_GROUP}
```

- The `optional:` prefix ensures **no error when Nacos is not configured**.
- The target Data ID defaults to the app name: `dextea-trade.yaml`.

## 2. Data ID & Format

| Item | Value | Description |
|----|------|------|
| Data ID | `dextea-trade.yaml` | from `spring.application.name` + `.yaml` |
| Group | `DEFAULT_GROUP` (overridable via `NACOS_CONFIG_GROUP`) | Config group |
| Namespace | `NACOS_NAMESPACE` (empty = public) | Env isolation |
| Format | YAML | Same structure as local `application.yaml` |

> For multiple environments (dev/test/prod), it is recommended to use **Namespace** for isolation rather than multiple Data IDs, to avoid app-name conflicts.

## 3. Example Nacos Config

Create `dextea-trade.yaml` in the Nacos console; example content (overriding DB, Redis, Alipay, etc.):

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql.prod.svc:3306/dextea?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: dextea
    password: ${DB_PASSWORD}   # sensitive items can still be injected via env var, no plaintext in Nacos
  data:
    redis:
      host: redis.prod.svc
      port: 6379
      password: ${REDIS_PASSWORD}

alipay:
  app-id: 2021xxxxxxxx
  private-key: ${ALIPAY_PRIVATE_KEY}
  alipay-public-key: ${ALIPAY_ALIPAY_PUBLIC_KEY}
  subject: 德贤茶庄订单

cosid:
  namespace: dextea-trade

mybatis:
  configuration:
    map-underscore-to-camel-case: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Key points:

- Nacos config has the **same structure** as local `application.yaml` and can be migrated directly.
- **Sensitive info (passwords, private keys) should remain as `${ENV}` placeholders**, injected via env vars or K8s Secret, so Nacos does not store plaintext.
- Any param not delivered by Nacos falls back to local defaults or env vars.

## 4. Priority vs Environment

Config priority (high → low):

```
Startup command-line args
  > Nacos config center (remote)
    > Environment variables / system properties
      > Local application.yaml defaults
```

That is: **Nacos-delivered config overrides same-named env vars and local defaults**; params not delivered by Nacos still fall back to env vars. This "remote-primary, env-fallback" combo is great for canary and emergency hotfixes (change Nacos without re-releasing).

## 5. Debug & Troubleshoot

| Symptom | Check |
|------|------|
| No Nacos pull in startup log | Confirm `NACOS_SERVER_ADDR` is set; `optional:` silently skips when missing |
| Pull reports 403 / auth failure | Check `NACOS_USERNAME` / `NACOS_PASSWORD` and whether Nacos auth is enabled |
| Pull reports namespace not found | Confirm `NACOS_NAMESPACE` is the namespace **ID** (not the name) |
| Config not effective | Verify Data ID is `dextea-trade.yaml` and Group matches `NACOS_CONFIG_GROUP` |
| Want hot reload | Nacos changes support dynamic refresh by default; whether the app reacts in real time depends on `@RefreshScope` / `@ConfigurationProperties` usage in code |

## 6. Enable Auth (Production Recommended)

Nacos 2.x is recommended to enable auth on the server side; the client connects via `NACOS_USERNAME` / `NACOS_PASSWORD` (see section 1). Combine with TLS and network isolation to avoid config leakage.

---

Back to: [Deployment Guide](Deployment.md) ｜ Compare: [Environment Variables](Environment-Variables.md)
