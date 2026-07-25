# 运行前准备（Prerequisites）

本页是运维工程师在**启动 `dextea-trade` 之前**必须完成的检查清单：确认依赖、准备配置文件、初始化数据库。完成本页后，再按 [Deployment Guide](Deployment.md) 构建与启动。

> 所有可调参数见 [Configuration Parameters](Configuration-Parameters.md)；用环境变量怎么填见 [Environment Variables](Environment-Variables.md)。

## 1. 依赖服务清单

启动前，以下外部依赖必须**已就绪且网络可达**：

| 依赖 | 版本要求 | 是否必需 | 说明 |
|------|----------|----------|------|
| JDK | 21 | ✅ | Spring Boot 3.5 硬性要求，确认 `java -version` 输出 21 |
| MySQL | 5.7+ / 8.x | ✅ | 业务库，需提前建库并初始化表结构（见 §3） |
| Redis | 5+ | ✅ | 幂等缓存 + CosId 机器号分配，缺失会导致启动或运行失败 |
| Nacos | 2.x | ❌ | 仅当使用 Nacos 配置中心时需要，默认关闭 |
| 支付宝开放平台应用 | — | ⚠️ 条件 | 需要支付宝支付时必填凭证；否则可留空，服务仍能启动 |

确认命令示例：

```bash
java -version          # 期望：21.x
mysql  -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p -e "SELECT 1"   # 能连通
redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping     # 期望：PONG
```

## 2. 创建配置文件（`.env`）

仓库提供 `.env.example`，列出**全部**支持的环境变量及安全默认值。

```bash
cp .env.example .env
# 编辑 .env，填入你自己的 DB_* / REDIS_* / ALIPAY_* 值
```

> ⚠️ **安全提醒**
> - 仓库根目录已存在的 `.env` 包含共享/开发环境的**真实密码与支付宝私钥**，请勿直接使用，应视为已泄露并尽快轮转（改密码、重置支付宝密钥）。
> - `.gitignore` 使用 `*.env` 通配符忽略所有 `.env` 文件，**包括 `.env.example`**。若你修改了 `.env.example` 想提交，需 `git add -f .env.example` 强制跟踪；否则它不会进版本库。
> - 运行时用 `java -jar app.jar --env-file=.env` 注入，避免密钥出现在命令行。

## 3. 数据库初始化（重要）

⚠️ **本仓库不附带任何 SQL 初始化脚本**，无法 `auto-ddl` 自动建表。运维需自行准备数据库 schema，否则服务启动后会在执行业务 SQL 时失败。

启动前请确认：

1. 已创建业务库（默认库名 `dextea`，由 `DB_NAME` 指定）。
2. 已建好业务表（订单、商品、客制化选项、门店状态等）。**表结构请从研发同学获取并纳入你们的迁移/变更管理流程**（如 Flyway、Liquibase 或手工 SQL）。
3. 库字符集建议使用 `utf8mb4`，时区 `Asia/Shanghai`（JDBC URL 已固定 `serverTimezone=Asia/Shanghai`）。

> 回滚提示：应用回滚时配置与代码分离、无需改配置，但**表结构需向前兼容**，回滚前请确认旧版本代码能适配当前 schema。

## 4. 配置来源选择

| 场景 | 推荐方式 |
|------|----------|
| 单实例 / 容器 / K8s / PaaS | 纯环境变量（`.env` 或编排工具注入），见 [Environment Variables](Environment-Variables.md) |
| 多实例 / 多环境统一管理、热更新 | Nacos 配置中心，见 [Nacos](Nacos.md) |
| 混合 | Nacos 下发 + 环境变量兜底（Nacos 优先级更高） |

## 5. 启动前快速自检

- [ ] JDK 为 21
- [ ] MySQL 可连通且库/表已初始化
- [ ] Redis 可连通（PONG）
- [ ] `.env` 已创建，含正确的 `DB_*` 与 `REDIS_*`；未用仓库里那份含真实密钥的 `.env`
- [ ] 需要支付宝时，`ALIPAY_APP_ID` / `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` 已填且私钥用双引号包裹
- [ ] **生产环境已清空 `ALIPAY_FORCE_AMOUNT`**（否则订单只扣 ¥0.01）
- [ ] 若需健康检查/探针，已通过 `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info` 暴露 actuator（见 [Deployment §5](Deployment.md)）

完成以上检查后，前往 [Deployment Guide](Deployment.md) 进行构建与启动。

---

Back to: [docs/README.md](README.md) ｜ Next: [Deployment Guide](Deployment.md)
