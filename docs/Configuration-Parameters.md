# 配置参数

本表按组件分类汇总了 `dextea-trade` 的全部可配置参数。每个参数都标注了其**配置来源**（环境变量 / Nacos / 两者）、**默认值**以及**是否必填**。

> - 环境变量的命名规则与完整列表：[环境变量](Environment-Variables.md)。
> - Nacos 集成、Data ID 与下发格式：[Nacos](Nacos.md)。
> - 下文“Spring 宽松绑定”指：配置键 `a.b.c` 对应环境变量 `A_B_C`；二者等价，可任选其一使用。

## 1. 服务基础

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `server.port` | `SERVER_PORT` | `9090` | 否 | HTTP 端口 |
| `spring.application.name` | `SPRING_APPLICATION_NAME` | `dextea-trade` | 否 | 应用名；同时用作 Nacos Data ID 与 CosId 命名空间 |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | `default` | 否 | 激活的 Profile |

## 2. 数据源（MySQL）

默认已启用 `${ENV}` 占位符，因此环境变量可直接注入；Nacos 可下发完整的 `spring.datasource.*`。

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `spring.datasource.url` | 由 `DB_*` 拼接 | `jdbc:mysql://localhost:3306/dextea?...` | ✅ | JDBC 连接地址（拼接规则见下） |
| host（拼接项） | `DB_HOST` | `localhost` | ✅ | 数据库主机 |
| port（拼接项） | `DB_PORT` | `3306` | ✅ | 数据库端口 |
| name（拼接项） | `DB_NAME` | `dextea` | ✅ | 数据库名 |
| `spring.datasource.username` | `DB_USERNAME` | `root` | ✅ | 数据库用户 |
| `spring.datasource.password` | `DB_PASSWORD` | `root` | ✅ | 数据库密码 |
| `spring.datasource.driver-class-name` | — | `com.mysql.cj.jdbc.Driver` | 否 | 驱动类 |

> 注意：连接地址中的时区固定为 `Asia/Shanghai`，编码 `utf8`，`useSSL=false`。如需调整，直接在 Nacos 中覆盖 `spring.datasource.url`。

## 3. Redis

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `spring.data.redis.host` | `REDIS_HOST` | `localhost` | ✅ | Redis 主机 |
| `spring.data.redis.port` | `REDIS_PORT` | `6379` | ✅ | Redis 端口 |
| `spring.data.redis.password` | `REDIS_PASSWORD` | （空） | ⚠️ 视实例而定 | 无密码时留空；设置后则必填 |

## 4. Nacos（配置中心，可选）

设置 `NACOS_SERVER_ADDR` 后启用；未设置时经 `optional:` 前缀自动跳过，不影响启动。

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `spring.nacos.config.server-addr` | `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | ❌ | Nacos 地址 |
| `spring.nacos.config.namespace` | `NACOS_NAMESPACE` | （空） | ❌ | 命名空间 ID（环境隔离） |
| `spring.nacos.config.username` | `NACOS_USERNAME` | （空） | ❌ | 鉴权用户名 |
| `spring.nacos.config.password` | `NACOS_PASSWORD` | （空） | ❌ | 鉴权密码 |
| `spring.config.import` 中的 group | `NACOS_CONFIG_GROUP` | `DEFAULT_GROUP` | ❌ | 配置分组 |

## 5. 支付宝

支付宝配置**统一收敛在 `AlipaySdkConfig` 一个类中**，每个配置项对应一个环境变量，全部通过 `System.getenv(...)` 读取（默认值在类中定义），不再依赖 `application.yaml` 或 Spring 宽松绑定。因此配置项**只有环境变量名，没有 `alipay.*` 这样的 Spring 配置键**。

> 后续接入 Nacos 等统一配置中心时，只需在 Nacos 中将配置以环境变量形式下发（或在 `AlipaySdkConfig` 扩展读取来源），无需改动 `application.yaml`。

| 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|--------|------|------|
| `ALIPAY_OPENAPI_GATEWAY` | `https://openapi.alipay.com` | 否 | 支付宝网关地址（沙箱环境需替换） |
| `ALIPAY_APP_ID` | （空） | ⚠️ 使用支付宝时必填 | 开放平台应用 AppId |
| `ALIPAY_PRIVATE_KEY` | （空） | ⚠️ 使用支付宝时必填 | 应用私钥（多行内容需用双引号包裹） |
| `ALIPAY_PUBLIC_KEY` | （空） | ⚠️ 使用支付宝时必填 | 支付宝公钥 |
| `ALIPAY_SUBJECT` | `德贤茶庄订单` | 否 | 订单标题前缀 |
| `ALIPAY_PRODUCT_CODE` | `JSAPI_PAY` | 否 | 支付产品码 |
| `ALIPAY_FORCE_AMOUNT` | `0.01` | 否 | 开发/测试环境强制使用的固定订单金额（元）；非空时创建交易会把总额覆盖为该值，避免真实扣款。生产环境置空以使用真实金额 |
| `ALIPAY_NOTIFY_URL` | （空） | 否 | 支付宝异步支付回调地址（notify_url）；为空则不设置，非空时创建交易传给支付宝 |

## 6. CosId（分布式 ID）

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `cosid.namespace` | `COSID_NAMESPACE` | `dextea-trade` | 否 | 命名空间 |
| `cosid.machine.enabled` | `COSID_MACHINE_ENABLED` | `true` | 否 | 是否启用机器 ID |
| `cosid.machine.distributor.type` | `COSID_MACHINE_DISTRIBUTOR_TYPE` | `redis` | 否 | 机器 ID 分配方式（redis） |
| `cosid.snowflake.enabled` | `COSID_SNOWFLAKE_ENABLED` | `true` | 否 | 是否启用 Snowflake |
| `cosid.snowflake.provider.order.namespace` | `COSID_SNOWFLAKE_PROVIDER_ORDER_NAMESPACE` | `dextea-trade` | 否 | 订单 ID 生成器命名空间 |

## 7. MyBatis

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `mybatis.mapper-locations` | — | `classpath*:mapper/*.xml` | 否 | Mapper XML 位置 |
| `mybatis.type-aliases-package` | — | `cn.dextea.trade.entity` | 否 | 别名包 |
| `mybatis.configuration.map-underscore-to-camel-case` | — | `true` | 否 | 下划线转驼峰 |

## 8. SpringDoc（API 文档）

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `springdoc.api-docs.path` | — | `/docs/json` | 否 | OpenAPI JSON 路径 |
| `springdoc.swagger-ui.path` | — | `/docs/ui` | 否 | Swagger UI 路径 |
| `springdoc.packages-to-scan` | — | `cn.dextea.trade.controller` | 否 | 扫描包 |
| `springdoc.paths-to-match` | — | `/api/**` | 否 | 匹配路径 |

## 9. Actuator（运维探针）

默认仅引入了依赖；必须下发 `management.endpoints.web.exposure.include` 才能暴露 HTTP 端点（见 [部署指南 §5](Deployment.md)）。

| 配置键 | 环境变量 | 默认值 | 是否必填 | 说明 |
|--------|----------|--------|------|------|
| `management.endpoints.web.exposure.include` | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | （不暴露） | 否 | 暴露的端点，例如 `health,info` |

---

### 必填项速查

- **数据库**：`DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- **Redis**：`REDIS_HOST` `REDIS_PORT`（若设置了 `REDIS_PASSWORD` 则也需要）
- **支付宝（使用支付时）**：`ALIPAY_APP_ID` `ALIPAY_PRIVATE_KEY` `ALIPAY_PUBLIC_KEY`
- **Nacos**：全部可选；未配置则跳过

下一步：👉 [环境变量](Environment-Variables.md) 或 👉 [Nacos](Nacos.md)
