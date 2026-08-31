---
name: dextea-api-docs
description: Writes and maintains the hand-written HTTP API docs under docs/api/ of the dextea-trade project. This skill should be used when adding, updating, reviewing or auditing an interface document, when a Controller / request DTO / response DTO / error code enum changes and its doc must follow, when registering a new endpoint in the doc index, or when maintaining the error code registry in docs/api/error-codes.md.
---

# dextea-trade 接口文档维护

## 概述

`docs/api/` 下的接口文档为**手工维护**，不由代码生成，因此文档与代码一致性完全依赖流程约束。本 skill 提供从代码提取事实、按固定章节结构成文、同步索引与错误码总表的完整工作流。

## 权威规范（不在本 skill 中复制，务必现场读取）

动笔前先读，以仓库文件为准：

| 文件 | 内容 |
| ---- | ---- |
| `docs/api-authoring/example.md` | 章节结构与逐节写法规范、完整示例 |
| `docs/api/error-codes.md` | 错误码分段规范、全站错误码总表 |
| `docs/api/README.md` | 接口索引、鉴权/身份标识/响应信封/枚举字典等公共约定 |
| 同端的邻近文档 | 措辞与排版惯例的事实标准，如 `store-mark-order-ready.md` |

本 skill 只承载「怎么做」，规范细节以上述文件为准；两者冲突时按仓库文件执行，并提示用户规范已漂移。

## 工作流

### 1. 判断任务类型

- **新增接口** → 走 2~6 全流程。
- **接口变更**（参数增删、字段改名、错误码调整）→ 走 2、3、5、6，只改受影响章节，不重写整篇。
- **文档纠错 / 审计** → 直接走 6 的自检清单，逐条核对。

### 2. 从代码提取事实

禁止凭猜测写文档。所有字段、类型、必填性、枚举值、错误码都必须在代码中找到出处。代码定位方法与目录地图见 `references/code-map.md`。

### 3. 成文

- 复制 `assets/api-doc-template.md` 作为骨架，按 `docs/api-authoring/example.md` 的九章结构填写，章节顺序与标题不得增删改。
- 文件名 `{端}-{资源}-{动作}.md`，小写中划线，如 `customer-create-order.md`。
- 示例值贴近真实业务，禁止 `xxx` / `foo` 占位。
- 枚举值逐一列出「值 - 含义」，与代码枚举一致；全站通用枚举同时核对 `README.md` 的枚举字典。

### 4. 错误码字典

- 只列**本接口实际可能抛出**的码，依据是链路上真实抛出的 `BizError` / `SystemException`。
- HTTP 状态码不自行判断，按 `error-codes.md` 的推导规则填：`4` 开头取错误码前 3 位；`2` 开头 → 400；`3` / `5` 开头 → 500。
- 排列顺序沿用现有文档：先 `5xxxx` 系统类，再 `2xxxx` 业务类（升序），最后 `4xxxx` 客户端类（升序）。
- 每篇至少包含 `40001`、`40002`、`40100`、`50000`、`50101` 中与本接口相关者。
- 代码中新增或改动了错误码枚举 → 必须同步更新 `docs/api/error-codes.md` 对应小节（通用错误按参数/鉴权/资源/幂等/限流/系统/数据库/下游分组，业务错误按订单/支付等模块分组），并检查是否影响其他接口文档。

### 5. 登记索引

新增接口必须在 `docs/api/README.md` 对应端的表格中补一行（接口 / 方法 / 路径 / 文档链接）；路径或方法变更时同步修正该行。

### 6. 自检

按 `references/checklist.md` 逐条核对后再交付，重点是文档与代码的字段级一致性。

## 硬性约束

- **一个接口一个 md 文件**，不合并多接口。
- **不新增章节、不删除章节**；某类参数不存在时写「无 Body 参数。」而非删掉小节。
- **公共内容不下沉重复**：鉴权格式、响应信封、错误码分段这类全站约定，引用公共文档，不在单篇接口文档中另行解释。
- 但请求头表格与错误码字典**必须逐篇完整列出**，保证单篇可独立阅读。
- 文档使用简体中文，字段名/类型/错误码等标识符保持原文。
