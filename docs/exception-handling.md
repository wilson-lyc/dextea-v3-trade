# 异常处理机制重构设计

> 本文档结合主流大厂（阿里、美团等）异常处理实践与本项目现状，给出一套统一的异常处理与错误码规范，作为后续重构的基线。
> 调研来源：《阿里巴巴 Java 开发手册》及社区解读（阿里云开发者社区）、Spring Boot 全局异常处理实践、微服务错误码设计实践等。

## 1. 设计目标

- **统一出口**：所有异常只在一处（全局异常处理器）被转换为统一的 `APIResponse` 结构，避免散落在各 Controller 里 try-catch。
- **可定位**：每个错误都带有稳定、可读的错误码，便于日志检索、告警聚合、前端文案映射。
- **分层清晰**：业务异常在领域/应用层抛出携带语义，技术异常在技术层兜底，调用方（HTTP / MQ）各取所需。
- **信息安全**：对外只返回脱敏后的错误码与文案，堆栈、SQL、内部路径等敏感信息不泄露给客户端。
- **可重试区分**：可重试异常（如并发冲突、瞬时外部失败）与终态异常明确区分，供 MQ 消费端决定重试或死信。

## 2. 大厂实践要点（调研摘要）

| 要点 | 来源 / 说明 |
| --- | --- |
| 早抛出、晚捕获（Fail Fast, Catch Late） | 阿里 Java 异常处理最佳实践；问题在发生的层立即抛，只在能处理的层捕获 |
| 只捕获可处理的异常 | 不应为了"吞掉"异常而 catch，空 catch 是严重反模式 |
| 抛出具体异常，不抛泛化 `Exception` / `Throwable` | 异常类型要与方法语义一致，便于精确处理 |
| 用异常链保留根因（`cause`） | 包装第三方异常时务必传入原始 cause，避免吞掉根因 |
| 记录或抛出，不同时做 | 同一异常不要在多层重复 `log.error`，在最终兜底处统一记录 |
| 不用异常控制正常业务流程 | 异常处理错误分支，状态机的正常流转不应靠抛异常驱动 |
| 资源用 try-with-resources 释放 | 连接、流、锁在 finally / try-with-resources 中清理 |
| 不在 finally 块中抛新异常 | 否则会覆盖 try 块异常，丢失根因 |
| 5–6 位错误码 + 模块化分层 | 企业级错误码体系：全局通用码 + 业务模块码分段 |
| 统一返回体结构 | `code / message / data`；链路 ID（`tradeid`）通过响应 Header 原样透传，不进 JSON 体 |

## 3. 本系统现状

已实现的基础：

- `shared.error.BizError`（业务异常基类，继承 `RuntimeException`，携带 `BizErrorCode`）。
- `shared.error.BizErrorCode` 接口 + 各域枚举：`CommonErrorCode`(100xxx)、`OrderErrorCode`(101xxx)、`PayErrorCode`(102xxx)，已具备错误码分段雏形。
- `shared.infrastructure.web.GlobalExceptionHandler`（`@RestControllerAdvice`）统一兜底 HTTP 异常。
- `shared.api.APIResponse` 统一响应体。
- `RetryableException` 与 `RetryableCallbackException`、`RetryableOrderException` 用于区分可重试异常。

存在的一致性缺口：

1. **HTTP 与 MQ 不对称**：`GlobalExceptionHandler` 只覆盖 HTTP 入口；MQ 消费者（`PaymentCallbackMqConsumer`、`OrderTimeoutMqConsumer`）未接入统一异常转换与重试策略。
2. **返回体不一致**：成功用 `code=0`，业务异常用错误码，技术异常用 HTTP 状态码数字（如 500），三者语义混用，前端难以统一判断。
3. **可重试语义未贯穿**：`RetryableException` 与 `RuntimeException`/`Exception` 兜底无关联，MQ 消费端无法基于异常类型自动决定重试。
4. **错误码枚举分散**：`CommonErrorCode.NOT_FOUND` 用 `404` 与 HTTP 码混淆，未与业务码体系统一。
5. **缺少统一的"系统错误"兜底文案与监控钩子**：技术异常仅 `log.error`，未关联链路 ID / 告警。

## 4. 总体设计

### 4.1 异常分层

```
Throwable
└── RuntimeException
    ├── BizError                        # 业务异常（可预期、对客户端可见语义）
    │   └── <各域异常，可携带 BizErrorCode>
    ├── RetryableException              # 可重试异常（并发冲突/瞬时外部失败）
    │   ├── RetryableOrderException
    │   └── RetryableCallbackException
    └── SystemException (新增)          # 不可重试的技术异常（兜底、脱敏）
```

原则：

- 领域层、应用层只抛 `BizError`（携带语义错误码）或 `RetryableException`（明确可重试）。不抛 `Exception` / 技术框架异常。
- 基础设施层（adapter / repository）将第三方异常（SQL、Redis、HTTP 客户端、支付宝 SDK）包装为 `BizError` 或 `SystemException`，并保留 `cause`。
- `GlobalExceptionHandler` / MQ 处理器只认这三类，其余 `Exception` 一律归为 `SystemException` 兜底。

### 4.2 错误码规范

采用 **5 位数字**，按首段区分错误大类，与 HTTP 状态码完全解耦（业务码不取 404/500 这类 HTTP 值）。首段即错误性质，便于监控聚合、网关路由与前端文案映射：

| 段位 | 大类 | 含义 | 示例 |
| --- | --- | --- | --- |
| `1xxxxx` | 系统错误 | 基础设施故障、未知 panic、未预期的底层异常 | 10000 内部错误、10300 数据库未启用 |
| `2xxxxx` | 业务错误 | 领域规则不满足（订单/支付等业务校验） | 20001 订单已取消不可支付 |
| `3xxxxx` | 下游依赖错误 | 调用外部/中台服务失败 | 30001 下游服务未配置、30002 下游不可用 |
| `4xxxxx` | 参数 / 校验错误 | 入参缺失、格式非法、鉴权缺失 | 40001 参数缺失、40100 未登录 |
| `5xxxxx` | 限流 / 幂等 / 熔断 | 请求过于频繁、重复提交、熔断降级 | 50001 请求过于频繁、50002 重复提交 |

子类段（第 2–3 位）按业务域细分，规则建议：

| 子类段 | 归属 | 对应枚举 |
| --- | --- | --- |
| `10xxx` | 通用系统 | `CommonErrorCode` |
| `11xxx` | 订单域系统 | `OrderErrorCode`（系统类） |
| `12xxx` | 支付域系统 | `PayErrorCode`（系统类） |
| `20xxx` | 订单域业务 | `OrderErrorCode`（业务类） |
| `21xxx` | 支付域业务 | `PayErrorCode`（业务类） |
| `30xxx` | 下游依赖（订单中台等） | `DownstreamErrorCode`（新增） |
| `40xxx` | 通用参数/鉴权 | `CommonErrorCode` |
| `50xxx` | 限流/幂等/熔断 | `CommonErrorCode` |

约定：

- 业务码与 HTTP 状态码是两套独立体系。HTTP 状态码仅表达传输层语义（2xx/4xx/5xx），`APIResponse.code` 表达业务语义。
- 首段决定异常性质与处理策略：`1xxxxx`/`3xxxxx` 通常归 `SystemException` 兜底并脱敏；`2xxxxx`/`4xxxxx` 由 `BizError` 携带；`5xxxxx` 通常可重试（如 50002 重复提交应转 `RetryableException`）。
- `CommonErrorCode.NOT_FOUND` 等占位 HTTP 值（404）应改为独立业务码（如 40001），不再复用 HTTP 数字。
- 新增业务错误必须登记到对应 `XxxErrorCode` 枚举，禁止在业务代码里硬编码魔法数字。

> 迁移说明：现有码为 6 位（100xxx 通用、101xxx 订单、102xxx 支付）。重构时将整体迁移到 5 位首段方案——原 `101xxx` 订单业务码并入 `2xxxxx`，原 `102xxx` 支付业务码并入 `2xxxxx`（支付子类 `21xxx`），系统/下游/参数/限流类按上表重新分配。迁移期间两套码并存需在响应 Header 中透传上游 `tradeid` 以便比对。

### 4.3 统一响应结构

保持现有 `APIResponse<T>`，并补充约定：

```json
{
  "code": 20001,
  "message": "订单已取消不可支付",
  "data": null
}
```

链路 ID（`tradeid`）不在 JSON 体内，而是通过响应 Header 原样透传（如 `tradeid: a1b2c3...`）。本系统作为中台，不从自身生成该 ID：仅从上游转发的请求 Header 中读取；读到了就写入响应 Header 并向 otel 记录链路，读不到就跳过（不补、不生成），后续日志/链路即无该关联键。客户端/网关据此关联日志排障。

- 成功：`code = 0`，`message = "成功"`（沿用现状，不破坏已有契约）。
- 业务异常（`2xxxxx`/`4xxxxx`）：返回 `BizError` 携带的 `code` 与 `message`。
- 可重试异常（`5xxxxx` 等）：HTTP 侧返回对应业务码；MQ 侧不脱敏、交给重试框架。
- 系统异常（`1xxxxx`/`3xxxxx`）：`code` 用系统级兜底码（如 10000），`message` 返回脱敏后的"系统繁忙，请稍后重试"，真实原因只在日志/链路中可见。

## 5. 改造清单

### 5.1 Web 层（`GlobalExceptionHandler`）

- 保留 `BizError` 处理；新增 `RetryableException` 处理（返回明确可重试标识，便于网关重试）。
- 新增 `SystemException` 兜底，脱敏文案；若请求带上游 `tradeid`，则原样写回响应 Header 并向 otel 记录链路，否则不补。
- 将 `Exception` / `RuntimeException` 兜底收敛为统一 `SystemException` 转换，避免重复日志。
- 校验类异常（`MethodArgumentNotValidException` 等）统一使用 `CommonErrorCode` 体系，不混用 HTTP 数字。

### 5.2 MQ 层

- 在 `PaymentCallbackMqConsumer`、`OrderTimeoutMqConsumer` 增加统一异常拦截：
  - `BizError`（终态、不可重试）→ 记录日志，不再重投。
  - `RetryableException` → 抛出交由 Spring Retry / MQ 重试机制，超过阈值进死信。
  - `SystemException` / 其他 → 记录日志并走重试或死信，视幂等性而定。
- 回调类消费建议实现幂等，重试安全。

### 5.3 错误码枚举

- 新增 `DownstreamErrorCode`（3xxxxx）承载下游依赖错误。
- `CommonErrorCode` 按首段重组：系统类 `1xxxxx`（如 `SYSTEM_ERROR = 10000`、`MYBATIS_SYSTEM_EXCEPTION = 10300`）、参数/鉴权类 `4xxxxx`（如 `MISSING_REQUEST_HEADER = 40001`）、限流/幂等类 `5xxxxx`（如 `RETRY_LATER = 50002`）；移除与 HTTP 码混淆的项（如 404）。
- 各域 `XxxErrorCode` 按业务类 `2xxxxx` 重组：订单业务 `20xxx`、支付业务 `21xxx`；新增域沿用首段规则并登记到本文档"错误码分配表"。

### 5.4 可观测性

- 链路 ID（`tradeid`）策略：作为中台，仅消费上游转发的 `tradeid` Header，有则用、无则不补；拿到后由 `TraceInterceptor` 透传至响应 Header 并向 otel 上报链路。
- 日志统一以 `tradeid` 作为 MDC 键（接入现有 otel `TraceInterceptor`），无该 Header 时该键为空，日志仍可正常输出。
- 按首段聚合监控：对 `1xxxxx`/`3xxxxx` 系统/下游错误与高频 `5xxxxx` 限流错误增加告警钩子（Metrics 计数），便于发现系统性问题。

## 6. 错误码分配表（维护）

| 码 | 名称 | 大类 | 域 | 说明 |
| --- | --- | --- | --- | --- |
| 10000 | SYSTEM_ERROR | 系统 | common | 未知系统错误（脱敏文案） |
| 10300 | DB_NOT_ENABLED | 系统 | common | 数据库/缓存未启用或不可用 |
| 10301 | MYBATIS_SYSTEM_EXCEPTION | 系统 | common | 数据库访问异常（脱敏文案） |
| 20001 | CUSTOMER_NOT_FOUND | 业务 | order | 顾客不存在 |
| 20014 | IDEMPOTENCY_KEY_CONFLICT | 业务 | order | 重复提交（幂等冲突） |
| 20018 | ORDER_UPDATE_CONFLICT | 业务 | order | 状态更新冲突（可重试） |
| 20028 | ORDER_PAYMENT_PICKUP_CODE_REQUIRED | 业务 | order | 订单支付必须含取餐码 |
| 21001 | ALIPAY_CREATE_TRADE_FAILED | 业务 | payment | 支付宝创建交易失败 |
| 30001 | DOWNSTREAM_NOT_CONFIGURED | 下游 | common | 下游服务未配置 |
| 30002 | DOWNSTREAM_UNAVAILABLE | 下游 | common | 下游不可用 |
| 40001 | NOT_FOUND / MISSING_REQUEST_HEADER | 参数 | common | 资源/请求头缺失 |
| 40002 | PARAM_MISSING | 参数 | common | 参数缺失 |
| 40100 | UNAUTHORIZED | 参数 | common | 未登录 |
| 50001 | TOO_FREQUENT | 限流 | common | 请求过于频繁 |
| 50002 | DUPLICATE_SUBMIT | 限流 | common | 重复提交 |
| 40001 | MISSING_REQUEST_HEADER | 参数 | common | 参数缺失（迁移自 100001，移除 404 混用） |
| 40100 | UNAUTHORIZED（建议新增） | 参数 | common | 未登录 |
| 50001 | TOO_FREQUENT（建议新增） | 限流 | common | 请求过于频繁 |
| 50002 | DUPLICATE_SUBMIT | 限流 | common | 重复提交（可重试，迁移自 101014 幂等语义） |

> 完整码见各 `XxxErrorCode` 枚举；新增码须在此表登记并避免首段/子类段冲突。迁移期旧 6 位码逐步下线。

## 7. 落地建议（分阶段）

1. **阶段一（低风险）**：统一 `GlobalExceptionHandler` 兜底与错误码枚举（移除 HTTP 码混用），不改动现有业务抛错点。
2. **阶段二**：引入 `SystemException`，基础设施层异常包装；统一透传上游 `tradeid` Header 并向 otel 上报，并补告警。
3. **阶段三**：补齐 MQ 层统一异常与重试/死信策略，打通可重试语义。

---

← [返回 README](../README.md)
