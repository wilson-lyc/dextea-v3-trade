# dextea-trade

**dextea-trade** 是「德贤茶庄」线上点餐系统的交易中台，作为连接顾客端、支付渠道与门店制作系统的核心枢纽，承担订单创建、支付对接、支付回调消费、订单状态流转及制作下单等核心交易职责。系统基于 Spring Boot 构建，采用 DDD（领域驱动设计）分层架构（interface / application / domain / infrastructure），以领域模型与端口（port）/适配器（adapter）隔离业务逻辑与外部依赖，并通过 RocketMQ 与下游制作、通知等模块解耦。

## 主要功能

- **订单创建**：支持下单前预构建（算价、识别不可售商品）与正式下单两种模式，正式下单具备严格的幂等控制。
- **支付对接**：集成支付宝 JSAPI 支付，生成交易单并提供支付所需参数；支持支付金额强制覆盖（测试/联调用）。
- **支付回调消费**：消费 RocketMQ 中的支付结果回调消息，校验订单号、交易状态与金额后驱动订单进入已支付状态。
- **取餐码生成**：订单支付成功时按「门店 + 日期」生成递增取餐码，供门店出餐与取餐提醒使用。
- **订单状态流转**：支付成功后标记订单已支付并转入制作中，驱动制单消息下发至门店制作系统。
- **支付超时关单**：下单后发送延迟消息，超时未支付则自动关闭订单，释放库存与交易资源。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 框架 | Java 21、Spring Boot |
| 持久化 | MySQL（MyBatis）、Redis |
| 消息队列 | 阿里云 RocketMQ 5.x |
| 支付 | 支付宝开放平台（JSAPI） |
| 分布式 ID | CosId（Snowflake，机器号由 Redis 分配） |
| 配置 / 注册 | Nacos（可选） |
| 文档 | SpringDoc OpenAPI（Swagger） |
| 可观测性 | OpenTelemetry（Trace + Log 上报 OTLP，可选） |

## 部署与运行

### 环境依赖

下列组件为本服务运行的必要依赖，部署前须就绪：

- **JDK 21**
- **MySQL**（建议库名 `dextea`，含订单、订单项、取餐码计数等表）
- **Redis**（承载幂等键、分布式锁、缓存及 CosId 机器号）
- **阿里云 RocketMQ 5.x**（承载支付回调、制作状态、超时关单三个消息通道）
- **支付宝开放平台**应用（JSAPI 支付与异步通知回调）

Nacos 为可选的配置源与注册中心，未连接时不阻塞服务启动。

### 构建与运行

```bash
# 使用 Maven Wrapper 构建
./mvnw clean package -DskipTests

# 以可执行 JAR 启动（可通过环境变量覆盖配置）
java -jar target/trade-*.jar
```

常用环境变量（完整配置详见各文档与 `application.yaml`）：

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `SERVER_PORT` | HTTP 端口 | `9090` |
| `SPRING_APPLICATION_NAME` | 应用名 | `dextea-trade` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL 连接 | `localhost` / `3306` / `dextea` |
| `REDIS_HOST` / `REDIS_PORT` | Redis 连接 | `localhost` / `6379` |
| `ALIPAY_APP_ID` / `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` / `ALIPAY_NOTIFY_URL` | 支付宝对接参数 | 无 |
| `PAYMENT_CALLBACK_MQ_ENABLED` | 支付回调消费开关 | `false` |
| `OTEL_ENABLED` | OpenTelemetry 总开关 | `true` |
| `OTEL_LOGS_EXPORTER_ENABLED` | 日志通过 OTLP 上报开关 | `true` |
| `OTEL_LOGS_EXPORTER` | OTel 日志导出方式（`otlp`/`none`/`console`） | `otlp` |
| `OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` | OTLP 日志上报地址 | `http://localhost:4317` |
| `OTEL_RESOURCE_ATTRIBUTES` | 资源属性，如 `service.name=dextea-trade` | 无 |

### 可观测性（OpenTelemetry）

服务内置 OpenTelemetry，支持将 **Trace** 与 **日志** 通过 OTLP 协议上报到 Collector（如 OTel Collector、Grafana Alloy）。

- **总开关**：`OTEL_ENABLED`（默认 `true`）。关闭后 OTel 全部走 no-op，日志照常只输出到控制台。
- **日志上报开关**：`OTEL_LOGS_EXPORTER_ENABLED`（默认 `true`）。关闭后 logback 不再挂载 `OpenTelemetryAppender`，日志不对外上报；建议本地开发时设为 `false` 以减少无谓网络开销。
- **上报目标**：由标准 `OTEL_*` 环境变量控制，例如：

```bash
OTEL_LOGS_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=http://otel-collector:4317 \
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317 \
OTEL_RESOURCE_ATTRIBUTES=service.name=dextea-trade \
java -jar target/trade-*.jar
```

上报的日志会自动携带当前链路的 `trace_id` / `span_id`，可在后端（Grafana/Loki/Jaeger 等）与 Trace 联动检索。HTTP 请求的 `traceId` 也会写入 MDC 并输出到控制台日志行（见 `logback-spring.xml`）。

### 接口文档

服务启动后，可通过 Swagger UI 查阅接口定义：`http://localhost:9090/swagger-ui.html`

## 文档

详细的设计与实现说明请参阅 `docs/` 目录下的文档：

- [项目代码结构](docs/code-structure.md)
- [创建订单逻辑](docs/order-creation.md)
- [订单 ID 生成逻辑](docs/order-id-generation.md)
- [取餐码生成逻辑](docs/pickup-code-generation.md)
- [支付回调 MQ 消费说明](docs/payment-callback-mq.md)
- [制单 MQ 设计说明](docs/order-making-mq.md)
