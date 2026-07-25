# 环境变量

本服务**默认支持纯环境变量注入**——无需任何外部配置文件即可启动，非常适合容器化和 PaaS 部署。本文档说明命名规则、完整列表以及最小可运行示例。

> 关于参数归属与默认值，请先查阅 [配置参数](Configuration-Parameters.md)。关于集中式配置管理，请前往 [Nacos](Nacos.md)。
> 关于启动前的检查清单（依赖、数据库表结构、`.env` 创建），请查阅 [前置条件](Prerequisites.md)。

### ⚠️ 安全注意事项（开始前必读）

- **切勿提交 `.env`。** 它包含真实的数据库密码 / 支付宝私钥。仓库的 `.gitignore` 已忽略 `*.env`；请确保本地 `.env` 没有被强制加入（`git add -f`）。
- **以 `.env.example` 作为模板。** 将其复制为 `.env` 并填入你自己的值——**不要**复用仓库中已提交的 `.env` 里的取值（它们来自共享/开发环境，应当轮换）。
- **多行密钥需加引号。** `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` 为多行内容；请将整个值用**双引号**包裹（见 §4）。
- **生产环境必须清空 `ALIPAY_FORCE_AMOUNT`。** 其默认值 `0.01` 会覆盖真实订单金额，以避免开发环境产生真实扣款；若在生产环境仍保留该值，将只扣 ¥0.01 而非真实金额。

## 1. 命名规则（Spring 宽松绑定）

Spring Boot 的“宽松绑定（Relaxed Binding）”允许你把配置键 `a.b.c` 写成环境变量 `A_B_C`：

- 配置键：`spring.datasource.username` → 环境变量：`SPRING_DATASOURCE_USERNAME`
- 配置键：`cosid.snowflake.enabled` → 环境变量：`COSID_SNOWFLAKE_ENABLED`
- 配置键：`management.endpoints.web.exposure.include` → 环境变量：`MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`

要点：

1. 小写点号 `.` 会转换为大写并使用下划线 `_` 作为分隔符。
2. 短横线 `-` 也映射为下划线（例如 `alipay.app-id` → `ALIPAY_APP_ID`）。
3. 环境变量名**全部大写**。

本项目已为部分高频参数预设了 `${ENV}` 占位符（见下方“内置映射”）；其余参数遵循上述统一规则。

## 2. 内置 `${ENV}` 映射

这些参数在 `application.yaml` 中已直接引用环境变量，使用起来更直观：

| 用途 | 环境变量 | 对应配置键 |
|------|----------|------------|
| 数据库主机 | `DB_HOST` | 拼接进 `spring.datasource.url` |
| 数据库端口 | `DB_PORT` | 拼接进 `spring.datasource.url` |
| 数据库名 | `DB_NAME` | 拼接进 `spring.datasource.url` |
| 数据库用户 | `DB_USERNAME` | `spring.datasource.username` |
| 数据库密码 | `DB_PASSWORD` | `spring.datasource.password` |
| Redis 主机 | `REDIS_HOST` | `spring.data.redis.host` |
| Redis 端口 | `REDIS_PORT` | `spring.data.redis.port` |
| Redis 密码 | `REDIS_PASSWORD` | `spring.data.redis.password` |
| Nacos 地址 | `NACOS_SERVER_ADDR` | `spring.nacos.config.server-addr` |
| Nacos 命名空间 | `NACOS_NAMESPACE` | `spring.nacos.config.namespace` |
| Nacos 用户名 | `NACOS_USERNAME` | `spring.nacos.config.username` |
| Nacos 密码 | `NACOS_PASSWORD` | `spring.nacos.config.password` |
| Nacos 分组 | `NACOS_CONFIG_GROUP` | `spring.config.import` 中的 group |

## 3. 完整环境变量列表

| 环境变量 | 默认值 | 是否必填 | 说明 |
|----------|--------|------|------|
| `SERVER_PORT` | `9090` | 否 | HTTP 端口 |
| `SPRING_PROFILES_ACTIVE` | `default` | 否 | 激活的 Profile |
| `DB_HOST` | `localhost` | ✅ | MySQL 主机 |
| `DB_PORT` | `3306` | ✅ | MySQL 端口 |
| `DB_NAME` | `dextea` | ✅ | 数据库名 |
| `DB_USERNAME` | `root` | ✅ | 数据库用户 |
| `DB_PASSWORD` | `root` | ✅ | 数据库密码 |
| `REDIS_HOST` | `localhost` | ✅ | Redis 主机 |
| `REDIS_PORT` | `6379` | ✅ | Redis 端口 |
| `REDIS_PASSWORD` | （空） | ⚠️ 视实例而定 | Redis 密码 |
| `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | ❌ | Nacos 地址（设置后启用） |
| `NACOS_NAMESPACE` | （空） | ❌ | 命名空间 |
| `NACOS_USERNAME` | （空） | ❌ | 用户名 |
| `NACOS_PASSWORD` | （空） | ❌ | 密码 |
| `NACOS_CONFIG_GROUP` | `DEFAULT_GROUP` | ❌ | 配置分组 |
| `ALIPAY_OPENAPI_GATEWAY` | `https://openapi.alipay.com` | 否 | 网关 |
| `ALIPAY_APP_ID` | （空） | ⚠️ 使用支付宝时必填 | 应用 ID |
| `ALIPAY_PRIVATE_KEY` | （空） | ⚠️ 使用支付宝时必填 | 应用私钥（多行） |
| `ALIPAY_PUBLIC_KEY` | （空） | ⚠️ 使用支付宝时必填 | 支付宝公钥 |
| `ALIPAY_SUBJECT` | `德贤茶庄订单` | 否 | 订单标题前缀 |
| `ALIPAY_PRODUCT_CODE` | `JSAPI_PAY` | 否 | 支付产品码（如 JSAPI_PAY） |
| `ALIPAY_FORCE_AMOUNT` | `0.01` | 否 | 开发环境强制固定订单金额（元）；非空时覆盖真实金额，避免真实扣款。生产环境置空 |
| `ALIPAY_NOTIFY_URL` | （空） | ❌ | 支付宝异步支付回调地址（notify_url）；为空则不设置，非空时创建交易传给支付宝 |
| `COSID_NAMESPACE` | `dextea-trade` | 否 | CosId 命名空间 |
| `COSID_MACHINE_ENABLED` | `true` | 否 | 是否启用机器 ID |
| `COSID_MACHINE_DISTRIBUTOR_TYPE` | `redis` | 否 | 机器 ID 分配方式 |
| `COSID_SNOWFLAKE_ENABLED` | `true` | 否 | 是否启用 Snowflake |
| `COSID_SNOWFLAKE_PROVIDER_ORDER_NAMESPACE` | `dextea-trade` | 否 | 订单 ID 命名空间 |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | （不暴露） | 否 | 暴露的 Actuator 端点 |

> [配置参数](Configuration-Parameters.md) 中列出但上文未出现的其他参数（如 MyBatis、SpringDoc）也可按第 1 节的规则，使用 `SPRING_DATASOURCE_*` / `MYBATIS_*` / `SPRINGDOC_*` 的大写形式注入。

## 4. 最小可运行示例

### 4.0 基于模板创建你的 `.env`（推荐）

仓库附带了 `.env.example`，列出了**所有**支持的变量及安全的默认值。将其复制后填入你的真实取值：

```bash
cp .env.example .env
# 然后编辑 .env —— 按需设置 DB_*、REDIS_* 以及 ALIPAY_*
```

从文件注入所有变量后运行 jar（命令行上不出现任何密钥）：

```bash
java -jar target/dextea-trade-0.0.1-SNAPSHOT.jar --env-file=.env
```

> `--env-file` 是 Spring Boot 3.x 的原生参数；旧的 `SPRING_APPLICATION_JSON` / shell `export` 方式同样可用。

### 4.1 手动导出（备选）

启动仅需数据库和 Redis（不使用支付宝时可省略相关变量）：

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=dextea
export DB_USERNAME=root
export DB_PASSWORD=secret

export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
# REDIS_PASSWORD=   # 无密码时留空

java -jar target/dextea-trade-0.0.1-SNAPSHOT.jar
```

### 4.2 启用支付宝支付

需要支付宝时设置以下三项（其余均有安全默认值）。如果你采用 §4.0 的 `.env` 方式，只需将它们加入你的 `.env` 文件。

```bash
export ALIPAY_APP_ID=2021xxxxxxxx
export ALIPAY_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----
MIIE...
-----END PRIVATE KEY-----"
export ALIPAY_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----
MIIB...
-----END PUBLIC KEY-----"
```

> ⚠️ 私钥/公钥通常为**多行内容**。请始终将整个值用**双引号**包裹；通过 `--env-file` 或编排工具（Docker Compose / K8s Secret）注入可避免 shell 转义问题。
>
> ⚠️ 切记**在生产环境清空 `ALIPAY_FORCE_AMOUNT`**（见顶部安全注意事项），否则每笔订单都只会扣 ¥0.01。

### docker-compose 片段

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
      ALIPAY_PUBLIC_KEY: ${ALIPAY_PUBLIC_KEY}
```

## 5. 验证配置是否生效

启动后，访问：

- API 文档：`http://<host>:9090/docs/ui`
- 健康检查：`http://<host>:9090/actuator/health`（需先暴露 `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`）

若连接失败，请先在 [配置参数](Configuration-Parameters.md) 中核对对应参数的“是否必填”标记与环境变量拼写。

---

返回：[部署指南](Deployment.md) ｜ 切换：[Nacos](Nacos.md)
