# dextea-trade（德贤茶庄线上点餐系统 · 交易端后台）

`dextea-trade` 是「德贤茶庄」线上点餐系统的**交易端后台服务**，负责订单创建、下单前的商品/客制化选项可用性校验、与支付宝对接完成支付下单等核心交易链路。

- 应用名：`dextea-trade`
- 技术栈：Spring Boot 3.5 / Java 21
- 默认端口：`9090`
- 接口文档（Swagger UI）：`http://<host>:9090/docs/ui`，OpenAPI JSON：`/docs/json`

## 核心能力

- **订单创建**：`POST /api/orders`，含 Redis 幂等 + MySQL 唯一索引双重保障。
- **下单前置校验**：解析 `skuId`，校验门店、商品、客制化选项的可用性，存在不可用时返回清单而不落库。
- **支付宝对接**：调用 `alipay.trade.create` 创建支付单，并回填 `trade_no`。
- **分布式 ID**：基于 CosId（Snowflake）生成全局唯一订单号。

> 更完整的业务流程与已知问题，见 [`docs/Order-Creation-Flow-Analysis.md`](docs/Order-Creation-Flow-Analysis.md)。

## 技术依赖

| 组件 | 用途 |
|------|------|
| MySQL + MyBatis | 业务数据持久化 |
| Redis | 幂等缓存、CosId 机器号分配 |
| 支付宝 SDK（alipay-sdk-java-v3） | 支付下单 |
| Nacos Config | 可选，远程配置中心（默认关闭，不配置也能跑） |
| CosId | 分布式 Snowflake ID |
| SpringDoc | API 文档 |

## 快速部署（环境变量模式）

本服务支持**纯环境变量注入配置**，无需任何外部配置文件即可启动，适合容器化与 PaaS 部署。

### 1. 前置依赖

- JDK 21
- 一个可用的 MySQL 实例
- 一个可用的 Redis 实例
- （可选）支付宝开放平台应用凭证；不使用支付宝支付时可跳过，但服务仍能正常启动

### 2. 构建

```bash
./mvnw clean package -DskipTests
# 产物：target/dextea-trade-0.0.1-SNAPSHOT.jar
```

### 3. 运行（设置必需的环境变量）

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=dextea
export DB_USERNAME=root
export DB_PASSWORD=your_db_password

export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=            # 无密码可留空

export ALIPAY_APP_ID=your_app_id
export ALIPAY_PRIVATE_KEY="-----BEGIN PRIVATE KEY----- ..."
export ALIPAY_ALIPAY_PUBLIC_KEY="-----BEGIN PUBLIC KEY----- ..."

java -jar target/dextea-trade-0.0.1-SNAPSHOT.jar
```

> ⚠️ **密钥类变量（如 `ALIPAY_PRIVATE_KEY`）必须使用双引号包裹**，避免换行与特殊字符被 Shell 解析破坏。私钥通常是多行内容，建议通过 `--env-file` 或编排工具注入。

### 4. 用 Docker 一键部署（推荐）

```bash
docker run -d --name dextea-trade -p 9090:9090 \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e DB_NAME=dextea \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=your_db_password \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  -e ALIPAY_APP_ID=your_app_id \
  -e ALIPAY_PRIVATE_KEY="$ALIPAY_PRIVATE_KEY" \
  -e ALIPAY_ALIPAY_PUBLIC_KEY="$ALIPAY_ALIPAY_PUBLIC_KEY" \
  dextea-trade:latest
```

启动后访问 `http://<host>:9090/docs/ui` 查看接口文档。

### 必需 vs 可选参数

| 参数 | 是否必需 | 说明 |
|------|----------|------|
| `DB_*` | ✅ 必需 | 缺省值仅适用于本地 Demo |
| `REDIS_*` | ✅ 必需 | 幂等与机器号分配依赖 Redis |
| `ALIPAY_*` | ⚠️ 条件 | 需要支付宝支付时必填；否则可留空，服务仍可启动 |
| `NACOS_*` | ❌ 可选 | 不配置时自动跳过 Nacos，使用本地/环境变量配置 |
| `SERVER_PORT` | ❌ 可选 | 默认 `9090` |
| `SPRING_PROFILES_ACTIVE` | ❌ 可选 | 默认 `default` |

完整的环境变量清单、默认值与示例，见 **[`docs/Environment-Variables.md`](docs/Environment-Variables.md)**。

## 部署方案总览

本服务提供两种配置来源、多种部署形态：

- **环境变量模式**（默认、零配置文件）：适合 Docker / K8s / 云函数等所有容器化场景。详见 [Configuration Parameters](docs/Configuration-Parameters.md) 与 [Environment Variables](docs/Environment-Variables.md)。
- **Nacos 模式**（可选）：适合多环境、多实例统一管理配置。详见 [Nacos Configuration](docs/Nacos.md)。
- 两种模式可组合：Nacos 中未下发的配置仍可由环境变量兜底。

更系统的部署说明（含构建、启动、健康检查、回滚）请阅读 **[`docs/Deployment.md`](docs/Deployment.md)**。

## 文档索引

| 文档 | 内容 |
|------|------|
| [docs/README.md](docs/README.md) | 文档导航首页 |
| [docs/Deployment.md](docs/Deployment.md) | 部署总览（构建、启动、健康检查、回滚） |
| [docs/Configuration-Parameters.md](docs/Configuration-Parameters.md) | 全量参数一览表（按组件分类） |
| [docs/Environment-Variables.md](docs/Environment-Variables.md) | 环境变量命名规则、完整清单与示例 |
| [docs/Nacos.md](docs/Nacos.md) | Nacos 接入方式、Data ID、配置示例与优先级 |
| [docs/Order-Creation-Flow-Analysis.md](docs/Order-Creation-Flow-Analysis.md) | 业务流程分析与已知问题（原有文档） |
