# DDD 重构优化与改造分析报告

> 分析对象：`dextea-trade`（德贤茶庄线上点餐系统 · 交易端后台）
> 分析基线：分支 `refactor/order-domain-model`（`git` 当前干净工作区）
> 分析目标：在已完成的 DDD 分层重构基础上，识别**进一步需要优化和改造**的方向，给出优先级与落地建议。

---

## 1. 背景与结论摘要

项目已完成从「贫血 Service + Mapper」到**四层六边形架构**的重构：

```
interfaces   → controller / dto / assembler
application  → OrderCommandService / OrderQueryService（命令/查询分离）
domain       → model / service / port / util
infrastructure → adapter（端口适配）/ persistence（Mapper 适配）
```

重构整体方向正确，端口-适配器、状态机 + 乐观锁、幂等设计均已落地，且旧文档《Order-Creation-Flow-Analysis.md》中指出的多项缺陷（索引越界、缺订单明细、未校验门店状态、`diningMethod` 被丢弃）**已经修复**。

但当前落地仍停留在「**结构上分层、行为上仍偏服务化**」的阶段，距离成熟的 DDD 还有若干关键差距。核心问题集中在三点：

1. **业务完整性缺口**：库存从未扣减（超卖风险），`createOrder` 无事务边界且同步耦合支付宝，存在悬空订单。
2. **防腐层（ACL）名不副实**：订单上下文直接依赖商品目录的领域模型，限界上下文的边界被击穿。
3. **聚合根贫血**：`Order` 是纯数据载体，状态机是静态工具类，不变式未收归聚合内部。

下文按优先级展开，并在第 6 节给出改造清单与路线建议。

---

## 2. 重构已取得的成果（肯定）

为避免"为改而改"，先明确已经做对的部分：

| 维度 | 已落地情况 |
|------|-----------|
| 分层架构 | interfaces / application / domain / infrastructure 四层清晰，包命名规范 |
| 端口-适配器 | `OrderRepository`、`ProductCatalogPort`、`CustomerPort`、`StorePort`、`PaymentClientPort`、`OrderLockPort` 等均有独立 interface + impl |
| 支付状态机 | `OrderStatusMachine` 以「当前状态 + 事件 → 目标状态」白名单描述合法流转 |
| 并发安全 | `OrderStatusDomainService.changeStatus` 用 Redis 分布式锁 + `version` 列 CAS 更新（`WHERE trade_status=? AND version=?`），并写 `OrderStatusLog` 审计 |
| 幂等设计 | Redis 快校验 + MySQL 唯一索引 + `DuplicateKeyException` 降级，逻辑保留且良好 |
| 旧缺陷修复 | `productIdList` 越界（改为与 `items` 一一对应的 `resolution.productIds`）；订单明细持久化（`OrderItem` + `batchInsert`）；门店状态校验（`isStoreAvailable` 比对 `StoreStatusEnum.OPEN`）；`diningMethod` 已落库与校验 |

---

## 3. P0 —— 业务正确性与数据一致性（必须优先处理）

### 3.1 库存从未扣减，存在超卖风险

全局搜索 `deduct / stock / inventory / 库存 / 扣减` **无任何命中**。`OrderPlacementDomainService` 只做"可用性校验"：

```348:354:src/main/java/cn/dextea/trade/order/domain/service/OrderPlacementDomainService.java
private boolean isProductUnavailable(Product product, Integer storeStatus) {
    boolean globalOffShelf = product.getStatus() == null
            || product.getStatus() != ProductGlobalStatusEnum.ON_SHELF.getCode();
    boolean storeSoldOut = storeStatus == null
            || storeStatus != ProductStoreStatusEnum.AVAILABLE.getCode();
    return globalOffShelf || storeSoldOut;
}
```

这里只比对"是否售罄/下架"的状态码，**从不扣减实际库存数量**。旧文档指出的「未扣减库存」问题在重构后**依然未解决**，且当前校验只判断状态枚举（0/1），并不校验真实可售数量，无法支撑"限量商品"场景。

**建议**：
- 在订单域新增库存防腐端口 `InventoryPort`（或复用 catalog 提供的库存能力），通过领域事件 `OrderCreated` 触发扣减；
- 库存不足时，将其纳入 `preBuild` 的"不可用清单"返回，与现有优雅降级路径一致；
- 配套补偿：支付关闭/退款时回补库存。

### 3.2 `createOrder` 缺乏事务边界，且同步耦合支付宝

`OrderCommandServiceImpl.createOrder` **没有 `@Transactional`**（全工程唯一的 `@Transactional` 在 `OrderStatusDomainService.changeStatus`）。流程为：

```103:155:src/main/java/cn/dextea/trade/order/application/impl/OrderCommandServiceImpl.java
Order order = Order.builder()...build();
orderRepository.save(order);          // ① 插入订单头 + 明细
...
String tradeNo = paymentClientPort.createPayment(...);  // ② 外部 HTTP 调用支付宝
order.setTradeNo(tradeNo);
orderRepository.updateTradeNo(order.getId(), tradeNo);  // ③ 回填
```

风险点：
- **① 无事务**：`OrderRepositoryImpl.save` 先 `insert(order)` 再 `batchInsert(items)`，两步不在同一事务，明细插入失败会留下**无明细的订单头**。
- **② 同步外部调用 + 无补偿**：若支付宝调用超时/失败，会留下 `TRADE_WAIT_PAY` 且 `trade_no=null` 的"悬空订单"，当前**没有任何定时对账/补偿任务**能恢复它（旧文档曾表扬"支付宝与订单创建解耦"，但新代码实际已把它**重新同步耦合**进下单主链路）。

**建议**：
- 为 `save` 增加事务边界（`@Transactional(rollbackFor = Exception.class)` 或编程式事务），保证订单头与明细原子落库；
- 将"支付创建"从 `createOrder` 主链路剥离：订单先落库（`WAIT_PAY`），支付改为独立步骤/事件驱动；支付宝失败只影响支付，不影响订单创建；
- 增加悬空订单的定时对账（按 `trade_no is null` 且超时未支付扫描重试或关闭）。

### 3.3 支付状态机存在"死状态"与语义混淆

`TradeStatusEnum` 定义了 `TRADE_REFUNDING(4,"退款中")`，但：

- `OrderEventEnum` 中**没有任何事件**会导向 `TRADE_REFUNDING`；
- `OrderStatusMachine` 的转移表中**无到达 `TRADE_REFUNDING` 的条目** → 该状态永远不可达（死状态）。

然而 `OrderPaymentSyncAdapter.syncClosed` 的 `isClosedTerminal` 却把它当终态判断：

```98:104:src/main/java/cn/dextea/trade/order/infrastructure/adapter/OrderPaymentSyncAdapter.java
private static boolean isClosedTerminal(Integer status) {
    return isStatus(status, TradeStatusEnum.TRADE_CLOSED, TradeStatusEnum.TRADE_REFUNDED, TradeStatusEnum.TRADE_REFUNDING);
}
```

由于 `TRADE_REFUNDING` 永不可达，这段判断形同虚设。

同时 `syncClosed` 的语义也有问题：已支付/已结算订单收到"关闭"回调时，一律发 `REFUND` 事件 → `TRADE_REFUNDED`，把**"交易关闭（未退款）"与"退款完成"**混为一谈，未区分支付宝"交易关闭"与"退款成功"两种不同业务含义。

**建议**：补齐 `REFUNDING` 的转移与对应事件（如 `REFUNDING` 事件），或删除该死状态；在状态机中显式区分 `CLOSE`（未退款关闭）与 `REFUND`（已退款），避免状态语义被静默合并。

### 3.4 旧文档"枚举语义错误"仍未纠正

旧文档指出的 `isOptionUnavailable` 用全局维度枚举比对门店维度状态的问题，在重构后的新代码中**依然存在**：

```356:362:src/main/java/cn/dextea/trade/order/domain/service/OrderPlacementDomainService.java
private boolean isOptionUnavailable(CustomizationOption option, Integer storeStatus) {
    boolean globalDisabled = option.getStatus() == null
            || option.getStatus() != CustomizationOptionGlobalStatusEnum.ACTIVE.getCode();
    boolean storeDisabled = storeStatus == null
            || storeStatus != CustomizationOptionGlobalStatusEnum.ACTIVE.getCode();  // ⚠️ storeStatus 是门店维度，不应比对全局维度枚举
    return globalDisabled || storeDisabled;
}
```

`storeStatus` 来自 `customization_option_store_status`（门店维度），应比对门店维度枚举而非 `CustomizationOptionGlobalStatusEnum.ACTIVE`。当前因两者取值恰好都是 `1` 而未出 bug，但属语义错误，后续维护极易踩坑。

**建议**：为门店维度的客制化选项状态新增独立枚举（如 `CustomizationOptionStoreStatusEnum`），与全局枚举区分。

---

## 4. P1 —— 战术 DDD 模式落地（让领域真正"富"起来）

### 4.1 聚合根 `Order` 是贫血模型

`Order`、`OrderItem`、`OrderStatusLog` 全部是 `@Data @Builder` 纯数据载体，没有构造约束、没有行为、不保护不变式。所有逻辑都在 `OrderPlacementDomainService` / `OrderStatusDomainService` 中，状态机是静态工具类，由 service 去"改" `Order` 的普通字段。

这本质上是「**领域服务 + 贫血实体**」模式，而非富领域模型。

**建议**：把不变式与行为收归聚合根内部，例如：

```java
// 期望形态（示意）
public class Order {
    public void apply(OrderEventEnum event, String operator, ...) {
        TradeStatusEnum target = OrderStatusMachine.getTarget(this.status, event);
        if (target == null) throw new BizError(...);
        this.tradeStatus = target.getCode();
        this.version++;            // 由聚合自管版本
    }
    public boolean canTransition(OrderEventEnum event) { ... }
    public void markPaid(String tradeNo, LocalDateTime paidAt) { ... }
}
```

领域服务只负责「加载聚合 → 调用聚合行为 → 持久化」，不再直接操作字段。这样不变式随聚合走，移植性、可测试性都更好。

### 4.2 防腐层（ACL）名不副实：订单上下文直接依赖 catalog 领域模型

这是当前架构最值得警惕的问题。订单域的三个"防腐端口"的返回类型，**直接就是商品目录的领域模型**：

```3:10:src/main/java/cn/dextea/trade/order/domain/port/ProductCatalogPort.java
import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
...
List<Product> findProducts(List<Long> ids);   // 返回的是 catalog 的 Product
```

`CustomerPort` 返回 `catalog.domain.model.Customer`，`StorePort` 返回 `catalog.domain.model.Store`。进而导致 `OrderPlacementDomainService` 与 `OrderQueryServiceImpl` 直接 `import cn.dextea.trade.catalog.domain.model.*` 并使用 catalog 的 `Product/Customization/CustomizationOption/Store/Gallery/Customer`。

**后果**：订单限界上下文没有自己的"通用语言"，catalog 模型任意改动（重命名字段、改构造器）都会穿透影响订单域——这正是限界上下文 + ACL 要解决的问题，目前被绕过了。所谓"端口"退化为"catalog 的透传"。

**建议**：在订单上下文定义本地快照值对象（如 `ProductSnapshot`、`StoreSnapshot`、`BuyerSnapshot`、`CustomizationOptionSnapshot`），端口契约只暴露订单真正需要的字段；由 `ProductCatalogAdapter` 等适配器在基础设施层完成"catalog 模型 → 订单快照"的翻译。这样 catalog 与 order 的演变互相隔离。

### 4.3 `making_status`（制作进度）维度从未被驱动

`MakingStatusEnum`（WAIT→DOING→DONE→DELIVERED）只在订单创建时置为 `MAKING_WAIT`：

```110:111:src/main/java/cn/dextea/trade/order/application/impl/OrderCommandServiceImpl.java
.makingStatus(MakingStatusEnum.MAKING_WAIT.getCode())
.payMethod(command.getPlatform().getCode())
```

全局搜索 `setMakingStatus` / `MakingStatusEnum` 的"流转"使用：**除了创建与查询展示，没有任何状态机、没有更新方法**。而 `updateStatusCas` 只动 `trade_status` 与 `version`：

```59:63:src/main/java/cn/dextea/trade/order/infrastructure/persistence/OrderRepositoryImpl.java
public int updateStatusCas(String orderNo, int targetStatus, int expectedStatus, int currentVersion,
                           String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt) {
    return orderMapper.updateStatusCas(orderNo, targetStatus, expectedStatus, currentVersion, tradeNo, paidAt, refundedAt);
}
```

更隐蔽的问题：**`trade_status` 与 `making_status` 共用同一 `version` 列**。一旦将来为制作进度加状态流转，支付状态更新与制作状态更新会在版本号上互相冲突（CAS 互相失败 → 丢失更新）。

**建议**：
- 若制作进度是真实业务（门店出杯流程），应补齐 `MakingStatusMachine`、独立更新方法与版本策略（如双版本号或分离聚合）；
- 若暂不需要，应移除该字段，避免"定义了但永远不变"的误导。

### 4.4 缺领域事件机制，副作用只能硬编码/缺失

订单创建、状态变更后，没有发布 `OrderCreated` / `OrderPaid` / `OrderRefunded` 等**领域事件**。这直接导致：
- 库存扣减无触发点（呼应 3.1）；
- 通知、对账、埋点等副作用无统一扩展点。

**建议**：引入领域事件发布端口（或 Spring `ApplicationEvent` / MQ 集成事件）。下单成功后发布 `OrderCreated`，由库存订阅者扣减、通知订阅者推送；支付回调发布 `OrderPaid`，进一步解耦主流程与副作用。

---

## 5. P2 —— 限界上下文与架构层面

### 5.1 `order` 与 `pay` 上下文双向耦合，契约归属错乱

当前存在跨上下文依赖：
- `OrderCommandServiceImpl` 直接 `import cn.dextea.trade.pay.domain.model.PlatformEnum` 与 `pay.domain.exception.PayErrorCode`（order → pay 编译依赖）；
- 订单的 `PaymentClientPort` 由订单基础设施适配到 pay 的 `PaymentService`（order → pay 运行依赖）；
- `OrderPaymentSyncAdapter`（位于 **order 模块**）却实现了 `pay.domain.port.PaymentResultSyncPort`（**pay 拥有的契约**）。

问题：支付结果接收契约由 **pay 上下文定义**、**order 上下文实现**，契约归属倒置；且 order 编译期依赖 pay，形成上下文间的循环耦合倾向。

**建议**：
- 将"支付结果接收"契约归到订单上下文（如 `OrderStatusUpdatePort` / `ReceivePaymentResultPort`，放在 `order.domain.port`），由 pay 调用，使依赖方向单向、契约归属清晰；
- 长期可演进为「订单发布 `OrderCreated` 集成事件 → pay 订阅并创建支付 → pay 发布 `PaymentResult` 集成事件 → order 订阅更新状态」的事件驱动上下文映射，彻底斩断编译期循环。

### 5.2 `OrderRepository` 命令/查询职责混合（未做 CQRS 分离）

`OrderRepository` 同时承担写与读：写侧有 `save / updateTradeNo / updateStatusCas / insertStatusLog`；读侧有 `findByOrderNo / findById / findByIdempotencyKey / findByCustomerIdAndCreatedAfter / findItemsByOrderIds / findFullItemsByOrderId`。而读侧（列表/详情视图）还需跨 catalog 取封面 URL、客制化名（`OrderQueryServiceImpl` 直接调 `productCatalogPort.findGalleries` 等），聚合因此变得臃肿。

**建议**：将读模型拆为独立的 `OrderViewRepository` 或 `OrderQueryRepository`，写侧只保留聚合持久化，使订单写聚合更纯粹，也便于未来读模型走独立存储/投影。

---

## 6. P3 —— 工程化与质量

### 6.1 聚合再水合（rehydration）不完整

`findByOrderNo` 等返回的 `Order` 其 `items` 为 `null`（明细由查询服务另行 `findFullItemsByOrderId` 加载）。聚合根未保证内部集合一致性，业务代码若误用空 `items` 可能 NPE。

**建议**：repository 内统一提供"完整聚合"加载方法（创建/变更场景加载 items），查询场景走独立的读模型，避免同一 `Order` 类型在不同场景语义不一致。

### 6.2 幂等缓存与序列化泄露到应用层

`OrderCommandServiceImpl` 直接用 `StringRedisTemplate` + `ObjectMapper` 做结果缓存：

```198:218:src/main/java/cn/dextea/trade/order/application/impl/OrderCommandServiceImpl.java
private OrderCreateResult getCachedResult(String redisKey) { ... redisTemplate.opsForValue().get(redisKey) ... }
private void cacheResult(String redisKey, OrderCreateResult result) { ... redisTemplate.opsForValue().set(...) ... }
```

**建议**：抽象为 `OrderResultCachePort`（与现有 ACL 思路一致），把 Redis 细节挡在应用层之外，便于单测替换与未来更换缓存介质。

### 6.3 领域层对 Spring 框架的耦合

`OrderPlacementDomainService`、`OrderStatusDomainService` 使用了 `@Service`、`@Transactional`、`@Slf4j`；`@Transactional` 甚至直接标注在领域服务方法上（见 3.2），意味着**事务边界由领域层决定，而本应负责编排的应用层反而没有显式事务**。

**建议**：严格六边形可让领域层框架无关（去掉 Spring 注解，领域服务作为普通 bean 由装配层注入）；至少应将事务边界明确上移到应用编排层（`@Transactional` 放在 `OrderCommandService` / `OrderQueryService` 的编排方法上），领域层只表达业务规则。

### 6.4 缺乏测试

`src/test/java` 当前为空。对 `OrderStatusMachine`（状态转移正确性）、`OrderPlacementDomainService`（定价/校验规则）、`createOrder` / `changeStatus`（事务与并发）均**没有单元测试与集成测试**，DDD 重构的回归保障缺失。

**建议**：优先补三类测试——状态机转移表、预构建校验（含不可用清单、枚举语义）、下单与状态变更的端到端（可用 Mock 端口隔离 infra）。

### 6.5 查询时间窗硬编码

`OrderQueryServiceImpl.getOrdersByCustomer` 把"近 3 个月"硬编码为 `LocalDateTime.now().minusMonths(3)`。

**建议**：作为查询参数或领域规则外置，避免在应用服务里埋业务常量。

---

## 7. 改造清单（优先级总览）

| 优先级 | 编号 | 问题 | 现状位置 | 建议 |
|--------|------|------|----------|------|
| P0 | 3.1 | 库存从未扣减，超卖风险 | `OrderPlacementDomainService` | 引入 `InventoryPort` + `OrderCreated` 事件扣减；不足时纳入不可用清单 |
| P0 | 3.2 | `createOrder` 无事务 + 同步耦合支付宝 | `OrderCommandServiceImpl.createOrder` | `save` 加事务；支付创建异步/独立；增加悬空订单对账 |
| P0 | 3.3 | 状态机有死状态 `TRADE_REFUNDING`、close/refund 语义混淆 | `TradeStatusEnum` / `OrderStatusMachine` / `OrderPaymentSyncAdapter` | 补齐转移或删死状态；区分关闭与退款 |
| P0 | 3.4 | `isOptionUnavailable` 用全局枚举比对门店状态 | `OrderPlacementDomainService:356` | 新增门店维度枚举并替换 |
| P1 | 4.1 | 聚合根 `Order` 贫血 | `Order` / `*DomainService` | 行为收归聚合（`apply/canTransition/markPaid`） |
| P1 | 4.2 | ACL 透传 catalog 领域模型 | `ProductCatalogPort` / `CustomerPort` / `StorePort` | 定义订单本地快照值对象 + 适配器翻译 |
| P1 | 4.3 | `making_status` 永不变更，且与支付状态共用 version | `MakingStatusEnum` / `updateStatusCas` | 补齐制作状态机与版本策略，或移除字段 |
| P1 | 4.4 | 无领域事件，副作用缺失 | 全局 | 引入领域/集成事件发布端口 |
| P2 | 5.1 | order 与 pay 双向耦合、契约归属倒置 | `OrderPaymentSyncAdapter` / `OrderCommandServiceImpl` | 支付结果契约归 order；事件驱动解耦 |
| P2 | 5.2 | Repository 命令/查询混合 | `OrderRepository` | 拆出 `OrderViewRepository` 读模型 |
| P3 | 6.1 | 聚合再水合不完整 | `OrderRepository` / `OrderQueryServiceImpl` | 提供完整聚合加载；查询走独立读模型 |
| P3 | 6.2 | 幂等缓存泄露到应用层 | `OrderCommandServiceImpl` | 抽象 `OrderResultCachePort` |
| P3 | 6.3 | 领域层耦合 Spring / 事务边界错配 | `*DomainService` | 事务上移应用层；领域层去框架注解 |
| P3 | 6.4 | 无测试 | `src/test/java`（空） | 补状态机/校验/流程测试 |
| P3 | 6.5 | 查询时间窗硬编码 | `OrderQueryServiceImpl:53` | 参数化或领域规则外置 |

---

## 8. 建议的演进路线

1. **第一阶段（止血，P0）**：先解决库存扣减、下单事务边界、悬空订单对账、状态机死状态与枚举语义错误。这些直接影响资金与数据正确性，应优先于任何"美观性"重构。
2. **第二阶段（富领域，P1）**：把行为收归聚合根、建立真正的 ACL 快照翻译、补齐制作状态维度、引入领域事件。这一阶段的收益是订单域开始拥有自己的通用语言与不变式保护。
3. **第三阶段（上下文治理，P2/P3）**：理顺 order 与 pay 的上下文映射、做 CQRS 读模型拆分、补测试与基础设施端口抽象，使架构在长期演进中保持低耦合、可测试。

> 说明：本报告基于 `refactor/order-domain-model` 分支当前源码静态分析，未执行运行时验证。涉及事务与并发的结论（3.2、4.3）建议结合压测与集成测试进一步确认。
