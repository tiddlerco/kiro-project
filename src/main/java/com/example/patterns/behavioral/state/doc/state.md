# 状态模式 State

业务场景：订单状态机。一笔订单在其生命周期内会经历「已创建 → 已支付 → 已发货 → 已完成」的正常流转，以及在中途被「取消」的分支流转。对订单执行支付、发货、完成、取消等动作时，同一动作在不同状态下表现出完全不同的行为：在某状态下属合法流转则转入目标状态，否则被拒绝且订单状态保持不变。

## 一、解决的问题

订单状态流转若不使用状态模式，常见做法是在一个服务方法里用 `switch`/`if-else` 同时判断「当前状态」与「触发动作」的所有组合：

```
if (status == CREATED && action == pay)      { status = PAID; }
else if (status == CREATED && action == cancel) { status = CANCELLED; }
else if (status == PAID && action == ship)    { status = SHIPPED; }
else if (...) { ... }
else { throw 非法流转; }
```

这种写法会带来如下设计问题：

- **违反开闭原则**：每新增一个状态或一条流转规则，都要回到这个核心方法里追加分支，改动面大、回归风险高。
- **分支爆炸、可读性差**：状态数与动作数相乘，条件分支随之膨胀，嵌套深、理解和定位成本高。
- **状态相关行为分散**：同一状态下的行为散落在巨型方法的各个分支中，无法内聚，也难以独立测试单一状态的行为。

状态模式将「每一个状态」抽象为一个独立的状态对象，把「该状态下各动作如何响应（合法则流转、非法则拒绝）」内聚到对应状态类中。上下文持有当前状态对象并将动作委派给它处理，从而把「有哪些状态、各状态如何响应动作」与「调用方如何触发动作」彻底解耦，消除巨型条件分支。

## 二、遵循的设计原则

- **开闭原则（OCP）**：新增一个订单状态只需新增一个实现 `OrderState` 的 `@Component` 并声明其状态码即可自动接入路由表，无需修改上下文 `OrderStateContext`、装配服务 `OrderStateService` 与调用方 `OrderStateController`。体现位置：`OrderStateService#initStateRegistry()` 以注入的 `List<OrderState>` 自动构建「状态码 → 状态对象」路由表。
- **单一职责原则（SRP）**：每个具体状态类只负责「本状态下各动作的响应」这一件事（如 `CreatedState` 只描述已创建状态如何响应支付/取消），而「状态切换与持久化」的控制权统一收敛到 `OrderStateContext#transitionTo(String)`，状态对象本身保持无状态。
- **依赖倒置原则（DIP）**：上下文与装配服务仅依赖抽象接口 `OrderState`，不感知任何具体状态实现。体现位置：`OrderStateContext` 与 `OrderStateService` 均面向 `OrderState` 接口编程。
- **里氏替换原则（LSP）**：所有具体状态均可作为 `OrderState` 被上下文统一持有与委派，约定合法动作回调上下文完成流转、非法动作抛出 `IllegalStateTransitionException` 且状态不变。

## 三、优点与适用及不适用场景

**优点：**

- 消除巨型 `switch`/`if-else` 分支，将状态相关行为内聚到各状态类，可读性与可维护性显著提升。
- 状态可独立增减，新增状态符合开闭原则，扩展成本低。
- 每个状态类职责单一、无状态，便于单独测试与复用；状态转换关系清晰显式。

**适用场景：**

- 对象的行为依赖其状态，且需要在运行期根据状态改变行为（如订单、工单、审批流等状态机）。
- 存在大量与状态相关的条件分支，且状态与流转规则会持续演进的场景。

**不适用场景：**

- 状态数量很少、流转规则固定且极少变化时，引入状态模式反而增加类的数量与抽象成本，直接条件判断更直观。
- 状态间行为差异极小、几乎共用同一套逻辑时，拆分状态类收益有限。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Context（上下文） | 持有当前状态对象，对外提供动作入口并委派当前状态处理，承载状态切换与持久化 | `com.example.patterns.behavioral.state.OrderStateContext` |
| State（状态抽象） | 定义各动作在某状态下的统一接口与状态码约定 | `com.example.patterns.behavioral.state.state.OrderState` |
| ConcreteState（具体状态） | 实现本状态下各动作的响应（合法流转或拒绝） | `CreatedState`、`PaidState`、`ShippedState`、`CompletedState`、`CancelledState` |
| 状态机装配与入口 | 装配「状态码 → 状态对象」路由表并提供流转统一入口 | `com.example.patterns.behavioral.state.service.OrderStateService` |
| 状态载体 | 承载订单及其当前状态码，作为持久化对象 | `com.example.patterns.behavioral.state.entity.OrderEntity` |

> 关于角色取舍的说明：经典状态模式中，状态切换（设置上下文的下一状态）既可由上下文负责，也可由状态对象自身负责。本示例为契合「先持久化、后切换内存状态」的一致性要求与 Spring 单例共享，将状态切换收敛到上下文 `OrderStateContext#transitionTo`，各状态对象在确认动作合法后回调该方法完成流转，从而保持状态对象无内部可变字段、可作为单例被安全共享。

合法流转关系：已创建 → 已支付 / 已取消；已支付 → 已发货 / 已取消；已发货 → 已完成；已完成、已取消为终态，不再允许任何流转。
