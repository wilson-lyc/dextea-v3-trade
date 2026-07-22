# 文档导航

本目录汇集 `dextea-trade`（德贤茶庄线上点餐系统 · 交易端后台）的部署与配置文档。各文档之间已通过链接相互跳转，可从一个主题快速到达相关主题。

## 文档地图

```
README（根目录）
  └── 项目介绍 + 快速部署入口
        │
        ├── docs/Deployment.md  ──────────► 怎么把服务跑起来
        │        │
        │        ├── docs/Configuration-Parameters.md  ──► 有哪些配置可配
        │        │        │
        │        │        ├── docs/Environment-Variables.md  ──► 用环境变量怎么配
        │        │        │
        │        │        └── docs/Nacos.md  ──────► 用 Nacos 怎么配
        │        │
        │        └── （健康检查 / 回滚 等运维要点）
        │
        └── docs/Order-Creation-Flow-Analysis.md  ──► 业务流程与已知问题
```

## 建议阅读顺序

1. **先看总览**：[Deployment Guide](Deployment.md) —— 了解整体部署形态与两种配置来源。
2. **再看参数**：[Configuration Parameters](Configuration-Parameters.md) —— 一张表掌握所有可配置项及其默认值。
3. **按场景深入**：
   - 走环境变量 / 容器化部署 → [Environment Variables](Environment-Variables.md)
   - 走配置中心统一管理 → [Nacos Configuration](Nacos.md)

## 文档清单

| 文档 | 适合谁看 | 关键内容 |
|------|----------|----------|
| [Deployment Guide](Deployment.md) | 运维 / 开发者 | 构建、启动、两种配置来源、健康检查、回滚 |
| [Configuration Parameters](Configuration-Parameters.md) | 所有人 | 按组件分类的全量参数表、默认值、必填性 |
| [Environment Variables](Environment-Variables.md) | 容器化部署者 | 命名规则、完整环境变量清单、最小可运行示例 |
| [Nacos Configuration](Nacos.md) | 配置中心管理者 | 接入方式、Data ID、配置示例、与环境变量优先级 |
| [Order Creation Flow Analysis](Order-Creation-Flow-Analysis.md) | 开发者 | 下单链路、幂等设计、已知 Bug 与改进点 |

## 两种配置来源的关系

- **环境变量模式**是默认形态：所有参数都可通过 `${ENV}` 注入，无需任何配置文件或 Nacos 即可启动。
- **Nacos 模式**是可选增强：当配置 `NACOS_SERVER_ADDR` 后，服务会从 Nacos 拉取 `dextea-trade.yaml`；**Nacos 与本地/环境变量配置可叠加**——Nacos 下发的配置优先级高于环境变量，未下发的仍由环境变量兜底。

> 不确定某个参数该用哪种方式配？先到 [Configuration Parameters](Configuration-Parameters.md) 查它的「配置来源」与「默认值」。
