# catalog 模块（商品目录域 · 支撑域）

> 注意：本模块在代码中的包名是 `catalog`（非 "catalogue"）。

## 角色定位

`catalog` 是**只读的支撑子域（supporting subdomain）**：它封装了商品、客制化、
图片、门店、顾客等**参考数据**的查询能力，但不包含下单、支付等核心交易逻辑。
其核心消费者是 `order` 域——订单在下单前校验与计价时，通过防腐端口
（`order/domain/port/ProductCatalogPort` 等）向本模块获取**只读快照**。

设计要点：**catalog 不依赖 order，order 也不直接依赖 catalog 的持久化实现**，
二者仅通过 catalog 暴露的 `CatalogQueryService` 接口 + order 侧 adapter 解耦。

## 目录结构

```
catalog/
├── domain/                    领域层
│   ├── enums/                状态枚举：门店/顾客/商品(全局&门店)/客制化(项目&选项) 状态（类型化，避免裸码外泄）
│   ├── model/                领域模型（9 个实体/值对象，内聚可用性行为：isOnShelf/isOpen/isActive/isAvailableInStore 等）
│   ├── repository/           CatalogRepository 只读仓储端口（领域层定义，基础设施实现）
│   ├── service/
│   │   ├── CatalogQueryService.java        只读查询服务接口（对外支撑契约）
│   │   └── impl/CatalogQueryServiceImpl.java 实现，依赖 CatalogRepository 端口（不直接依赖 Mapper）
│   └── （本模块无 application / interfaces 层——纯只读支撑数据，不对外暴露独立 HTTP 接口）
└── infrastructure/
    └── persistence/          持久化：9 个 MyBatis Mapper + CatalogRepositoryImpl（仓储端口实现）
```

## 领域模型一览（model）

| 模型 | 含义 |
|------|------|
| `Store` | 门店 |
| `Customer` | 顾客（含 `alipayOpenId`，支付绑卡用） |
| `Product` | 商品（含价格、全局上/下架状态） |
| `ProductStoreStatus` | 商品在某门店的售卖状态（售罄等） |
| `Customization` | 客制化项目（如「温度」「甜度」），可绑定到某个商品 |
| `CustomizationOption` | 客制化选项（如「热」「冰」），归属某个客制化项目 |
| `CustomizationOptionStoreStatus` | 选项在门店的可用状态 |
| `ProductImage` | 商品封面图关联 |
| `Gallery` | 图片库（存储真实 URL） |

## 对外契约：`CatalogQueryService`

订单域通过该接口批量获取参考数据（均为 `findXxxByIds` 风格的批量查询）。
对应实现 `CatalogQueryServiceImpl` 依赖领域层 `CatalogRepository` 端口（由
`infrastructure/persistence/CatalogRepositoryImpl` 调用各 Mapper 实现），
从而保持领域层不直接依赖基础设施。订单侧的 `order/infrastructure/adapter/CatalogAdapter`
把该接口适配成订单域统一的 `CatalogPort`（商品/客制化/门店/顾客共用同一端口）。

> 只读快照裁剪：`Store` / `Customer` 快照不含 `password` / `account` 等凭证字段，
> 仅保留下单前校验与支付绑卡所需信息，避免敏感数据进入订单上下文。

## 扩展指引

- 新增一类参考数据（如「套餐」）：在 `model` 加实体 → `persistence` 加 Mapper →
  在 `CatalogQueryService` 增加批量查询方法 → 在 order 侧新增/扩展对应 `Port` 与 `Adapter`。
- 状态枚举集中在 `domain/enums`，新增状态务必同步维护对应枚举，避免散落的魔法数字。
