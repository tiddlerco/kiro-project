# Requirements Document

## Introduction

本项目是一个基于 Spring Boot 的设计模式实战教学示例工程，目标是系统化演示 GoF（Gang of Four）经典设计模式在企业级真实场景中的工程化落地。项目面向具备一定 Java 与 Spring 基础、希望深入掌握设计模式实战应用的开发者。

与玩具型示例不同，本项目的每个模式示例均基于真实企业业务场景（如订单、支付、风控、通知、审批流、缓存、限流等）构建，具备足够的复杂度与代码量，严格遵循 SOLID 等设计原则与统一的代码规范，使示例可直接作为大型企业项目的参考实现。

项目按 GoF 三大类别（创建型、结构型、行为型）组织代码结构，并结合 Spring 的依赖注入、AOP、事件机制等框架特性，体现设计模式在现代企业框架中的真实用法。每个模式均配套说明文档，阐述其解决的问题、遵循的设计原则与适用权衡，并提供可运行的演示入口与单元测试。

考虑到企业实战中各模式使用频率差异，项目对模式按优先级分级（P0 核心、P1 扩展、P2 可选），并对未实现的低频模式说明取舍理由。

## Glossary

- **Showcase_Project（示例项目）**：整个基于 Spring Boot 的设计模式实战示例工程，含构建配置、源代码、文档与测试。
- **Pattern_Module（模式模块）**：针对单个设计模式的代码模块，包含该模式的角色实现、业务场景代码、说明文档与演示入口。
- **Demo_Controller（演示控制器）**：用于触发某个模式示例运行的 HTTP 控制器，仅负责路由分发。
- **Pattern_Service（模式服务）**：承载模式核心业务逻辑的服务层组件。
- **Pattern_Doc（模式说明文档）**：随模式模块提供的说明文档，描述业务场景、解决的问题、遵循的设计原则与权衡。
- **Priority_List（模式优先级清单）**：按企业实战常用度对全部目标模式进行分级（P0/P1/P2）的清单，并记录取舍理由。
- **Showcase_Code（示例代码）**：示例项目中由开发者编写的全部业务与模式实现代码。
- **GoF（Gang of Four）**：《设计模式：可复用面向对象软件的基础》一书提出的 23 种经典设计模式及其三大分类（创建型、结构型、行为型）。
- **SOLID**：五项面向对象设计原则，含单一职责原则、开闭原则、里氏替换原则、接口隔离原则、依赖倒置原则。
- **迪米特法则（Law of Demeter）**：最少知识原则，要求对象只与其直接协作者交互，降低耦合。
- **传统 URL 路径风格**：以动作语义命名路径（如 `/pattern/strategy/calculate`、`/pattern/order/add`）并以请求方法区分操作类型的接口风格，区别于以资源与 HTTP 动词语义为核心的 RESTful 风格。
- **企业业务场景域**：本项目用于承载模式示例的真实业务领域集合，包括但不限于订单、支付、风控、通知、审批流、缓存、限流、对账、报表。
- **演示入口（Demo_Entry）**：可触发某个模式示例执行并产生可观察结果的入口，形式为 HTTP 接口或单元测试。
- **P0/P1/P2**：模式优先级等级。P0 为企业高频核心模式（必须实现）；P1 为中频扩展模式（建议实现）；P2 为低频可选模式（按需实现）。

## Requirements

### Requirement 1：项目基础架构与技术栈

**User Story:** 作为学习者，我希望示例项目基于 Spring Boot 搭建并具备按模式分类的清晰结构，以便我能直接运行项目并按类别查阅各模式示例。

#### Acceptance Criteria

1. THE Showcase_Project SHALL 基于 Spring Boot 框架构建。
2. THE Showcase_Project SHALL 按 GoF 三大类别（创建型、结构型、行为型）划分顶层代码包。
3. WHERE 某个设计模式被纳入实现范围，THE Showcase_Project SHALL 为该模式提供位于对应类别包下的独立子包。
4. WHEN 执行单条构建启动命令，THE Showcase_Project SHALL 完成构建与启动，并使 Spring 应用上下文成功加载、应用进入可接收请求的运行状态。
5. WHEN Showcase_Project 启动完成，THE Showcase_Project SHALL 在启动日志中输出全部可用演示入口的清单，且每个清单条目至少包含所属设计模式名称与该入口的触发方式（HTTP 接口路径或单元测试标识）。
6. THE Showcase_Project SHALL 提供统一的构建配置文件（如 Maven 的 pom.xml）声明全部依赖及其确定的版本号。
7. IF 构建或启动过程中发生错误，THEN THE Showcase_Project SHALL 中止启动流程、不进入运行状态，并在控制台输出指明失败原因的错误信息。

### Requirement 2：创建型模式覆盖

**User Story:** 作为学习者，我希望项目演示企业常用的创建型设计模式，以便我掌握对象创建相关的解耦技巧。

#### Acceptance Criteria

1. THE Showcase_Project SHALL 提供单例（Singleton）模式示例，场景为全局配置与本地缓存管理器，且该示例 SHALL 同时包含 Spring 单例 Bean 实现与经典单例实现两种方式，以支撑二者差异的对比。
2. WHEN 在同一次应用运行期间通过同一访问入口多次获取该单例对象，THE Showcase_Project SHALL 返回同一实例（引用相等）。
3. THE Showcase_Project SHALL 提供工厂方法（Factory Method）模式示例，场景为按支付渠道创建支付处理器，且 SHALL 支持至少 2 种支付渠道，并由工厂依据传入的渠道标识返回对应的独立支付处理器实现。
4. IF 传入的支付渠道标识不受支持，THEN THE Showcase_Project SHALL 返回错误指示，并不创建任何支付处理器实例。
5. THE Showcase_Project SHALL 提供抽象工厂（Abstract Factory）模式示例，场景为多云对象存储（如 OSS、S3、COS）产品族的创建，且 SHALL 覆盖至少 2 个云厂商产品族，每个产品族 SHALL 包含至少 2 种相互关联的产品。
6. THE Showcase_Project SHALL 提供建造者（Builder）模式示例，场景为订单或通知消息的分步构建，且被构建对象 SHALL 包含至少 1 个必选部件与至少 2 个可选部件，并 SHALL 支持分步设置各部件后构建出包含已设置部件的完整对象。
7. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供原型（Prototype）模式示例，场景为营销活动模板的克隆复制。
8. WHERE 项目纳入 P1 扩展模式范围，WHEN 克隆某个营销活动模板原型，THE Showcase_Project SHALL 返回与原型各字段值相等的深拷贝副本，且对该副本的后续修改 SHALL NOT 影响原型对象。

### Requirement 3：结构型模式覆盖

**User Story:** 作为学习者，我希望项目演示企业常用的结构型设计模式，以便我掌握对象组合与接口适配的技巧。

#### Acceptance Criteria

1. THE Showcase_Project SHALL 提供代理（Proxy）模式示例，场景为结合 Spring AOP 的接口缓存与限流代理，其中代理对象与被代理目标对象实现同一接口，并在不修改目标对象代码的前提下通过 Spring AOP 织入缓存与限流横切逻辑。
2. THE Showcase_Project SHALL 提供适配器（Adapter）模式示例，场景为通过统一的短信发送接口对接至少 2 家接口签名互不相同的第三方短信服务商。
3. THE Showcase_Project SHALL 提供装饰器（Decorator）模式示例，场景为对通知消息发送进行能力增强，且提供至少 2 个（如签名、加密、日志）可任意叠加组合的装饰能力。
4. THE Showcase_Project SHALL 提供外观（Facade）模式示例，场景为下单流程通过单一外观接口编排库存、优惠、支付至少 3 个子系统，使调用方无需直接依赖各子系统。
5. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供桥接（Bridge）模式示例，场景为将消息推送解耦为推送渠道与消息类型两个维度，且每个维度各提供至少 2 个可独立扩展的变体。
6. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供组合（Composite）模式示例，场景为对组织架构或审批节点树进行统一处理，且树形结构至少包含一个组合节点与一个叶子节点，并通过同一接口递归处理两类节点。
7. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供享元（Flyweight）模式示例，场景为对数据字典或风控规则的共享对象进行复用。
8. WHERE 项目纳入 P1 扩展模式范围，WHEN 以相同内蕴状态标识重复获取享元对象，THE Showcase_Project SHALL 返回同一个共享实例。

### Requirement 4：行为型模式覆盖

**User Story:** 作为学习者，我希望项目演示企业常用的行为型设计模式，以便我掌握对象间职责分配与协作的技巧。

#### Acceptance Criteria

1. THE Showcase_Project SHALL 提供策略（Strategy）模式示例，场景为促销优惠计算，且 SHALL 至少包含满减、折扣、立减三种可在不修改调用方代码的前提下相互替换的优惠策略实现。
2. THE Showcase_Project SHALL 提供模板方法（Template Method）模式示例，场景为数据导入或对账流程，且其算法骨架 SHALL 以固定顺序编排各处理步骤，并将其中至少一个步骤定义为可由子类重写的抽象步骤。
3. THE Showcase_Project SHALL 提供观察者（Observer）模式示例，场景为结合 Spring 事件机制的订单状态变更通知，且该示例 SHALL 为同一订单状态变更事件注册至少两个相互独立、各自处理该事件的监听者。
4. THE Showcase_Project SHALL 提供责任链（Chain of Responsibility）模式示例，场景为风控规则校验链，该链 SHALL 至少包含两个按顺序排列的规则校验节点，且校验请求 SHALL 沿链依次传递，直至被某一节点拦截或通过全部节点。
5. THE Showcase_Project SHALL 提供状态（State）模式示例，场景为订单状态机的状态流转，该状态机 SHALL 至少包含三个订单状态，并显式定义各状态之间允许发生的合法流转。
6. IF 针对订单当前状态触发了一个未被定义为合法流转的状态变更请求，THEN THE Showcase_Project SHALL 拒绝该次流转、保持订单处于变更前的状态，并返回指示该流转非法的错误结果。
7. THE Showcase_Project SHALL 提供命令（Command）模式示例，场景为管理后台操作，且每个可执行操作 SHALL 被封装为独立的命令对象，并在执行后记录于可追溯的命令历史中。
8. WHEN 管理后台操作的撤销演示入口被调用，THE Showcase_Project SHALL 逆向执行命令历史中最近一次已执行的命令，并将其影响的数据恢复至该命令执行前的状态。
9. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供迭代器（Iterator）模式示例，场景为自定义分页结果集的统一遍历。
10. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供中介者（Mediator）模式示例，场景为售后工单多方协作或复杂表单联动，且各协作方之间 SHALL 仅通过中介者交互而不直接相互引用。
11. WHERE 项目纳入 P1 扩展模式范围，THE Showcase_Project SHALL 提供备忘录（Memento）模式示例，场景为草稿内容的保存与恢复。
12. WHERE 项目纳入 P2 可选模式范围，THE Showcase_Project SHALL 提供访问者（Visitor）模式示例，场景为财务报表对多类型单据的统计计算。
13. WHERE 项目纳入 P2 可选模式范围，THE Showcase_Project SHALL 提供解释器（Interpreter）模式示例，场景为风控规则表达式的解析与求值。

### Requirement 5：模式优先级与取舍说明

**User Story:** 作为学习者，我希望项目按企业实战常用度对模式排定优先级并说明取舍，以便我优先掌握高价值模式。

#### Acceptance Criteria

1. THE Priority_List SHALL 将需求 2 至需求 4 中列出的全部目标模式纳入分级，且每个目标模式恰好归入 P0、P1、P2 三个等级中的一个。
2. THE Priority_List SHALL 为每个目标模式记录其归入对应等级的判定依据，且判定依据至少包含该模式的企业实战常用度（高频、中频或低频）与支撑该判定的至少一个典型企业应用场景。
3. WHERE 某个目标模式未被实现，THE Priority_List SHALL 记录该模式被推迟或省略的取舍理由，且取舍理由至少包含未实现的原因与该模式所属的优先级等级。
4. THE Showcase_Project SHALL 实现 Priority_List 中标记为 P0 等级的全部模式，且不遗漏任何 P0 等级模式。

### Requirement 6：实战场景真实性与复杂度

**User Story:** 作为学习者，我希望每个模式示例都基于真实企业场景且具备足够复杂度，以便我能将示例迁移到实际项目中。

#### Acceptance Criteria

1. WHERE 某个设计模式被纳入实现范围，THE Pattern_Module SHALL 基于企业业务场景域所列业务领域（订单、支付、风控、通知、审批流、缓存、限流、对账、报表）中的一个真实业务场景实现其示例。
2. THE Pattern_Module SHALL 使其全部类名与方法名取自所选业务领域的业务术语，且不得使用与该业务领域无关的通用占位命名。
3. THE Pattern_Module SHALL 至少包含 3 个相互协作的类或接口，且使该设计模式定义的每个核心角色均至少由一个类或接口承担。
4. WHEN Pattern_Module 的演示入口以满足业务约束的合法输入被触发，THE Pattern_Module SHALL 执行该模式的业务正常路径并返回可观察的执行结果。
5. IF 某个模式示例仅以无业务含义的占位命名（如 Foo、Bar、Test）实现，THEN THE Pattern_Module SHALL 被标记为不满足实战要求并需重做。
6. IF Pattern_Module 的演示入口接收到非法输入或不满足业务约束的请求，THEN THE Pattern_Module SHALL 执行对应的业务异常或边界处理路径并返回指示该异常或边界情形的可观察结果。

### Requirement 7：模式说明文档

**User Story:** 作为学习者，我希望每个模式都附带说明文档，以便我理解它解决的问题、遵循的原则与权衡。

#### Acceptance Criteria

1. WHERE 某个设计模式被纳入实现范围，THE Pattern_Module SHALL 提供唯一一份采用统一章节结构的配套 Pattern_Doc，该结构依次涵盖“解决的问题”“遵循的设计原则”“优点与适用及不适用场景”“参与角色与对应类”四个部分。
2. THE Pattern_Doc SHALL 描述该模式在所选业务场景中解决的问题，且至少包含该场景的问题陈述与不使用该模式时将面临的设计问题。
3. THE Pattern_Doc SHALL 逐条列出该示例所遵循的设计原则（至少 1 项），并为每项说明其在示例代码中的体现方式或位置。
4. THE Pattern_Doc SHALL 说明该模式的优点、适用场景与不适用场景，且优点、适用场景、不适用场景各至少列出 1 项。
5. THE Pattern_Doc SHALL 完整列出该模式定义的全部参与角色，并为每个角色标注其在示例代码中真实存在的对应类名。
6. IF 该模式的某个标准参与角色在本示例中未实现，THEN THE Pattern_Doc SHALL 说明该角色被省略或合并的理由。

### Requirement 8：设计原则的体现

**User Story:** 作为学习者，我希望示例代码切实遵循设计原则，以便我学到正确的工程实践而非反面教材。

#### Acceptance Criteria

1. THE Showcase_Code SHALL 使其每个类满足 SOLID 五项原则的可观察判定：(a) 单一职责——每个类仅承担单一职责且仅有一个引起其变更的原因；(b) 开闭——新增功能通过新增类型实现而非修改既有类；(c) 里氏替换——任一实现可替换其所声明的接口或父类型而不改变调用方的预期行为；(d) 接口隔离——客户端不依赖其不使用的接口方法；(e) 依赖倒置——协作对象以抽象（接口）类型声明并注入，而非依赖具体实现类。
2. THE Showcase_Code SHALL 遵循迪米特法则，即任一方法仅调用其直接协作者（自身、方法参数、自身的成员对象、方法内创建的对象）的方法，不出现跨越两个及以上对象的链式穿透调用（如 a.getB().getC().doSomething()）。
3. WHERE 两个类之间存在代码复用关系且该关系不构成真实的 "is-a"（子类型可完全替换其父类型）语义，THE Showcase_Code SHALL 采用组合而非继承实现该复用。
4. THE Showcase_Code SHALL 将各模式中预期可替换或可扩展的角色（变化点）抽象为接口，并使调用方依赖该接口而非具体实现类。
5. WHEN 在某个模式的变化点上新增一个变体实现，THE Showcase_Code SHALL 仅通过新增实现类完成扩展，而不修改该变化点既有的接口与既有实现类。
6. IF 某个 Pattern_Module 的代码经审查发现违反其 Pattern_Doc 中声明遵循的设计原则，THEN THE Showcase_Code SHALL 将该违反项（含被违反的原则与所在类）记录为缺陷，并在通过项目验收前完成修正，且修正后既有演示入口与单元测试仍保持通过。

### Requirement 9：统一代码规范约束

**User Story:** 作为团队负责人，我希望全部示例代码遵循统一代码规范，以便代码风格一致、可读且可维护。

#### Acceptance Criteria

1. THE Showcase_Code SHALL 为每个方法（含接口方法、实现类方法、私有方法）提供 Javadoc，且该 Javadoc 至少包含非空的方法说明文字、针对每个方法参数的 @param 标签、以及针对非 void 返回值的 @return 标签（方法无参数时省略 @param 标签，返回类型为 void 时省略 @return 标签）。
2. THE Demo_Controller SHALL 采用传统 URL 路径风格定义接口路径。
3. THE Showcase_Code SHALL 在全部依赖注入点统一使用 @Resource 注解完成注入。
4. THE Demo_Controller SHALL 在每个接口方法中仅接收请求参数、调用 Pattern_Service 处理业务并返回其结果，不在控制器内编写业务判断、数据遍历或数据访问逻辑。
5. THE Showcase_Code SHALL 将每个承担独立职责的类定义为独立的顶层类文件而非内部类，仅实体类允许包含内部类以表达与其内聚的数据结构。
6. WHERE Demo_Controller 提供新增或修改操作，THE Demo_Controller SHALL 使用 POST 请求方法映射该操作。
7. WHERE Demo_Controller 提供删除操作，THE Demo_Controller SHALL 使用 GET 请求方法映射该操作（本项目刻意采用的传统 URL 路径风格约定，区别于 RESTful 的 DELETE 方法）。
8. WHERE 某个模式示例使用 MyBatis 访问数据库，THE Showcase_Code SHALL 将 SQL 语句定义在 XML 映射文件中。
9. WHERE 某个模式示例使用 MyBatis 访问数据库，THE Showcase_Code SHALL 通过 Mapper XML 配置查询条件，而非使用 MyBatis-Plus 的 Wrapper（如 QueryWrapper、LambdaQueryWrapper）。
10. WHERE 删除操作通过 GET 请求暴露，THE Demo_Controller SHALL 要求该请求显式携带非空的删除目标标识参数，并在执行删除前完成调用者权限校验，以降低被浏览器预取或链接爬取误触发的风险。
11. IF 通过 GET 暴露的删除请求未携带删除目标标识参数或调用者权限校验未通过，THEN THE Demo_Controller SHALL 拒绝执行该删除操作、保持目标数据不变，并返回指示对应失败原因的错误响应。

### Requirement 10：Spring 框架特性结合

**User Story:** 作为学习者，我希望示例展示设计模式在 Spring 框架中的真实应用，以便我理解模式与框架的结合方式。

#### Acceptance Criteria

1. WHERE 某个设计模式被纳入实现范围，THE Showcase_Project SHALL 通过 Spring 容器注入该模式各协作角色之间的依赖对象，而非在角色实现内部直接以 new 创建这些协作对象。
2. THE Showcase_Project SHALL 至少为一个被纳入实现范围的设计模式使用 Spring AOP 实现其横切关注点（如日志记录、缓存、限流、权限校验中的至少一项）。
3. THE Showcase_Project SHALL 至少为一个被纳入实现范围的设计模式使用 Spring 事件机制（事件发布与监听）实现发布方与监听方之间的解耦。
4. WHERE 某个模式存在多个可注入实现，THE Showcase_Project SHALL 演示根据给定的实现标识从 Spring 容器获取对应实现，并在标识匹配时返回唯一确定的实现实例。
5. IF 用于选择实现的标识在 Spring 容器中不存在匹配的可注入实现，THEN THE Showcase_Project SHALL 拒绝该选择请求并返回指示标识无效的错误结果，且不返回任何实现实例。

### Requirement 11：可运行性与演示验证

**User Story:** 作为学习者，我希望每个模式示例都能被独立运行与验证，以便我观察其行为并确认其正确性。

#### Acceptance Criteria

1. WHERE 某个设计模式被纳入实现范围，THE Pattern_Module SHALL 提供至少一个可独立触发的演示入口（HTTP 接口或单元测试），且其触发不依赖其他 Pattern_Module 的演示入口。
2. WHEN 某个演示入口被调用且执行成功，THE Pattern_Module SHALL 返回或输出能反映该模式核心行为的执行结果；经 HTTP 接口触发时以响应内容返回该结果，经单元测试触发时以可被断言校验的返回值或日志输出体现。
3. IF 某个演示入口在被调用时执行失败，THEN THE Pattern_Module SHALL 返回或输出包含失败原因描述的错误结果，而非静默失败或无响应。
4. WHERE 某个设计模式被纳入实现范围，THE Pattern_Module SHALL 提供至少一个单元测试，且该单元测试以断言校验该模式核心行为在至少一条正常路径下的预期执行结果。
5. WHEN 项目的单元测试套件被执行，THE Showcase_Project SHALL 使全部单元测试通过，且该套件不包含被禁用或跳过的单元测试。
