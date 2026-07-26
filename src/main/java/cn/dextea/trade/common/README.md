# common 模块（跨模块通用）

`common` 是与具体业务无关的**横切基础设施**：统一响应体、枚举工具、业务异常、
全局异常处理、OpenAPI 文档配置。所有业务模块（catalog / order / pay）均依赖它，
而它**不依赖任何业务模块**，处于依赖图最底层。

## 目录结构

```
common/
├── api/APIResponse.java              统一接口响应包装（code / message / data）
├── enums/                            枚举基础工具
│   ├── CodeEnum.java                带 code 的枚举接口约定
│   ├── StringCodeEnum.java          code 为 String 的枚举接口
│   └── EnumUtils.java               枚举 ↔ code 的查找/反查工具
├── error/                           业务异常
│   ├── BizErrorCode.java            全局业务错误码枚举（各域 *ErrorCode 统一引用）
│   └── BizError.java               继承 RuntimeException 的业务异常（携带错误码）
├── web/GlobalExceptionHandler.java  @RestControllerAdvice 全局异常拦截，统一转 APIResponse
└── config/OpenApiConfig.java       SpringDoc / OpenAPI（Swagger）配置
```

## 使用约定

- **统一响应**：所有 Controller 返回 `APIResponse<T>`（`code=0` 表示成功）。
- **业务异常**：业务校验失败抛 `BizError(BizErrorCode, "补充说明")`；
  `GlobalExceptionHandler` 捕获后自动转为对应 `code` 的错误响应，无需在各 Controller 手写 try-catch。
- **枚举规范**：状态/类型类枚举建议实现 `CodeEnum` / `StringCodeEnum`，用 `EnumUtils`
  按 `code` 反查，避免在领域代码中出现散落的魔法数字（参考 `order/domain/enums` 与 `catalog/domain/enums`）。

## 扩展指引

- 新增全局错误码：在 `error/BizErrorCode` 追加枚举项（需给定 `code` 与 `message`）。
- 新增模块级错误码（如 `OrderErrorCode`、`PayErrorCode`）：各自域 `exception/` 下定义，
  内部组合 `BizError` / `BizErrorCode` 使用，保持异常出口统一。
