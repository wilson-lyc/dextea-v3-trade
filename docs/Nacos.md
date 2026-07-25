# Nacos 配置

`dextea-trade` 通过 `spring-alibaba-nacos-config` 支持**以 Nacos 作为配置中心**。设置 `NACOS_SERVER_ADDR` 后，服务在启动时会从 Nacos 拉取应用配置；未设置时，会经由 `optional:` 前缀静默跳过，**不影响本地/环境变量方式启动**。

> 关于环境变量写法，请查阅 [环境变量](Environment-Variables.md)；关于参数归属与默认值，请查阅 [配置参数](Configuration-Parameters.md)。

## 1. 启用

只需在环境（或启动参数）中设置 Nacos 地址即可启用：

```bash
export NACOS_SERVER_ADDR=127.0.0.1:8848
export NACOS_NAMESPACE=      # 命名空间 ID，为空 = public
export NACOS_USERNAME=       # 启用鉴权时填写
export NACOS_PASSWORD=
export NACOS_CONFIG_GROUP=DEFAULT_GROUP   # 配置分组
```

`application.yaml` 中已包含导入语句：

```yaml
spring:
  config:
    import:
      - optional:nacos:${spring.application.name}.yaml?group=${NACOS_CONFIG_GROUP:DEFAULT_GROUP}
```

- `optional:` 前缀确保**未配置 Nacos 时也不会报错**。
- 目标 Data ID 默认为应用名：`dextea-trade.yaml`。

## 2. Data ID 与格式

| 项目 | 取值 | 说明 |
|----|------|------|
| Data ID | `dextea-trade.yaml` | 由 `spring.application.name` + `.yaml` 构成 |
| Group | `DEFAULT_GROUP`（可通过 `NACOS_CONFIG_GROUP` 覆盖） | 配置分组 |
| Namespace | `NACOS_NAMESPACE`（为空 = public） | 环境隔离 |
| 格式 | YAML | 与本地 `application.yaml` 结构一致 |

> 针对多套环境（dev/test/prod），建议使用 **Namespace** 进行隔离，而非多个 Data ID，以避免应用名称冲突。

## 3. Nacos 配置示例

在 Nacos 控制台创建 `dextea-trade.yaml`；示例内容（覆盖数据库、Redis、支付宝等）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql.prod.svc:3306/dextea?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: dextea
    password: ${DB_PASSWORD}   # 敏感项仍可经环境变量注入，Nacos 中不存明文
  data:
    redis:
      host: redis.prod.svc
      port: 6379
      password: ${REDIS_PASSWORD}

cosid:
  namespace: dextea-trade

alipay:
  gateway: https://openapi.alipay.com
  app-id: 2021xxxxxxxxxxxx
  private-key: ${ALIPAY_PRIVATE_KEY}   # 多行私钥建议仍经环境变量/K8s Secret 注入
  public-key: ${ALIPAY_PUBLIC_KEY}
  subject: 德贤茶庄订单
  product-code: JSAPI_PAY
  force-amount:                        # 生产环境留空，使用真实订单金额
  notify-url: https://api.example.com/api/alipay/notify

mybatis:
  configuration:
    map-underscore-to-camel-case: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

要点：

- Nacos 配置与本地 `application.yaml` **结构一致**，可直接迁移（数据库 / Redis / CosId / MyBatis / Actuator / 支付宝 等）。
- **敏感信息（密码、私钥）应保留为 `${ENV}` 占位符**，通过环境变量或 K8s Secret 注入，从而 Nacos 不存储明文。
- 任何未由 Nacos 下发的参数都会回退到本地默认值或环境变量。

## 4. 与环境变量的优先级

配置优先级（高 → 低）：

```
启动命令行参数
  > Nacos 配置中心（远程）
    > 环境变量 / 系统属性
      > 本地 application.yaml 默认值
```

即：**Nacos 下发的配置会覆盖同名的环境变量与本地默认值**；Nacos 未下发的参数仍会回退到环境变量。这种“远程为主、环境兜底”的组合非常适合灰度发布与应急热修复（改 Nacos 无需重新发布）。

## 5. 调试与排错

| 现象 | 排查方向 |
|------|------|
| 启动日志中没有任何 Nacos 拉取 | 确认 `NACOS_SERVER_ADDR` 已设置；缺失时 `optional:` 会静默跳过 |
| 拉取报 403 / 鉴权失败 | 检查 `NACOS_USERNAME` / `NACOS_PASSWORD` 以及 Nacos 是否启用了鉴权 |
| 拉取报命名空间不存在 | 确认 `NACOS_NAMESPACE` 是命名空间的 **ID**（而非名称） |
| 配置未生效 | 核对 Data ID 为 `dextea-trade.yaml` 且 Group 与 `NACOS_CONFIG_GROUP` 一致 |
| 希望热刷新 | Nacos 变更默认支持动态刷新；应用能否实时响应取决于代码中 `@RefreshScope` / `@ConfigurationProperties` 的使用情况 |

## 6. 启用鉴权（生产环境推荐）

建议在 Nacos 2.x 服务端启用鉴权；客户端通过 `NACOS_USERNAME` / `NACOS_PASSWORD` 连接（见第 1 节）。结合 TLS 与网络隔离，可避免配置泄露。

---

返回：[部署指南](Deployment.md) ｜ 对比：[环境变量](Environment-Variables.md)
