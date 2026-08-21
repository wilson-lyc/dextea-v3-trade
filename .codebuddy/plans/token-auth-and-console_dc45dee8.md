---
name: token-auth-and-console
overview: 为 dextea-trade 中台增加基于 MySQL 存储 Token 的请求鉴权机制：通过自定义拦截器对所有业务请求校验 Authorization Token，配置开关可启用/关闭鉴权；提供独立控制台登录（账号密码来自配置）及会话，并内置简易 HTML 页面用于维护 Token（增删改查、启用/过期）。
design:
  architecture:
    framework: html
  styleKeywords:
    - 后台管理
    - 简洁实用
    - 卡片布局
    - 内网中台
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 22px
      weight: 600
    subheading:
      size: 16px
      weight: 500
    body:
      size: 14px
      weight: 400
  colorSystem:
    primary:
      - "#1F6FEB"
      - "#2563EB"
    background:
      - "#0F172A"
      - "#F8FAFC"
      - "#FFFFFF"
    text:
      - "#1E293B"
      - "#E2E8F0"
    functional:
      - "#16A34A"
      - "#DC2626"
      - "#F59E0B"
todos:
  - id: add-config-and-ddl
    content: 新增 auth 配置项与 api_tokens 建表 DDL 脚本
    status: completed
  - id: add-errorcode-and-config
    content: 新增 AuthErrorCode 与 AuthConfig 配置属性类
    status: completed
  - id: add-po-and-mapper
    content: 新增 ApiTokenPO 与 ApiTokenMapper
    status: completed
    dependencies:
      - add-config-and-ddl
  - id: add-application-usecase
    content: 实现 ConsoleLoginUseCase 与 TokenManageUseCase 及 command/result
    status: completed
    dependencies:
      - add-po-and-mapper
  - id: add-interceptor-and-config
    content: 实现 AuthInterceptor 与 WebMvcConfig 注册及白名单
    status: completed
    dependencies:
      - add-errorcode-and-config
  - id: add-console-controller
    content: 实现 ConsoleController 及 SpringDoc 扫描配置
    status: completed
    dependencies:
      - add-application-usecase
      - add-interceptor-and-config
  - id: add-console-page
    content: 内置 /console/index.html 控制台页面
    status: completed
    dependencies:
      - add-console-controller
---

## 产品概述
为 dextea-trade 交易中台增加统一 Token 鉴权机制与 Token 管理控制台。所有业务请求须携带有效 Token，可通过配置开启或关闭鉴权；控制台通过配置的账号密码登录后维护（增删改查）Token。Token 持久化于 MySQL。

## 核心功能
- 请求鉴权：自定义拦截器校验每个业务请求的 Authorization Token，失效/过期/禁用时拒绝访问
- 鉴权开关：通过配置项启用或关闭鉴权；关闭时系统行为与现状一致，不破坏现有接口
- 控制台登录：独立登录接口，使用配置中的账号密码校验，成功后发放控制台会话 Token（存 Redis）
- Token 维护网页：内置简易 HTML 页面（/console/index.html），登录后可对 Token 进行增删改查、启停、查看过期时间
- Token 存储：MySQL 表存储明文 Token 及名称、启用状态、过期时间、创建/更新时间
- 白名单：放行控制台、登录接口、Swagger 文档、Actuator 健康等端点


## 技术栈
- 沿用现有栈：Spring Boot 3.5.16、Java 21、MyBatis 3.0.4（注解方式 SQL）、MySQL、Redis、Lombok
- 前端：内置静态 HTML + 原生 JS（无独立前端工程），由 Spring Boot 托管于 /console/**
- 不引入 Spring Security，使用 HandlerInterceptor 实现鉴权，与现有轻量 DDD 风格一致

## 实现方案
### 总体策略
在 interface 层新增拦截器与 WebMvc 配置实现统一鉴权；在 infrastructure 层新增 Token 的 PO 与 Mapper（MySQL 注解 SQL）；在 application 层封装控制台登录与 Token 管理用例；在 interface 层新增 ConsoleController 暴露 REST 接口并托管静态页面；配置项控制鉴权总开关与控制台账号密码。

### 关键技术决策
- **自定义拦截器而非 Spring Security**：项目现有鉴权逻辑缺失、风格轻量，HandlerInterceptor 可直接复用统一 APIResponse/错误码体系，避免引入重型依赖与配置迁移成本。
- **Token 查库校验 + 轻量缓存**：每次请求按 token 查 MySQL 判断存在、启用、未过期。为降低 DB 压力，可用 Redis 缓存 token 有效性（短 TTL，失效变更时清理）；白名单与开关在 WebMvcConfig 注册时决定拦截范围。
- **控制台会话 Token 存 Redis**：登录成功生成随机 session token 写入 Redis（带 TTL，可刷新），控制台接口校验该 session；复用项目已有的 spring-boot-starter-data-redis。
- **MySQL 存明文 Token**：token 值由系统随机生成（如 UUID/随机字符串），使用方首次创建可见一次；表含 name、enabled、expire_at 便于运维管理。
- **DB 初始化**：新增 DDL 脚本放置于 resources，启动时手动或运维执行建表（与现有无自动建表风格一致，不引入 Flyway/Liquibase）。

### 性能与可靠性
- 拦截器为热路径：校验走 Redis 缓存优先，缓存未命中回源 MySQL；token 失效/删除时主动清除缓存，避免脏读。复杂度 O(1) 命中缓存。
- 白名单在配置期确定，不进入业务校验逻辑，避免无谓 DB 查询。
- 登录失败与 token 校验失败复用 GlobalExceptionHandler，统一返回 APIResponse；不泄露明文密码，日志中禁止打印密码与 token 明文（控制台页面也仅展示创建时刻的 token）。

### 执行注意事项
- 必须保证 `auth.enabled=false` 时行为与现状完全一致（拦截器不注册或提前放行），不影响现有订单接口。
- 新 ConsoleController 需加入 SpringDoc 扫描范围（修改 application.yaml 的 springdoc.packages-to-scan 或新增扫描项），否则 Swagger 不可见。
- 新增错误码（token 无效/过期/禁用、控制台未登录、控制台账号密码错误）放入 shared.error，复用 BizError 抛出。
- 遵循项目规则：不写代码注释。

## 架构设计
### 数据流
业务请求 → AuthInterceptor（开关校验→白名单→取 Authorization→Redis 缓存→MySQL 校验）→ 业务 Controller
控制台请求 → ConsoleController：/console/login（校验配置账号密码→Redis 存 session）→ /console/tokens/**（校验 session→ApiTokenUseCase→ApiTokenMapper→MySQL）

### 模块关系
- shared：新增鉴权错误码、AuthConfig 配置属性
- interfaces.http（新增 console 子包）：ConsoleController、AuthInterceptor、WebMvcConfig、静态页面
- application（新增 console 子包）：ConsoleLoginUseCase、TokenManageUseCase、command/result
- infrastructure.persistence（新增 console 子包）：ApiTokenPO、ApiTokenMapper

## 目录结构与文件
```
src/main/resources/
├── application.yaml                         # [MODIFY] 新增 auth.enabled / auth.console.username / auth.console.password 配置项（环境变量可覆盖），扩展 springdoc.packages-to-scan
├── db/token-schema.sql                       # [NEW] 建表 DDL：api_tokens(id, token, name, enabled, expire_at, created_at, updated_at)
└── static/console/index.html                # [NEW] 内置控制台页面：登录表单 + Token 列表 CRUD 简易 UI，调用 /console/** 接口

src/main/java/cn/dextea/trade/
├── shared/
│   ├── error/
│   │   └── AuthErrorCode.java                # [NEW] 鉴权相关错误码（token 无效/过期/禁用、控制台未登录、账号密码错误），实现 BizErrorCode
│   └── config/
│       └── AuthConfig.java                   # [NEW] @ConfigurationProperties 绑定 auth.* 配置（enabled、console.username、console.password）
└── console/
    ├── interfaces/http/
    │   ├── controller/
    │   │   └── ConsoleController.java         # [NEW] REST 接口：登录、登出、Token 分页/列表、创建、更新、删除、启停；@RestController+@Tag+@RequiredArgsConstructor
    │   ├── AuthInterceptor.java               # [NEW] HandlerInterceptor：取 Authorization 头→缓存/库校验；开关关闭则放行
    │   └── WebMvcConfig.java                  # [NEW] 注册 AuthInterceptor，按开关决定是否拦截，配置白名单（/console/**、登录、swagger、actuator）
    ├── application/
    │   ├── uscase/
    │   │   ├── ConsoleLoginUseCase.java       # [NEW] 校验配置账号密码→生成 session token 存 Redis
    │   │   └── TokenManageUseCase.java        # [NEW] Token 增删改查、启停业务逻辑，维护缓存一致性
    │   ├── dto/command/
    │   │   ├── ConsoleLoginCommand.java       # [NEW] 登录命令（username、password）
    │   │   └── SaveApiTokenCommand.java       # [NEW] 创建/更新 Token 命令（name、expireAt、enabled）
    │   └── dto/result/
    │       ├── ConsoleLoginResult.java        # [NEW] 返回控制台 session token
    │       └── ApiTokenResult.java            # [NEW] Token 视图（不含明文 token 列表展示用，创建时返回一次明文）
    └── infrastructure/persistence/
        ├── po/
        │   └── ApiTokenPO.java                # [NEW] api_tokens 表映射 PO（Lombok @Data）
        └── mapper/
            └── ApiTokenMapper.java            # [NEW] @Mapper：selectByToken、insert、list、updateEnabled、updateExpire、deleteById、selectById
```

## 关键代码结构
```java
public interface ApiTokenMapper {
    @Select("SELECT * FROM api_tokens WHERE token = #{token}")
    ApiTokenPO selectByToken(@Param("token") String token);

    @Insert("INSERT INTO api_tokens(token,name,enabled,expire_at,created_at,updated_at) "
          + "VALUES(#{token},#{name},#{enabled},#{expireAt},NOW(),NOW())")
    int insert(ApiTokenPO po);

    @Select("SELECT * FROM api_tokens ORDER BY created_at DESC")
    List<ApiTokenPO> list();

    @Update("UPDATE api_tokens SET enabled=#{enabled},updated_at=NOW() WHERE id=#{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    @Delete("DELETE FROM api_tokens WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
}
```


## 设计风格
内置控制台页面采用简洁实用的后台管理风格，配合项目内网中台的定位。页面置于 /console/index.html，使用原生 HTML + CSS + JS，无需构建工具。整体为单页结构：顶部为登录区（未登录时展示），登录成功后切换为 Token 管理区。

## 页面区块（自上而下）
- 顶部标题栏：显示「dextea-trade 控制台」标题与登录状态/登出按钮，深色背景浅色文字。
- 登录区块：账号、密码输入框与登录按钮；登录失败显示错误提示。
- Token 列表区块：表格展示 Token 名称、状态（启用/禁用）、过期时间、创建时间；提供刷新按钮。
- 新建/编辑区块：名称、过期时间（可选）、启用开关、提交按钮；新建成功时弹窗展示一次性明文 Token。
- 行操作区块：每条记录提供启用/禁用、删除按钮，删除前二次确认。

## 交互说明
- 登录后将会话 Token 存入 localStorage，后续请求通过 Authorization 头（Bearer）携带。
- 所有操作通过 fetch 调用 /console/** 接口，统一解析 APIResponse。
- 页面响应式，桌面端居中卡片布局，移动端纵向自适应。
