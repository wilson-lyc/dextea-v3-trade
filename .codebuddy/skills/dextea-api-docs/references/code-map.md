# 从代码提取文档事实

接口文档的每一项都必须能在代码中找到出处。按下表定位，`{module}` 为 `order` / `payment` 等领域模块。

## 目录地图

| 需要的信息 | 代码位置 |
| ---------- | -------- |
| HTTP 方法、路径、路径参数、请求头 | `src/main/java/cn/dextea/trade/{module}/interfaces/http/controller/*Controller.java` |
| 查询 / Body 参数及校验规则 | `.../interfaces/http/dto/request/*Request.java` |
| 响应 `data` 字段 | `.../interfaces/http/dto/response/*Response.java` |
| 请求 / 响应与应用层的字段映射 | `.../interfaces/http/assembler/*Assembler.java` |
| 业务流程、前置条件、副作用 | `.../application/usecase/*UseCase.java` 及其调用的领域服务 |
| 应用层入参 / 出参 | `.../application/dto/command/*Command.java`、`.../application/dto/result/*Result.java` |
| 业务错误码 | `.../{module}/domain/exception/*ErrorCode.java`（`OrderErrorCode`、`PayErrorCode`） |
| 通用 / 鉴权 / 下游错误码 | `cn/dextea/trade/shared/error/`（`CommonErrorCode`、`AuthErrorCode`、`DownstreamErrorCode`） |
| 框架异常到错误码的映射、HTTP 状态码推导 | `cn/dextea/trade/shared/infrastructure/web/GlobalExceptionHandler.java` |
| 令牌校验与 401 行为 | `cn/dextea/trade/shared/infrastructure/auth/AuthInterceptor.java` |
| 响应信封 | `cn/dextea/trade/shared/api/APIResponse.java` |
| 模块整体分层说明 | `docs/code-structure.md` |

## 提取要点

### 路径与请求头

以 `@RequestMapping` 类级前缀拼接方法级 `@GetMapping` / `@PostMapping` 得到完整路径。请求头看 `@RequestHeader`，常量名如 `STORE_ID_HEADER` / `CUSTOMER_ID_HEADER` 在 Controller 顶部定义。带 `@NotNull` 的头即必填，缺失时返回 `40001`。

### 参数必填性与类型

- 必填性看 `@NotNull` / `@NotBlank` / `@NotEmpty`；无校验注解且业务上可空的写「否」，并说明缺省行为（缺省值通常在 Request DTO 字段初始值或 UseCase 中）。
- 长度、范围、精度看 `@Size` / `@Max` / `@Min` / `@Digits` 等注解，逐条写入「说明」列。
- 文档类型词使用 `string` / `integer` / `long` / `decimal` / `boolean` / `object` / `array`，与 Java 类型对应：`Long`→`long`、`Integer`→`integer`、`BigDecimal`→`decimal`、`List<T>`→`array`。
- 嵌套结构在下方单独用表格描述，字段名带父级前缀（如 `items[].skuId`）或用「`items` 数组元素」小节。
- 请求与响应同构、但由服务端填充的字段，注明「响应填充，请求无需传」。

### 枚举值

顺着字段类型找到领域枚举，逐一列出「值 - 含义」。若该枚举已在 `docs/api/README.md` 的枚举字典中，取值必须与之完全一致，不得各篇不同。

### 错误码收集

顺 UseCase 调用链逐层看真实抛出点，不要照抄枚举全集：

1. `grep` 本接口 UseCase 及其下游领域服务、仓储中的 `BizError` / `SystemException` 抛出处，取其携带的错误码枚举。
2. 加上框架层必然可能出现的通用码：缺失请求头 `40001`、参数校验 `40002`、未登录 `40100`、兜底 `50000`、数据库异常 `50101`。
3. 涉及支付渠道调用的接口，补 `PayErrorCode` 与 `DownstreamErrorCode` 中实际会冒泡到该接口的码。
4. 每个码的「说明」列可在枚举 message 基础上补充触发条件（如 `21014` 补「idempotencyKey 冲突」），使调用方能据此定位问题。
