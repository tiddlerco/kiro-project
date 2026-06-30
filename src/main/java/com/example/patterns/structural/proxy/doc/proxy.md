# 代理模式 Proxy

业务场景：报表查询的「缓存 + 限流」代理。报表查询是高成本操作（统计计算耗时较长），且查询入口需防止被高频调用打垮。借助 Spring AOP，代理对象与真实主题实现同一接口，在不修改真实查询逻辑的前提下，透明织入缓存复用与请求限流。

## 一、解决的问题

报表查询服务 `ReportQueryServiceImpl` 只应专注于「如何统计报表」这一核心职责。但在真实业务中，这类高成本查询往往还需要附加两类横切关注点：

- **缓存**：相同查询条件短时间内重复请求时，应复用既有结果，避免重复承担高昂的计算成本。
- **限流**：查询入口需约束请求速率，防止高频调用耗尽资源。

若把缓存与限流逻辑直接写进 `ReportQueryServiceImpl`，会带来如下设计问题：

```
public ReportData query(ReportQueryRequest req) {
    enforceRateLimit();              // 限流逻辑混入
    ReportData cached = cache.get(); // 缓存逻辑混入
    if (cached != null) return cached;
    // ...真正的报表统计逻辑被横切代码淹没...
}
```

- **职责耦合**：查询逻辑与缓存、限流逻辑纠缠在一起，真实主题不再「单纯」，可读性与可测试性下降。
- **违反开闭原则**：调整缓存策略或限流阈值，都要改动核心查询代码，回归风险高。
- **复用困难**：缓存与限流是通用能力，写死在某个实现里无法被其他服务复用。

代理模式将这些横切逻辑抽到一个与真实主题实现同一接口的「代理」中：调用方面向接口编程、对代理无感知，代理在转发调用前后织入增强逻辑，真实主题则保持职责单一。本示例进一步借助 Spring AOP 以动态代理形式实现，连「手写代理类逐方法转发」的样板代码都省去了。

## 二、遵循的设计原则

- **开闭原则（OCP）**：为报表查询新增缓存、限流能力，无需修改真实主题 `ReportQueryServiceImpl` 与调用方 `ReportQueryController` 的任何代码，仅新增切面 `CacheRateLimitAspect`。后续调整缓存或限流策略也只动切面。
- **单一职责原则（SRP）**：真实主题 `ReportQueryServiceImpl` 只负责报表统计计算，缓存命中复用与限流判定统一收敛到代理 `CacheRateLimitAspect`，两者职责清晰分离、可各自独立演进与测试。
- **依赖倒置原则（DIP）**：调用方 `ReportQueryController` 仅依赖抽象主题接口 `ReportQueryService`，既不感知真实主题，也不感知代理切面的存在。体现位置：控制器以 `@Resource` 注入 `ReportQueryService` 接口。
- **里氏替换原则（LSP）**：代理与真实主题实现同一接口 `ReportQueryService`，对调用方完全可替换——无论实际执行的是被代理增强的对象还是原始真实主题，契约（同参查询返回报表数据）保持一致。

## 三、优点与适用及不适用场景

**优点：**

- 在不修改真实主题代码的前提下增强其行为，调用方与真实主题均无感知，符合开闭原则。
- 横切关注点（缓存、限流）与核心业务逻辑解耦，各自职责单一、便于复用与独立测试。
- 借助 Spring AOP 动态代理，免去手写代理类逐方法转发的样板代码，按切点声明式织入。

**适用场景：**

- 需要在访问真实对象前后附加统一的控制逻辑：缓存、限流、权限校验、日志、事务、延迟加载等。
- 真实对象创建或调用成本高，需通过缓存复用、惰性初始化等手段优化（缓存代理、虚拟代理）。
- 需要对远程对象、受保护对象提供本地占位或访问控制（远程代理、保护代理）。

**不适用场景：**

- 横切逻辑与核心逻辑高度耦合、无法清晰剥离时，强行抽代理反而增加间接层与理解成本。
- 对单次调用延迟极其敏感的场景，代理引入的额外转发与拦截开销需审慎权衡。
- 简单对象且无任何增强诉求时，直接调用更直观，引入代理属过度设计。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Subject（抽象主题） | 定义真实主题与代理共同遵循的接口 | `com.example.patterns.structural.proxy.ReportQueryService` |
| RealSubject（真实主题） | 实现核心业务逻辑（纯报表查询） | `com.example.patterns.structural.proxy.ReportQueryServiceImpl` |
| Proxy（代理） | 在转发调用前后织入缓存与限流横切逻辑 | `com.example.patterns.structural.proxy.CacheRateLimitAspect`（以 Spring AOP 充当） |
| Client（调用方） | 面向抽象主题编程，对代理增强无感知 | `com.example.patterns.structural.proxy.controller.ReportQueryController` |
| Request（请求对象） | 承载查询条件并提供缓存键 | `com.example.patterns.structural.proxy.domain.ReportQueryRequest` |
| Result（结果对象） | 承载查询结果，含观察缓存命中的信号 | `com.example.patterns.structural.proxy.domain.ReportData` |

> 关于角色实现方式的说明：经典代理模式中，代理通常是一个手写的、与真实主题实现同一接口的类，内部持有真实主题引用并逐方法转发。本示例借助 Spring AOP，由框架在运行期为真实主题生成动态代理对象，由切面 `CacheRateLimitAspect` 承载代理职责。这是对经典结构的工程化落地——既保留了「代理与真实主题实现同一接口、调用方无感知」的本质，又免去了样板转发代码，并能借助切点表达式精确控制织入范围。
