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

采用 **5 位数字**，格式为「首位大类 + 第 2-3 位模块码 + 第 4-5 位具体错误」，完整规范见 `docs/api/error-codes.md`：

| 首位 | 大类 | HTTP 状态码 | 说明 | 示例 |
| --- | --- | --- | --- | --- |
| `2` | 业务错误 | 400 | 统一业务前缀，第 2-3 位为模块码（`21` 订单、`22` 支付，新模块向 `23` 递增） | 21001 顾客不存在、22001 支付宝创建交易失败 |
| `3` | 第三方/下游错误 | 500 | 第 2-3 位为渠道码（`30` 通用，新渠道向 `31` 递增） | 30001 下游服务未配置、30002 下游不可用 |
| `4` | 客户端错误 | 错误码前 3 位 | 第 2-3 位写 HTTP 状态码后两位：`400xx` 参数、`401xx` 鉴权、`404xx` 资源不存在、`409xx` 冲突幂等、`429xx` 限流 | 40002 参数缺失、40100 未登录、40901 重复提交、42901 请求过于频繁 |
| `5` | 系统错误 | 500 | `500xx` 通用系统、`501xx` 存储类 | 50000 系统繁忙、50101 数据库访问异常 |

约定：

- HTTP 状态码由 `GlobalExceptionHandler.resolveHttpStatus` 按错误码推导：`4` 段取错误码前 3 位（未列举的小段兜底 400）；业务错误（`2`）统一返回 400；`3`/`5` 段返回 500。
- 首段决定异常性质与处理策略：`3`/`5` 段通常归 `SystemException` 兜底并脱敏；`2`/`4` 段由 `BizError` 携带；`409xx`/`429xx` 等冲突限流类可转 `RetryableException`。
- 新增业务错误必须登记到对应 `XxxErrorCode` 枚举，禁止在业务代码里硬编码魔法数字。

### 4.3 统一响应结构

保持现有 `APIResponse<T>`，并补充约定：

```json
{
  "code": 21016,
  "message": "订单不存在",
  "data": null
}
```

链路 ID（`tradeid`）不在 JSON 体内，而是通过响应 Header 原样透传（如 `tradeid: a1b2c3...`）。本系统作为中台，不从自身生成该 ID：仅从上游转发的请求 Header 中读取；读到了就写入响应 Header 并向 otel 记录链路，读不到就跳过（不补、不生成），后续日志/链路即无该关联键。客户端/网关据此关联日志排障。

- 成功：`code = 0`，`message = "成功"`（沿用现状，不破坏已有契约）。
- 业务异常（`2`）/客户端错误（`4`）：返回 `BizError` 携带的 `code` 与 `message`。
- 可重试异常（`409xx`/`429xx` 等）：HTTP 侧返回对应业务码；MQ 侧不脱敏、交给重试框架。
- 系统异常（`3`/`5` 段）：`code` 用系统级兜底码（如 50000），`message` 返回脱敏后的"系统繁忙，请稍后重试"，真实原因只在日志/链路中可见。

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
- `CommonErrorCode` 按首段重组：客户端错误类 `4xxxx`（参数 `400xx`、鉴权 `401xx`、冲突幂等 `409xx`、限流 `429xx`）、系统类 `5xxxx`（如 `SYSTEM_ERROR = 50000`、`MYBATIS_SYSTEM_EXCEPTION = 50101`）。
- 各域 `XxxErrorCode` 按业务类 `2xxxxx` 重组：订单业务 `21xxx`、支付业务 `22xxx`；新增域沿用模块码规则并登记到本文档「错误码分配表」。

### 5.4 可观测性

- 链路 ID（`tradeid`）策略：作为中台，仅消费上游转发的 `tradeid` Header，有则用、无则不补；拿到后由 `TraceInterceptor` 透传至响应 Header 并向 otel 上报链路。
- 日志统一以 `tradeid` 作为 MDC 键（接入现有 otel `TraceInterceptor`），无该 Header 时该键为空，日志仍可正常输出。
- 按首段聚合监控：对 `3`/`5` 段系统/下游错误与高频 `429xx` 限流错误增加告警钩子（Metrics 计数），便于发现系统性问题。

## 6. 错误码分配表（维护）

| 码 | 名称 | 大类 | 域 | 说明 |
| --- | --- | --- | --- | --- |
| 码 | 名称 | 大类 | 域 | 说明 |
| --- | --- | --- | --- | --- |
| 50000 | SYSTEM_ERROR | 系统 | common | 未知系统错误（脱敏文案） |
| 50100 | DB_NOT_ENABLED | 系统 | common | 数据库/缓存未启用或不可用 |
| 50101 | MYBATIS_SYSTEM_EXCEPTION | 系统 | common | 数据库访问异常（脱敏文案） |
| 21xxx | OrderErrorCode | 业务 | order | 订单模块业务错误（21001 顾客不存在、21014 幂等冲突、21018 状态更新冲突等） |
| 22xxx | PayErrorCode | 业务 | payment | 支付模块业务错误（22001 支付宝创建交易失败等） |
| 30001 | DOWNSTREAM_NOT_CONFIGURED | 下游 | common | 下游服务未配置 |
| 30002 | DOWNSTREAM_UNAVAILABLE | 下游 | common | 下游不可用 |
| 40001 | MISSING_REQUEST_HEADER | 客户端 | common | 缺少请求头 |
| 40002 | PARAM_MISSING | 客户端 | common | 参数缺失 |
| 40100 | UNAUTHORIZED | 客户端 | common | 未登录 |
| 40400 | NOT_FOUND | 客户端 | common | 资源不存在 |
| 40901 | DUPLICATE_SUBMIT | 客户端 | common | 重复提交（可重试） |
| 42901 | TOO_FREQUENT | 客户端 | common | 请求过于频繁 |

> 完整码见各 `XxxErrorCode` 枚举与 `docs/api/error-codes.md` 全站错误码总表；新增码须同步登记，避免模块码冲突。

## 7. 落地建议（分阶段）

1. **阶段一（低风险）**：统一 `GlobalExceptionHandler` 兜底与错误码枚举（移除 HTTP 码混用），不改动现有业务抛错点。
2. **阶段二**：引入 `SystemException`，基础设施层异常包装；统一透传上游 `tradeid` Header 并向 otel 上报，并补告警。
3. **阶段三**：补齐 MQ 层统一异常与重试/死信策略，打通可重试语义。

---

← [返回 README](../README.md)
