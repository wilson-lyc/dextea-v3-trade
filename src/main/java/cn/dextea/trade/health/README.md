# health 模块（健康自检）

轻量级、与业务无关的**基础设施健康检测**模块，用于运维探活与上线自检。
结构扁平，未采用 DDD 四层（本身无领域逻辑），直接由 Controller 暴露端点。

## 目录结构

```
health/
├── HealthController.java      REST 入口：`/api/v1/health/{mysql,redis,backend}`
├── HealthService.java         检测接口：checkMysql / checkRedis / checkBackend
├── HealthServiceImpl.java     实现：实际探测各组件连通性
└── HealthResult.java         结果模型（component / status / message / durationMillis / details）
```

## 端点与语义

| 端点 | 检测内容 |
|------|----------|
| `GET /api/v1/health/mysql` | MySQL 连通性 |
| `GET /api/v1/health/redis` | Redis 连通性 |
| `GET /api/v1/health/backend` | 后端服务（如支付渠道连通性） |

返回 `APIResponse<HealthResult>`，并通过 HTTP 状态码表达健康度：
- `UP` → `200 OK`
- `DOWN` → `503 SERVICE_UNAVAILABLE`，`code=5001`

`HealthResult` 用 `UP` / `DOWN` 字符串约定状态，并附带耗时（`durationMillis`）、
时间戳与细节（`details`，如连接信息或异常信息），便于排查。

## 扩展指引

- 新增被检测组件（如 `checkNacos`）：在 `HealthService` 加方法 →
  `HealthServiceImpl` 实现探测逻辑 → 在 `HealthController` 加对应端点。
- 注意 `HealthResult.up()/down()` 是推荐的构造方式，保持 `status` 取值统一。
