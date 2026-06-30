# 实现计划：design-patterns-showcase（设计模式实战示例工程）

## Overview

本计划将 design.md 的技术方案转化为可由编码代理逐条执行的增量式任务，最终交付一个基于 **Spring Boot 2.7.18 + JDK 8 + MyBatis Starter 2.3.2 + H2 + Lombok + Hibernate Validator + JUnit 5/Mockito + jqwik 1.8.x** 的设计模式教学工程，覆盖 P0 核心 14 个 + P1 扩展 7 个，共 21 个 GoF 模式。

任务严格遵循「基础设施优先 → 按 GoF 三大类别逐模式分组实现 → 整体验证收尾」的顺序，划分为六个阶段：

- 阶段一（任务 1）：项目基础骨架（Maven 工程与 pom.xml、启动类、application.yml、schema/data.sql）。
- 阶段二（任务 2）：公共基础设施（统一返回、控制器基类、异常体系、全局异常处理器、简化权限机制、启动清单、响应码常量）。
- 阶段三（任务 3~8）：创建型模式（单例、工厂方法、抽象工厂、建造者、原型）。
- 阶段四（任务 9~16）：结构型模式（代理、适配器、装饰器、外观、桥接、组合、享元）。
- 阶段五（任务 17~26）：行为型模式（策略、模板方法、观察者、责任链、状态、命令、迭代器、中介者、备忘录）。
- 阶段六（任务 27~28）：跨模式属性测试与整体验证收尾。

每个模式作为一个顶级任务，子任务依次覆盖：角色类与核心服务实现 → Demo_Controller 与演示入口注册与 Pattern_Doc → 核心行为单元测试（可选）→ 对应正确性属性的属性测试（可选）。需持久化的模式（状态、命令、享元）额外先实现实体类、Mapper 接口与 Mapper XML。后续任务始终建立在前序任务产出之上，不留孤立代码。

### 全局编码规范基线（每个编码任务均须遵守，源自 design.md「编码规范约束」专节 C1~C12）

- C1/C3：全部产出与注释用简体中文；标识符用具业务语义的英文；每个方法（接口/实现/私有，无一例外）提供完整 Javadoc（说明 + 每参数 `@param` + 非 void 的 `@return`）。
- C2：坚决不使用任何 Swagger 注解；pom.xml 不引入 springfox/springdoc。
- C4：Demo_Controller 采用传统 URL 路径风格（动作语义命名，如 `/pattern/strategy/calculate`），拒绝 RESTful。
- C5：依赖注入统一 `@Resource`，禁止 `@Autowired`；协作对象一律以接口类型声明并注入。
- C6：Controller 仅做路由分发（接收参数→调 Service→返回结果），继承 `BaseController`，用 `@Validated` + Request 对象校验；禁止在 Controller 内写 if 校验、for 循环、try-catch、直接操作 Mapper。
- C7：新增/修改用 `@PostMapping`；删除用 `@GetMapping` 且必须携带非空删除标识 + `@RequiresPermission` 权限校验。
- C8：禁止内部类（仅实体类可按需含内部类），所有独立逻辑类提取为独立顶层文件。
- C9：每个方法只做一件事，复杂逻辑拆分子方法。
- C10：表达力优先，使用 `Boolean.FALSE.equals()`、`CollectionUtils.isEmpty()`、`Objects.equals()` 等惯用法。
- C11：MyBatis 的 SQL 必须写在 XML 映射文件中，禁止使用 MyBatis-Plus 的 Wrapper（QueryWrapper/LambdaQueryWrapper）。
- C12：实体/请求对象用中文字段 Javadoc，可用 Lombok `@Data`，Request 对象用 JSR-303 校验注解。

> 每个模式均须满足：至少 1 个可独立触发的演示入口（需求 11.1）、至少 1 个断言核心行为正常路径的单元测试（需求 11.4）、统一四段式 Pattern_Doc（需求 7.1：解决的问题 / 遵循的设计原则 / 优点与适用及不适用场景 / 参与角色与对应类）、至少 3 个相互协作且命名取自业务术语的类或接口（需求 6.2、6.3）。

## Tasks

### 阶段一 · 项目基础骨架

- [x] 1. 搭建 Maven 工程与项目基础骨架
  - [x] 1.1 创建 Maven 工程、pom.xml 与三大类别包结构
    - 创建 `pom.xml`，以确定版本声明全部依赖：`spring-boot-starter-parent` 2.7.18、`spring-boot-starter-web`、`spring-boot-starter-aop`、`spring-boot-starter-validation`、`mybatis-spring-boot-starter` 2.3.2、`com.h2database:h2`、Lombok、`spring-boot-starter-test`、`net.jqwik:jqwik` 1.8.x（test 作用域）
    - 坚决不引入任何 Swagger 依赖（springfox/springdoc），落实 C2
    - 创建顶层包 `com.example.patterns`，并按 GoF 三大类别建立 `creational`、`structural`、`behavioral` 子包及 `common` 基础设施包，落实需求 1.2、1.3 的分类结构
    - _Requirements: 1.1, 1.2, 1.3, 1.6_

  - [x] 1.2 创建 Spring Boot 启动类与 application.yml
    - 创建启动类 `PatternsShowcaseApplication`
    - 创建 `application.yml`：配置 H2 内存库数据源、MyBatis（mapper XML 扫描路径 `classpath*:mapper/**/*.xml`、下划线转驼峰、类型别名包）、SQL 初始化开关
    - _Requirements: 1.1, 1.4_

  - [x] 1.3 编写 schema.sql 与 data.sql 完成 H2 持久化基座
    - 编写 `schema.sql` 创建 `biz_order`、`biz_product`、`sys_command_history`、`risk_rule_dict` 四张表（字段依 design.md「表结构与实体设计」）
    - 编写 `data.sql` 初始化风控规则字典（`risk_rule_dict`）等演示数据，确保启动时自动建表并初始化
    - _Requirements: 9.8_

### 阶段二 · 公共基础设施（common）

- [x] 2. 实现公共基础设施层
  - [x] 2.1 实现响应码常量、统一响应与统一分页返回
    - 实现 `common.constant.HttpStatus` 响应码常量
    - 实现 `common.core.domain.AjaxResult`（success/error 系列静态工厂方法，error 支持自定义码+消息）
    - 实现 `common.core.domain.TableDataInfo<T>`（rows/total/code/msg 字段 + 泛型 `build` 方法），保证列表/分页返回类型安全
    - _Requirements: 11.2_

  - [x] 2.2 实现控制器基类 BaseController
    - 实现 `common.core.controller.BaseController`，提供 `success()`/`success(data)`/`error(msg)`/`toAjax(rows)`/`getDataTable(list)` 等统一返回封装，供各 Demo_Controller 继承，落实 C6
    - _Requirements: 9.4, 11.2_

  - [x] 2.3 实现统一异常体系
    - 实现 `ServiceException`（通用业务异常）、`IllegalStateTransitionException`（非法状态流转异常）、`PermissionDeniedException`（权限不足异常）三类独立顶层异常类
    - _Requirements: 6.6, 11.3_

  - [x] 2.4 实现全局异常处理器 GlobalExceptionHandler
    - 实现 `@RestControllerAdvice` 全局异常处理器，按「业务异常 → 非法状态流转 → 权限不足 → 参数校验 `MethodArgumentNotValidException` → 兜底 `Exception`」优先级转换为 `AjaxResult.error`，使 Controller 内无需 try-catch（落实 C6）
    - 兜底处理记录日志且不向调用方泄露堆栈细节
    - _Requirements: 1.7, 6.6, 11.3_

  - [x] 2.5 实现简化权限校验机制（替代 Spring Security）
    - 实现自定义注解 `RequiresPermission`（value 为所需权限标识，如 `pattern:product:remove`）
    - 实现 `LoginUser` 当前登录用户模型与 `MockUserContext`（mock 当前用户及权限集合，可切换有权/无权用户以演示通过与拒绝两条路径）
    - 实现 `PermissionAspect` 环绕切面：方法执行前比对注解所需权限与当前用户权限，无权限时抛 `PermissionDeniedException`
    - _Requirements: 9.10, 9.11, 10.2_

  - [x] 2.6 实现演示入口注册表与启动清单打印
    - 实现 `DemoEntryRegistry` 与入口贡献聚合机制（各模式各自提供独立的入口贡献 Bean，由注册表通过 Spring 注入自动聚合，避免集中文件被并发修改、符合开闭原则）
    - 实现 `DemoEntryPrinter` 监听 `ApplicationReadyEvent`，启动完成后向日志输出全部演示入口清单，每条至少含「设计模式名称 + 触发方式（HTTP 路径或单测标识）」
    - _Requirements: 1.5_

  - [ ]* 2.7 编写上下文加载冒烟测试
    - 用 `@SpringBootTest` 验证 Spring 应用上下文成功加载、应用进入可运行状态（冒烟测试）
    - _Requirements: 1.4_

### 阶段三 · 创建型模式（Creational）

- [ ] 3. 创建型 - 单例 Singleton（P0）
  - [x] 3.1 实现单例角色与核心服务
    - 实现 `GlobalConfigManager`（Spring 默认单例 Bean，getConfig/setConfig）与 `LocalCacheManager`（经典枚举/双重检查锁单例，getInstance/put/get/size），并提供承载二者对比逻辑的 Service
    - 同时体现 Spring 单例与经典单例两种实现方式差异（需求 2.1）；遵循 C3/C5/C8
    - _Requirements: 2.1, 2.2, 10.1_

  - [x] 3.2 实现单例 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/singleton/setConfig`、`GET /pattern/singleton/sameInstance` 返回两次获取是否引用相等），继承 BaseController、传统 URL 路径（C4/C6）
    - 向演示入口注册表贡献本模式入口；编写 `creational/singleton/doc/singleton.md`（统一四段式）
    - _Requirements: 2.1, 7.1, 9.2, 11.1_

  - [ ]* 3.3 编写单例核心行为单元测试
    - 断言同一入口多次获取返回同一实例（引用相等）的正常路径
    - _Requirements: 11.4_

- [x] 4. 创建型 - 工厂方法 Factory Method（P0）
  - [x] 4.1 实现支付处理器族与工厂
    - 实现 `PaymentProcessor` 接口与 `WechatPaymentProcessor`、`AlipayPaymentProcessor` 至少两种渠道实现
    - 实现 `PaymentProcessorFactory`：利用 Spring 注入的 `Map<String,PaymentProcessor>` 按渠道标识返回对应实现，未知渠道抛 `ServiceException` 且不创建任何实例
    - _Requirements: 2.3, 2.4, 10.4, 10.5_

  - [x] 4.2 实现工厂方法 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/factory/pay`，Request 对象含 channel 并 `@Validated` 校验）
    - 注册演示入口；编写 `creational/factorymethod/doc/factorymethod.md`
    - _Requirements: 2.3, 7.1, 9.2, 11.1_

  - [ ]* 4.3 编写工厂方法核心行为单元测试
    - 断言受支持渠道返回对应处理器的正常路径，并覆盖未知渠道返回错误且不创建实例的边界路径
    - _Requirements: 11.4, 2.4_

- [x] 5. 创建型 - 抽象工厂 Abstract Factory（P0）
  - [x] 5.1 实现多云对象存储产品族与抽象工厂
    - 实现 `CloudStorageFactory` 接口与 `AliyunStorageFactory`、`AwsStorageFactory` 至少两个产品族工厂
    - 实现产品接口 `StorageClient`、`UrlSigner` 及各厂商 4 个具体产品实现（`AliyunStorageClient`/`AliyunUrlSigner`/`AwsStorageClient`/`AwsUrlSigner`），保证同族产品相互关联一致
    - _Requirements: 2.5, 10.1_

  - [x] 5.2 实现抽象工厂 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/abstractfactory/upload`，Request 含 vendor、key）
    - 注册演示入口；编写 `creational/abstractfactory/doc/abstractfactory.md`
    - _Requirements: 2.5, 7.1, 9.2, 11.1_

  - [ ]* 5.3 编写抽象工厂同族一致性示例测试
    - 为每个云厂商工厂各一例，断言其创建的产品族归属同一 vendor、相互配套（示例测试，非属性测试）
    - _Requirements: 2.5, 11.4_

- [x] 6. 创建型 - 建造者 Builder（P0）
  - [x] 6.1 实现通知消息与建造者
    - 实现 `NotificationMessage`（含静态 `builder()`，必选「接收人」+ 可选「标题/正文/附件/优先级」）与 `NotificationMessageBuilder`（链式分步设置，`build()` 校验必选部件，缺失则抛 `ServiceException`）
    - _Requirements: 2.6_

  - [x] 6.2 实现建造者 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/builder/buildNotification`）
    - 注册演示入口；编写 `creational/builder/doc/builder.md`
    - _Requirements: 2.6, 7.1, 9.2, 11.1_

  - [ ]* 6.3 编写建造者核心行为单元测试
    - 断言分步设置后构建出包含已设置部件的完整对象
    - _Requirements: 11.4_

  - [ ]* 6.4 编写属性测试 Property 4（建造者构建保真）
    - **Property 4：建造者构建保真**
    - **Validates: Requirements 2.6**
    - 随机生成必选部件与可选部件子集组合，断言构建对象恰好包含已设置部件取值；必选部件缺失时构建被拒绝并报错
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 4: 建造者构建保真`
    - _Requirements: 2.6_
    - _Properties: 4_

- [ ] 7. 创建型 - 原型 Prototype（P1）
  - [ ] 7.1 实现营销活动模板原型与深拷贝
    - 实现 `CampaignTemplate`（含嵌套可变对象 `List<CampaignRule>`，实现 `deepClone()` 逐字段深拷贝）与 `CampaignRule`
    - _Requirements: 2.7, 2.8_

  - [ ] 7.2 实现原型 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/prototype/cloneTemplate`）
    - 注册演示入口；编写 `creational/prototype/doc/prototype.md`
    - _Requirements: 2.7, 7.1, 9.2, 11.1_

  - [ ]* 7.3 编写原型核心行为单元测试
    - 断言克隆副本各字段值与原型相等的正常路径
    - _Requirements: 11.4_

  - [ ]* 7.4 编写属性测试 Property 5（原型深拷贝独立性）
    - **Property 5：原型深拷贝独立性**
    - **Validates: Requirements 2.8**
    - 随机生成含嵌套集合的模板，断言深克隆副本各字段相等，且对副本（含嵌套对象）的任意修改不影响原型
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 5: 原型深拷贝独立性`
    - _Requirements: 2.8_
    - _Properties: 5_

- [ ] 8. 检查点 - 创建型模式收尾
  - 确保所有测试通过，如有疑问请询问用户。

### 阶段四 · 结构型模式（Structural）

- [x] 9. 结构型 - 代理 Proxy（P0）
  - [x] 9.1 实现报表查询接口与 AOP 缓存限流代理
    - 实现 `ReportQueryService` 接口与 `ReportQueryServiceImpl`（纯查询逻辑）
    - 实现 `CacheRateLimitAspect`（`@Aspect`，以 Spring AOP 充当代理织入缓存命中复用与限流，超阈值抛 `ServiceException`），在不修改目标代码前提下织入横切逻辑
    - _Requirements: 3.1, 10.2_

  - [x] 9.2 实现代理 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/proxy/queryReport`，可重复调用观察缓存、可高频调用观察限流）
    - 注册演示入口；编写 `structural/proxy/doc/proxy.md`
    - _Requirements: 3.1, 7.1, 9.2, 11.1_

  - [ ]* 9.3 编写代理核心行为单元测试
    - 断言缓存命中复用与限流拒绝的核心行为
    - _Requirements: 11.4_

  - [ ]* 9.4 编写属性测试 Property 6（代理缓存幂等一致性）
    - **Property 6：代理缓存幂等一致性**
    - **Validates: Requirements 3.1**
    - 随机查询参数（目标用 mock），断言缓存有效期内重复查询返回与首次一致的结果（同参幂等）
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 6: 代理缓存幂等一致性`
    - _Requirements: 3.1_
    - _Properties: 6_

  - [ ]* 9.5 编写代理 AOP 织入与限流集成测试
    - 用 `@SpringBootTest` 验证切面对目标方法生效、超阈值请求被拒（集成测试）
    - _Requirements: 10.2, 3.1_

- [x] 10. 结构型 - 适配器 Adapter（P0）
  - [x] 10.1 实现统一短信接口与多服务商适配器
    - 实现 `SmsSender` 目标接口；实现 `AliyunSmsClient`、`TencentSmsClient` 两个签名互不相同的 Adaptee（模拟第三方 SDK）；实现 `AliyunSmsAdapter`、`TencentSmsAdapter` 将统一接口适配至各 Adaptee
    - _Requirements: 3.2_

  - [x] 10.2 实现适配器 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/adapter/sendSms`，Request 含 vendor、phone、content）
    - 注册演示入口；编写 `structural/adapter/doc/adapter.md`
    - _Requirements: 3.2, 7.1, 9.2, 11.1_

  - [ ]* 10.3 编写适配器核心行为单元测试
    - 断言统一接口经适配器正确委派到对应服务商并返回成功
    - _Requirements: 11.4_

  - [ ]* 10.4 编写属性测试 Property 7（适配器统一接口委派）
    - **Property 7：适配器统一接口委派**
    - **Validates: Requirements 3.2**
    - 随机服务商标识与合法手机号/内容，断言请求被正确委派至对应适配器、内容无损传递且返回成功
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 7: 适配器统一接口委派`
    - _Requirements: 3.2_
    - _Properties: 7_

- [x] 11. 结构型 - 装饰器 Decorator（P0）
  - [x] 11.1 实现通知发送组件与装饰器链
    - 实现 `NotifySender` 组件接口、`BaseNotifySender` 具体组件、`NotifyDecorator` 抽象装饰器（持有 `NotifySender`，体现组合优于继承）、`SignatureDecorator`/`EncryptDecorator`/`LogDecorator` 至少 2 个可任意叠加的具体装饰
    - _Requirements: 3.3, 8.3_

  - [x] 11.2 实现装饰器 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/decorator/send`，Request 指定要叠加的能力列表）
    - 注册演示入口；编写 `structural/decorator/doc/decorator.md`
    - _Requirements: 3.3, 7.1, 9.2, 11.1_

  - [ ]* 11.3 编写装饰器核心行为单元测试
    - 断言指定能力组合的装饰链发送结果体现各能力效果
    - _Requirements: 11.4_

  - [ ]* 11.4 编写属性测试 Property 8（装饰器能力任意叠加）
    - **Property 8：装饰器能力任意叠加**
    - **Validates: Requirements 3.3**
    - 随机装饰能力子集与叠加顺序，断言最终装饰链结果体现所有已叠加能力的效果
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 8: 装饰器能力任意叠加`
    - _Requirements: 3.3_
    - _Properties: 8_

- [x] 12. 结构型 - 外观 Facade（P0）
  - [x] 12.1 实现下单外观与三子系统
    - 实现 `OrderPlacementFacade` 与 `InventoryService`、`PromotionSubSystemService`、`PaymentSubSystemService` 至少 3 个子系统，子系统以接口注入，Facade 顺序编排扣减库存→计算优惠→发起支付
    - _Requirements: 3.4_

  - [x] 12.2 实现外观 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/facade/placeOrder`）
    - 注册演示入口；编写 `structural/facade/doc/facade.md`
    - _Requirements: 3.4, 7.1, 9.2, 11.1_

  - [ ]* 12.3 编写外观下单编排示例测试
    - 合法下单路径下断言三子系统按序被调用（示例测试）
    - _Requirements: 3.4, 11.4_

- [ ] 13. 结构型 - 桥接 Bridge（P1）
  - [ ] 13.1 实现推送渠道与消息类型双维度
    - 实现 `PushChannel` 实现者接口（`AppPushChannel`、`SmsPushChannel` 两实现）与 `AbstractMessage` 抽象（持有 `PushChannel`），`MarketingMessage`、`SystemMessage` 两个细化抽象，使两维度可独立扩展
    - _Requirements: 3.5_

  - [ ] 13.2 实现桥接 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/bridge/push`，Request 含 messageType、channel）
    - 注册演示入口；编写 `structural/bridge/doc/bridge.md`
    - _Requirements: 3.5, 7.1, 9.2, 11.1_

  - [ ]* 13.3 编写桥接维度组合参数化示例测试
    - 枚举「消息类型 × 推送渠道」全组合，断言各组合均正确推送（参数化示例测试）
    - _Requirements: 3.5, 11.4_

- [ ] 14. 结构型 - 组合 Composite（P1）
  - [ ] 14.1 实现审批节点树
    - 实现 `ApprovalNode` 组件接口（`process()`、`countLeaves()`）、`ApprovalLeaf` 叶子、`ApprovalGroup` 组合（含子节点列表，递归处理），通过同一接口递归处理两类节点
    - _Requirements: 3.6_

  - [ ] 14.2 实现组合 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/composite/process`，构造树并递归处理）
    - 注册演示入口；编写 `structural/composite/doc/composite.md`
    - _Requirements: 3.6, 7.1, 9.2, 11.1_

  - [ ]* 14.3 编写组合核心行为单元测试
    - 断言组合节点与叶子节点经同一接口被递归处理
    - _Requirements: 11.4_

  - [ ]* 14.4 编写属性测试 Property 9（组合递归一致性）
    - **Property 9：组合递归一致性**
    - **Validates: Requirements 3.6**
    - 随机生成审批树（深度/广度随机），断言叶子计数等于各子树叶子计数之和，且组合与叶子节点均被处理
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 9: 组合递归一致性`
    - _Requirements: 3.6_
    - _Properties: 9_

- [ ] 15. 结构型 - 享元 Flyweight（P1）
  - [ ] 15.1 实现风控规则实体、Mapper XML 与享元工厂
    - 实现 `RiskRuleEntity` 实体与 `RiskRuleMapper` 接口及 `RiskRuleMapper.xml`（`selectByCode`、`selectAllEnabled`，SQL 全部写在 XML、禁用 Wrapper，落实 C11）
    - 实现 `RiskRule` 享元与 `RiskRuleFactory`（内部缓存 `Map<String,RiskRule>`，内蕴状态从 `risk_rule_dict` 加载，相同 rule_code 复用同一共享实例）
    - _Requirements: 3.7, 3.8, 9.8, 9.9_

  - [ ] 15.2 实现享元 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`GET /pattern/flyweight/sameRule`，同 code 两次获取返回是否引用相等）
    - 注册演示入口；编写 `structural/flyweight/doc/flyweight.md`
    - _Requirements: 3.7, 7.1, 9.2, 11.1_

  - [ ]* 15.3 编写享元核心行为单元测试
    - 断言相同 rule_code 重复获取返回同一共享实例
    - _Requirements: 11.4_

- [ ] 16. 检查点 - 结构型模式收尾
  - 确保所有测试通过，如有疑问请询问用户。

### 阶段五 · 行为型模式（Behavioral）

- [x] 17. 行为型 - 策略 Strategy（P0）
  - [x] 17.1 实现促销策略族与上下文服务
    - 实现 `PromotionStrategy` 接口与 `FullReductionStrategy`（满减）、`DiscountStrategy`（折扣）、`DirectReductionStrategy`（立减）三种可相互替换策略
    - 实现 `PromotionCalculateService` 上下文：注入 `Map<String,PromotionStrategy>`，按类型标识选策略，不修改调用方代码即可替换
    - _Requirements: 4.1, 10.4_

  - [x] 17.2 实现策略 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/strategy/calculate`）
    - 注册演示入口；编写 `behavioral/strategy/doc/strategy.md`
    - _Requirements: 4.1, 7.1, 9.2, 11.1_

  - [ ]* 17.3 编写策略核心行为单元测试
    - 断言三种策略各自计算结果的正常路径
    - _Requirements: 11.4_

  - [ ]* 17.4 编写属性测试 Property 10（策略计算不变式）
    - **Property 10：策略计算不变式**
    - **Validates: Requirements 4.1**
    - 随机订单金额与策略参数，断言优惠额 ≥ 0、优惠额 ≤ 原金额、且优惠后金额 = 原金额 − 优惠额
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 10: 策略计算不变式`
    - _Requirements: 4.1_
    - _Properties: 10_

- [x] 18. 行为型 - 模板方法 Template Method（P0）
  - [x] 18.1 实现对账模板与多渠道实现
    - 实现 `AbstractReconcileTemplate`（`final reconcile()` 固定骨架：拉取→解析→比对→生成差异报告，其中 `fetch()`/`parse()` 为子类重写的抽象步骤）与 `AlipayReconcileTemplate`、`WechatReconcileTemplate`
    - _Requirements: 4.2_

  - [x] 18.2 实现模板方法 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/template/reconcile`，Request 含 channel）
    - 注册演示入口；编写 `behavioral/templatemethod/doc/templatemethod.md`
    - _Requirements: 4.2, 7.1, 9.2, 11.1_

  - [ ]* 18.3 编写模板方法步骤顺序示例测试
    - 断言步骤执行顺序恒为 fetch→parse→compare→report（示例测试）
    - _Requirements: 4.2, 11.4_

- [x] 19. 行为型 - 观察者 Observer（P0）
  - [x] 19.1 实现订单状态事件、发布者与多监听者
    - 实现 `OrderStatusChangedEvent` 事件、`OrderStatusEventPublisher` 发布者（基于 `ApplicationEventPublisher`），以及 `SmsNotifyListener`、`PointsRewardListener` 至少两个相互独立的 `@EventListener` 监听者，实现发布方与监听方解耦
    - _Requirements: 4.3, 10.3_

  - [x] 19.2 实现观察者 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/observer/changeOrderStatus`）
    - 注册演示入口；编写 `behavioral/observer/doc/observer.md`
    - _Requirements: 4.3, 7.1, 9.2, 11.1_

  - [ ]* 19.3 编写观察者多播集成测试
    - 用 `@SpringBootTest` 验证同一订单状态变更事件发布后各监听者均被回调（集成测试）
    - _Requirements: 4.3, 10.3, 11.4_

- [x] 20. 行为型 - 责任链 Chain of Responsibility（P0）
  - [x] 20.1 实现风控规则处理器链
    - 实现 `RiskRuleHandler` 接口（`handle(RiskContext)`、`order()`）与 `AmountLimitHandler`、`BlacklistHandler`、`FrequencyHandler` 至少两个按序节点，以及 `RiskRuleChain`（注入 `List<RiskRuleHandler>` 按 order 排序组链、驱动依次传递直至被拦截或通过全部节点）
    - _Requirements: 4.4_

  - [x] 20.2 实现责任链 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/chain/riskCheck`）
    - 注册演示入口；编写 `behavioral/chain/doc/chain.md`
    - _Requirements: 4.4, 7.1, 9.2, 11.1_

  - [ ]* 20.3 编写责任链核心行为单元测试
    - 断言请求沿链传递、被拦截或全部通过的核心行为
    - _Requirements: 11.4_

  - [ ]* 20.4 编写属性测试 Property 11（责任链传递与短路）
    - **Property 11：责任链传递与短路**
    - **Validates: Requirements 4.4**
    - 随机风控上下文，断言结果为「通过」当且仅当所有节点通过；被某节点拦截时其后节点不再被执行
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 11: 责任链传递与短路`
    - _Requirements: 4.4_
    - _Properties: 11_

- [x] 21. 行为型 - 状态 State（P0）
  - [x] 21.1 实现订单实体、Mapper XML 与状态机
    - 实现 `OrderEntity` 实体与 `OrderMapper` 接口及 `OrderMapper.xml`（`selectById`、`updateStatus`，SQL 写在 XML、禁用 Wrapper，落实 C11）
    - 实现 `OrderState` 接口与 `CreatedState`/`PaidState`/`ShippedState`/`CompletedState`/`CancelledState` 至少三个状态及 `OrderStateContext`（持有当前状态与订单，驱动流转，显式定义合法流转，非法流转抛 `IllegalStateTransitionException` 并保持原状态）
    - _Requirements: 4.5, 4.6, 9.8, 9.9_

  - [x] 21.2 实现状态 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/order/changeStatus`，Request 含 orderId、action）
    - 注册演示入口；编写 `behavioral/state/doc/state.md`
    - _Requirements: 4.5, 7.1, 9.2, 11.1_

  - [ ]* 21.3 编写状态机核心行为单元测试
    - 断言合法流转转入目标状态的正常路径，并覆盖非法流转被拒绝、状态不变、返回错误的边界路径
    - _Requirements: 11.4, 4.6_

  - [ ]* 21.4 编写属性测试 Property 12（状态机合法与非法流转）
    - **Property 12：状态机合法与非法流转**
    - **Validates: Requirements 4.5, 4.6**
    - 对「状态 × 动作」全组合随机抽样：合法流转转入唯一确定目标状态；否则被拒绝、状态不变并返回指示非法的错误结果
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 12: 状态机合法与非法流转`
    - _Requirements: 4.5, 4.6_
    - _Properties: 12_

- [x] 22. 行为型 - 命令 Command（P0）
  - [x] 22.1 实现商品与命令历史实体及 Mapper XML
    - 实现 `ProductEntity`、`CommandHistoryEntity` 实体
    - 实现 `ProductMapper`（`insert`、`updateById`、`logicDelete`、`restore`、`selectById`）与 `CommandHistoryMapper`（`insert`、`selectLastExecuted`、`markUndone`）及各自 XML（SQL 全部写在 XML、禁用 Wrapper，落实 C11）
    - _Requirements: 4.7, 9.8, 9.9_

  - [x] 22.2 实现命令角色与调用者
    - 实现 `OperationCommand` 接口（`execute()`/`undo()`/`describe()`）、`AddProductCommand`/`UpdateProductCommand`/`DeleteProductCommand` 具体命令（保存 before/after 快照）、`ProductService` 接收者、`CommandInvoker`（`invoke` 执行并入历史、`undoLast` 撤销最近命令并恢复数据）
    - _Requirements: 4.7, 4.8_

  - [x] 22.3 实现命令 Demo_Controller（含删除接口）、注册演示入口与 Pattern_Doc
    - 实现 `POST /pattern/command/execute`、`POST /pattern/command/undo`
    - 实现删除接口 `GET /pattern/command/deleteProduct`：使用 `@GetMapping`、强制携带非空删除标识 `confirmDelete`、标注 `@RequiresPermission("pattern:product:remove")` 完成权限校验（落实 C7，需求 9.7/9.10/9.11）
    - 注册演示入口；编写 `behavioral/command/doc/command.md`
    - _Requirements: 4.7, 4.8, 9.6, 9.7, 9.10, 9.11, 7.1, 11.1_

  - [ ]* 22.4 编写命令核心行为单元测试
    - 断言命令执行后历史新增记录、撤销后数据恢复的正常路径
    - _Requirements: 11.4_

  - [ ]* 22.5 编写属性测试 Property 13（命令历史与撤销往返）
    - **Property 13：命令历史与撤销往返**
    - **Validates: Requirements 4.7, 4.8**
    - 随机命令序列（增/改/删），断言每次执行使历史新增一条记录，对最近一次已执行命令撤销后受影响数据恢复至执行前状态
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 13: 命令历史与撤销往返`
    - _Requirements: 4.7, 4.8_
    - _Properties: 13_

  - [ ]* 22.6 编写属性测试 Property 16（删除操作安全边界）
    - **Property 16：删除操作安全边界**
    - **Validates: Requirements 9.10, 9.11**
    - 对「删除标识有/无 × 有权/无权」组合随机抽样，断言仅当两者同时满足才执行删除；否则拒绝、目标数据不变并返回失败原因
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 16: 删除操作安全边界`
    - _Requirements: 9.10, 9.11_
    - _Properties: 16_

  - [ ]* 22.7 编写删除接口 POST/GET 映射 MockMvc 集成测试
    - 用 MockMvc 验证新增/修改映射为 POST、删除映射为 GET（集成测试）
    - _Requirements: 9.6, 9.7_

- [ ] 23. 行为型 - 迭代器 Iterator（P1）
  - [ ] 23.1 实现自定义分页迭代器
    - 实现 `PageIterator<T>`（`hasNext()`/`next()`，跨页自动加载）、`PagedResultSet<T>`（`iterator()`）、`PageDataLoader<T>`（按页加载回调）
    - _Requirements: 4.9_

  - [ ] 23.2 实现迭代器 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`GET /pattern/iterator/traverse`）
    - 注册演示入口；编写 `behavioral/iterator/doc/iterator.md`
    - _Requirements: 4.9, 7.1, 9.2, 11.1_

  - [ ]* 23.3 编写迭代器核心行为单元测试
    - 断言跨页遍历产出元素的正常路径
    - _Requirements: 11.4_

  - [ ]* 23.4 编写属性测试 Property 14（迭代器遍历完整性）
    - **Property 14：迭代器遍历完整性**
    - **Validates: Requirements 4.9**
    - 随机数据集与页大小，断言迭代器遍历产出的元素序列在数量与顺序上与原始数据集完全一致
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 14: 迭代器遍历完整性`
    - _Requirements: 4.9_
    - _Properties: 14_

- [ ] 24. 行为型 - 中介者 Mediator（P1）
  - [ ] 24.1 实现售后工单中介者与各同事类
    - 实现 `AfterSaleMediator` 接口、`AfterSaleMediatorImpl` 具体中介者、`Colleague` 抽象同事（持有 mediator）、`CustomerColleague`/`AgentColleague`/`WarehouseColleague` 具体同事，各方仅通过中介者交互、不直接相互引用
    - _Requirements: 4.10_

  - [ ] 24.2 实现中介者 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/mediator/handleTicket`）
    - 注册演示入口；编写 `behavioral/mediator/doc/mediator.md`
    - _Requirements: 4.10, 7.1, 9.2, 11.1_

  - [ ]* 24.3 编写中介者协作示例测试
    - 断言工单协作流程中消息经中介者转发、各方无直接引用（示例测试）
    - _Requirements: 4.10, 11.4_

- [ ] 25. 行为型 - 备忘录 Memento（P1）
  - [ ] 25.1 实现草稿备忘录
    - 实现 `DraftDocument` 发起人（`save()` 生成 `DraftMemento`、`restore(memento)`）、`DraftMemento` 不可变快照、`DraftCaretaker` 管理者（保存历史快照栈）
    - _Requirements: 4.11_

  - [ ] 25.2 实现备忘录 Demo_Controller、注册演示入口与 Pattern_Doc
    - 实现 Demo_Controller（`POST /pattern/memento/saveDraft`、`POST /pattern/memento/restoreDraft`）
    - 注册演示入口；编写 `behavioral/memento/doc/memento.md`
    - _Requirements: 4.11, 7.1, 9.2, 11.1_

  - [ ]* 25.3 编写备忘录核心行为单元测试
    - 断言保存后修改再恢复，内容回到保存时快照的正常路径
    - _Requirements: 11.4_

  - [ ]* 25.4 编写属性测试 Property 15（备忘录保存恢复往返）
    - **Property 15：备忘录保存恢复往返**
    - **Validates: Requirements 4.11**
    - 随机草稿内容，断言保存生成备忘录后任意修改、再恢复，草稿内容等于保存时快照
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 15: 备忘录保存恢复往返`
    - _Requirements: 4.11_
    - _Properties: 15_

- [ ] 26. 检查点 - 行为型模式收尾
  - 确保所有测试通过，如有疑问请询问用户。

### 阶段六 · 跨模式属性测试与整体验证

- [ ] 27. 跨模式属性测试与全局收尾
  - [ ]* 27.1 编写属性测试 Property 1（实例共享与单例性）
    - **Property 1：实例共享与单例性**
    - **Validates: Requirements 2.2, 3.7, 3.8**
    - 覆盖单例（多次获取）与享元工厂（相同 rule_code 多次获取）两处，断言返回对象引用恒相等（同一实例）；本属性对应唯一一个属性测试方法
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 1: 实例共享与单例性`
    - _Requirements: 2.2, 3.7, 3.8_
    - _Properties: 1_

  - [ ]* 27.2 编写属性测试 Property 2（按标识选实现的正确性）
    - **Property 2：按标识选实现的正确性**
    - **Validates: Requirements 2.3, 10.4**
    - 覆盖工厂方法支付渠道、策略类型、容器可注入实现标识，随机受支持标识，断言返回实现唯一确定且其自身声明标识与请求标识一致；本属性对应唯一一个属性测试方法
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 2: 按标识选实现的正确性`
    - _Requirements: 2.3, 10.4_
    - _Properties: 2_

  - [ ]* 27.3 编写属性测试 Property 3（非法输入与越权请求返回可观察错误且无副作用）
    - **Property 3：非法输入与越权请求返回可观察错误且无副作用**
    - **Validates: Requirements 2.4, 6.6, 10.5, 11.3**
    - 随机非法输入（空值、越界、未知或无匹配标识）或失败调用，断言返回带失败原因的可观察错误（`AjaxResult.error` 或经全局异常处理器转换的错误响应），不静默失败且无实例创建/数据写入副作用；本属性对应唯一一个属性测试方法
    - 使用 jqwik `@Property(tries = 100)`，注释标注 `// Feature: design-patterns-showcase, Property 3: 非法输入与越权请求返回可观察错误且无副作用`
    - _Requirements: 2.4, 6.6, 10.5, 11.3_
    - _Properties: 3_

  - [ ]* 27.4 编写演示入口清单示例测试
    - 断言 `DemoEntryRegistry` 聚合结果含全部已实现模式且每条目字段（模式名称 + 触发方式）完整（示例测试）
    - _Requirements: 1.5_

  - [ ] 27.5 运行完整测试套件并核对覆盖完整性
    - 全量运行单元/属性/集成/示例测试，修复失败用例，确保套件全绿且不包含被禁用或跳过的测试（禁止 `@Disabled`/`@Ignore`）
    - 核对启动清单完整：21 个已实现模式的演示入口均出现在 `DemoEntryRegistry` 聚合清单中
    - 核对 Priority_List：P0 14 个模式全部实现无遗漏（需求 5.4），P2（访问者、解释器）按取舍说明不实现
    - _Requirements: 11.5, 1.5, 5.4_

- [ ] 28. 最终检查点 - 全工程收尾
  - 确保所有测试通过，如有疑问请询问用户。

## Notes

- 标记 `*` 的子任务为可选测试任务（单元测试、属性测试、集成测试、示例测试、冒烟测试），可为快速 MVP 跳过；未标记 `*` 的任务为核心实现/收尾任务，必须实现。顶级任务不带 `*`。
- 每个任务均引用具体需求条款（granular sub-requirement）以保证可追溯；属性测试任务额外通过 `_Properties: N_` 标注其对应的设计正确性属性编号，并在任务体内以 `**Property N**` + `**Validates: Requirements**` 双重标注。
- 检查点（任务 8/16/26/28）用于在创建型、结构型、行为型各阶段及全工程末尾做增量验证。
- 属性测试统一使用 **jqwik**（不自研框架），每个 `@Property` 至少 100 次迭代，并以注释 `// Feature: design-patterns-showcase, Property {number}: {property_text}` 标注；design.md 的 16 条正确性属性一一对应，每条恰好对应唯一一个属性测试方法。其中 Property 1/2/3 为跨模式属性（集中放在任务 27），Property 4~16 紧邻各自模式实现，以尽早捕获错误。
- design.md 中已降级为示例/集成测试的验收点（抽象工厂同族一致性 2.5、外观编排 3.4、桥接维度组合 3.5、模板方法步骤顺序 4.2、观察者多播 4.3/10.3、中介者协作 4.10、代理 AOP 织入 10.2、删除接口映射 9.6/9.7、上下文加载 1.4、演示入口清单 1.5）按对应测试类型编写，不作为属性测试。
- 设计原则与代码规范类约束（需求 8、需求 9.1~9.9 的 C1~C12）通过编码与代码评审保障，不单独设运行期测试任务。
- P0 模式（14 个）全部实现，无遗漏（需求 5.4）；P2 模式（访问者、解释器）依 Priority_List 取舍不实现。
- 演示入口注册采用「各模式自贡献独立 Bean + 注册表聚合」机制，既满足需求 1.5 的集中清单，又避免并行开发时集中文件写冲突、符合开闭原则。

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3"] },
    { "id": 2, "tasks": ["2.1", "2.3", "2.6"] },
    { "id": 3, "tasks": ["2.2", "2.4", "2.5", "3.1", "4.1", "5.1", "6.1", "7.1", "9.1", "10.1", "11.1", "12.1", "13.1", "14.1", "15.1", "17.1", "18.1", "19.1", "20.1", "21.1", "22.1", "23.1", "24.1", "25.1"] },
    { "id": 4, "tasks": ["2.7", "3.2", "3.3", "4.2", "4.3", "5.2", "5.3", "6.2", "6.3", "6.4", "7.2", "7.3", "7.4", "9.2", "9.3", "9.4", "10.2", "10.3", "10.4", "11.2", "11.3", "11.4", "12.2", "12.3", "13.2", "13.3", "14.2", "14.3", "14.4", "15.2", "15.3", "17.2", "17.3", "17.4", "18.2", "18.3", "19.2", "20.2", "20.3", "20.4", "21.2", "21.3", "21.4", "22.2", "23.2", "23.3", "23.4", "24.2", "24.3", "25.2", "25.3", "25.4", "27.1", "27.2"] },
    { "id": 5, "tasks": ["9.5", "19.3", "22.3", "22.4", "22.5", "27.3", "27.4"] },
    { "id": 6, "tasks": ["22.6", "22.7"] },
    { "id": 7, "tasks": ["27.5"] }
  ]
}
```
