# 责任链模式 Chain of Responsibility

业务场景：交易风控校验。一笔交易在放行前需要依次经过黑名单、单笔金额上限、近期交易频次等多道风控规则，任一规则命中即拦截并给出拦截原因，全部通过才放行。

## 一、解决的问题

在风控场景中，一笔交易往往需要顺序经过多道相互独立的校验规则。若不使用责任链模式，常见做法是在一个服务方法里用一长串 `if-else` 串起所有规则：

```
if (命中黑名单) { return 拦截; }
if (金额超限)   { return 拦截; }
if (频次超限)   { return 拦截; }
return 通过;
```

这种写法会带来如下设计问题：

- **违反开闭原则**：每新增、删除或调整一条规则，都要改动这个核心方法，改动面大、回归风险高。
- **职责耦合**：规则的「判定逻辑」与规则的「编排顺序、短路控制」全部挤在一个方法里，单一规则无法独立测试，顺序也难以灵活调整。
- **可读性与可维护性差**：规则越多，方法越臃肿，分支嵌套越深，理解和定位成本越高。

责任链模式将「每一条规则」抽象为一个独立的处理节点，将「规则的编排与短路传递」收敛到链驱动者。请求沿链依次传递，命中即短路，从而把「有哪些规则、按什么顺序执行」与「每条规则怎么判定」彻底解耦。

## 二、遵循的设计原则

- **开闭原则（OCP）**：新增一条风控规则只需新增一个实现 `RiskRuleHandler` 的 `@Component` 并赋予唯一 `order()` 序号即可自动接入链路，无需修改链驱动者 `RiskRuleChain` 与调用方 `RiskCheckController`。体现位置：`RiskRuleChain#initChain()` 以注入的 `List<RiskRuleHandler>` 自动组链。
- **单一职责原则（SRP）**：每个具体处理器只负责一条规则的判定（`BlacklistHandler` 判黑名单、`AmountLimitHandler` 判金额上限、`FrequencyHandler` 判频次），「短路传递」的链路控制权统一收敛到 `RiskRuleChain#check(RiskContext)`，节点本身保持无状态、互不引用。
- **依赖倒置原则（DIP）**：链驱动者与调用方仅依赖抽象接口 `RiskRuleHandler`，不感知任何具体规则实现。体现位置：`RiskRuleChain` 字段 `List<RiskRuleHandler>` 面向接口注入。
- **里氏替换原则（LSP）**：所有具体处理器均可作为 `RiskRuleHandler` 被链统一驱动与任意重排，约定 `handle` 必返回非空结果（放行返回通过结果、拦截返回带命中节点与原因的拦截结果）。

## 三、优点与适用及不适用场景

**优点：**

- 解耦请求发起方与处理者，发起方无需知道由哪个节点处理、按什么顺序处理。
- 规则可独立增减、自由重排（仅调整 `order()`），符合开闭原则，扩展成本低。
- 每个节点职责单一、无状态，便于单独测试与复用。

**适用场景：**

- 一个请求需要由多个对象按顺序处理，且具体由哪个对象处理在运行期才确定（如多级风控、多级审批、过滤器链）。
- 处理者集合与顺序需要动态配置或经常变化的场景。

**不适用场景：**

- 处理逻辑固定且极少变化、规则数量很少时，引入责任链反而增加抽象成本，直接顺序调用更直观。
- 对单次调用性能极其敏感、且链路较长的场景，需权衡逐节点传递带来的额外开销。
- 链路存在被「无人处理」风险时，需额外保证有兜底节点，否则请求可能静默穿透（本示例由链驱动者在全部通过时显式返回「通过」结果以规避该问题）。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Handler（处理器抽象） | 定义处理请求的统一接口与排序约定 | `com.example.patterns.behavioral.chain.handler.RiskRuleHandler` |
| ConcreteHandler（具体处理器） | 实现单条规则的判定逻辑 | `BlacklistHandler`、`AmountLimitHandler`、`FrequencyHandler` |
| Client / Invoker（链装配与驱动） | 组装有序链并驱动请求沿链短路传递 | `com.example.patterns.behavioral.chain.service.RiskRuleChain` |
| Request（请求对象） | 沿链传递、承载校验所需输入 | `com.example.patterns.behavioral.chain.domain.RiskContext` |
| Result（处理结果） | 承载放行/拦截结论及命中节点与原因 | `com.example.patterns.behavioral.chain.domain.RiskCheckResult` |

> 关于角色取舍的说明：经典责任链中，「后继引用（successor）」通常由各 Handler 自身持有（节点内部 `next.handle(...)`）。本示例为契合 Spring 容器自动装配与开闭原则，将「短路传递」的链路控制权从各节点上移并集中到链驱动者 `RiskRuleChain`，各节点因而不再持有后继引用、保持无状态。此为对经典结构的有意调整，目的是让节点可被容器统一注入、自由重排与独立测试。
