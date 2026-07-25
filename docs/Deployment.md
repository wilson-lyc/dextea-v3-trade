# 部署指南

本文档说明 `dextea-trade` 的部署形态、构建产物、启动方式、健康检查以及回滚注意事项，并解释服务的**两种配置来源**（环境变量 / Nacos）如何取舍。

> 关于完整参数列表，请查阅 [配置参数](Configuration-Parameters.md)；关于各模式的具体写法，请查阅 [环境变量](Environment-Variables.md) 与 [Nacos](Nacos.md)。

## 1. 环境与依赖

| 依赖 | 版本 / 要求 | 说明 |
|------|-------------|------|
| JDK | 21 | Spring Boot 3.5 所需 |
| MySQL | 5.7+ / 8.x | 业务数据库 |
| Redis | 5+ | 幂等缓存、CosId 机器 ID |
| Nacos | 2.x | **可选**，仅在使用 Nacos 配置时才需要 |

## 2. 构建

```bash
./mvnw clean package -DskipTests
# 产物：target/dextea-trade-0.0.1-SNAPSHOT.jar
```

若你还需要 Docker 镜像，请在此阶段接入团队的镜像构建流程（本仓库未附带 Dockerfile；最小化示例见第 4 节）。

## 3. 两种配置来源

服务通过 `application.yaml` 装配，并支持两种外部配置来源：

### 方案 A：环境变量（默认，容器部署推荐）

- 无需 Nacos，也无需额外的配置文件。
- 所有连接信息与密钥都通过环境变量注入，遵循十二要素“配置与代码分离”的原则。
- 适用于 Docker / Kubernetes / 云函数。
- 👉 写法与列表：[环境变量](Environment-Variables.md)。

### 方案 B：Nacos 配置中心（可选）

- 在环境中设置 `NACOS_SERVER_ADDR` 后，服务会在启动时尝试从 Nacos 拉取 `dextea-trade.yaml`（Data ID）。
- 适用于跨多个实例与环境（dev/test/prod）统一管理配置，并支持动态变更。
- Nacos 与本地/环境变量**可叠加**：Nacos 下发的配置优先级更高；未下发的参数回退到环境变量默认值。
- 👉 集成方式：[Nacos](Nacos.md)。

> 选择建议：单实例或容器化部署优先采用**方案 A**；当实例数量增长、需要统一管理 / 热刷新时，再迁移到**方案 B**（或两者结合）。

## 4. 启动方式

### 4.1 直接运行 jar

```bash
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=dextea \
DB_USERNAME=root DB_PASSWORD=**** \
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 \
ALIPAY_APP_ID=app_id ALIPAY_PRIVATE_KEY="$KEY" ALIPAY_PUBLIC_KEY="$PUB" \
java -jar target/dextea-trade-0.0.1-SNAPSHOT.jar
```

### 4.2 Docker（最小化示例）

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

> `.env` 文件保存所有环境变量，避免密钥出现在命令行上。完整变量列表：[环境变量](Environment-Variables.md)。

## 5. 健康检查与就绪探针

服务引入了 `spring-boot-starter-actuator`，可结合探针使用：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- 存活/就绪探针：`GET /actuator/health`
- 当应用已启动但某依赖（如 MySQL、Redis）尚未就绪时，`health` 返回 `DOWN`，可用于 K8s 的就绪控制。

> 注意：若需要在容器内暴露 Actuator 端点，请通过环境变量或 Nacos 下发上述 `management.*` 配置，写法同 [配置参数](Configuration-Parameters.md) 中“Spring / Actuator”一节。

## 6. 回滚与版本管理

- 版本：`0.0.1-SNAPSHOT`；建议为生产构建打上固定版本标签。
- 回滚：保留历史 jar / 镜像标签，直接重启旧版本即可；由于配置与代码分离，回滚无需改动任何配置。
- 数据库：迁移脚本建议单独管理（本仓库暂未包含 SQL 初始化脚本）；在回滚应用前，请确认表结构兼容性。

## 7. 常见部署问题

| 现象 | 排查方向 |
|------|------|
| 启动时数据源/Redis 连接失败 | 检查 `DB_*` / `REDIS_*` 环境变量是否已设置且网络可达 |
| 支付宝调用报“key error” | 检查 `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` 是否被 shell 截断（始终用双引号包裹） |
| 启动时卡在 Nacos 拉取 | 无 `NACOS_SERVER_ADDR` 时应经 `optional:` 自动跳过；若配置有误，请检查地址/命名空间 |
| 实例间 `machineId` 冲突 | CosId 使用 Redis 分配机器 ID；请确保 Redis 可用且各实例 `cosid.namespace` 一致 |

相关参数定义：[配置参数](Configuration-Parameters.md)。
