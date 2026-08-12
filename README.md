# dextea-trade

**德贤茶庄线上点餐系统（dextea）交易端** —— 订单 + 支付服务，使用 Java 21 + Spring Boot 3 + Maven 编写，采用 DDD 分层架构。

## 项目介绍

交易端承载德贤茶庄现制茶饮点单场景的完整订单/支付流程，由 `dextea-customer-api`（C 端 Go 网关）以反向代理方式接入：

- **订单**：预构建（价格/库存预校验）、下单（幂等校验 + Redis 分布式锁 + MySQL 唯一索引兜底）、月订单列表、订单详情、支付状态查询
- **支付**：支付宝 JSAPI 支付（`alipay.trade.create`，SDK v3）、支付回调处理（校验金额/平台/交易号）、支付状态对账（本地"支付中"时主动查询支付宝并回写）
- **状态流转**：制作完成、已取餐、超时关单（延迟消息，默认 16 分钟）、取餐码生成（Snowflake + CosId）

## 技术栈

- **Java 21** + **Spring Boot 3.5.16** + Maven（`mvnw` wrapper，Maven 3.9.16）
- **MyBatis**（注解式 Mapper）+ MySQL、**Spring Data Redis**（幂等存储/创建锁/订单项缓存）
- **Nacos**（可选配置中心）、**阿里云 RocketMQ**（可选消息通道）、**CosId**（Snowflake 订单号）
- springdoc-openapi（接口文档）、spring-boot-actuator

## 目录结构

```
dextea-trade/
├── src/main/java/cn/dextea/trade/
│   ├── order/                # 核心订单模块（DDD 五层）
│   │   ├── interfaces/       # OrderController（/api/v1/orders）+ DTO + assembler
│   │   ├── application/      # 9 个 usecase + PaymentReconciliationService 对账
│   │   ├── domain/           # 模型（Order/OrderItem/Product/Store/Customer）、枚举、端口、领域服务
│   │   └── infrastructure/   # Redis 适配器、MyBatis 持久层（乐观锁 version）、CosId 订单号
│   ├── pay/                  # 支付模块
│   │   ├── AlipayPaymentAdapter        # 实现 PaymentPort（创建/查询交易）
│   │   ├── PaymentCallbackApplicationService  # 支付回调消息处理
│   │   └── PaymentCallbackMqConsumer   # 消费 RocketMQ payment_callback 主题
│   └── shared/               # APIResponse、全局异常处理、Money/Quantity 值对象、OpenApiConfig
└── src/main/resources/
    └── application.yaml      # 唯一配置文件（全部为环境变量占位符）
```

### RocketMQ 消息通道（默认全部禁用，可选启用）

| 主题 | 角色 | 用途 |
| --- | --- | --- |
| `payment_callback` | 消费者 | 支付宝支付回调异步落单 |
| `order_making_status` | 生产者 | 制作状态变化广播 |
| `order_timeout` | 生产者 + 消费者 | 延迟消息触发超时关单 |

## 配置（环境变量）

唯一配置文件 `application.yaml` 中全部关键项使用环境变量占位符（本地可用 `.env` 提供；`.env` 已被 gitignore，不提交）。Nacos 为**可选**配置源（`optional:nacos:...`），连不上不阻塞启动，本地默认值兜底。

| 配置项 | 环境变量 | 默认值 |
| --- | --- | --- |
| 服务端口 | `SERVER_PORT` | `9090` |
| 应用名 | `SPRING_APPLICATION_NAME` | `dextea-trade` |
| Nacos | `NACOS_SERVER_ADDR` / `NACOS_NAMESPACE` / `NACOS_USERNAME` / `NACOS_PASSWORD` / `NACOS_CONFIG_GROUP` | `127.0.0.1:8848` / DEFAULT_GROUP |
| MySQL | `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | `localhost:3306` / `dextea` / root |
| Redis | `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost:6379` |
| 支付宝 | `ALIPAY_APP_ID` / `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` / `ALIPAY_GATEWAY` / `ALIPAY_NOTIFY_URL` / `ALIPAY_FORCE_AMOUNT` / `ALIPAY_SUBJECT` | 无 / 官方网关 |
| RocketMQ ×3 | `*_MQ_ENABLED` / `*_MQ_ENDPOINTS` / `*_MQ_NAMESPACE` / `*_MQ_ACCESS_KEY` / `*_MQ_SECRET_KEY` / `*_MQ_TOPIC` / `*_MQ_CONSUMER_GROUP` | 默认禁用 |
| CosId | `COSID_*` | 应用名 |
| 订单参数 | `ORDER_PAYMENT_TTL`（15 分钟）/ `ORDER_CREATE_ORDER_IDEM_TTL`（1440）/ `ORDER_CREATE_ORDER_LOCK_TTL`（1）/ `ORDER_ITEM_CACHE_TTL`（120） | — |

依赖的外部服务：**MySQL**（`orders`、`order_items`、`pickup_code_counter` 等表）、**Redis**（幂等键/锁/订单项缓存/CosId 机器号）、可选 Nacos、可选阿里云 RocketMQ 5.x、支付宝开放平台。三个 MQ 默认关闭，本地最小运行仅需 MySQL + Redis。

## 本地开发

```bash
./mvnw spring-boot:run        # 依赖 .env 提供的环境变量
```

## 构建

```bash
./mvnw clean package -DskipTests
```

产物：`target/trade-0.0.1-SNAPSHOT.jar`（Spring Boot 可执行 fat jar）。

## 部署

### 运行

```bash
java -jar target/trade-0.0.1-SNAPSHOT.jar
```

所有配置通过环境变量注入，服务默认监听 `:9090`。

### systemd 示例

```bash
mkdir -p /opt/dextea-trade
cp target/trade-0.0.1-SNAPSHOT.jar /opt/dextea-trade/
```

```ini
[Unit]
Description=dextea-trade
After=network.target mysql.service redis.service

[Service]
WorkingDirectory=/opt/dextea-trade
EnvironmentFile=/opt/dextea-trade/.env
ExecStart=/usr/bin/java -jar /opt/dextea-trade/trade-0.0.1-SNAPSHOT.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

### Docker 示例

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/trade-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t dextea-trade .
docker run -d --name dextea-trade --env-file .env -p 9090:9090 dextea-trade
```

### 部署要点

- 对外暴露 `:9090`（`/api/v1/orders`），置于内网供 `dextea-customer-api` 代理调用，或经网关暴露。
- 生产环境保证 MySQL/Redis 可访问；如需支付回调实时落单，启用 RocketMQ `payment_callback` 消费者并配置支付宝 `ALIPAY_NOTIFY_URL`。
- 支付宝私钥、MQ AK/SK 等敏感配置建议由密钥管理/CI 密钥注入，勿入库。
