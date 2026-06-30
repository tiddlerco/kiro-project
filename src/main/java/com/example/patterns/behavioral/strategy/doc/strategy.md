# 策略模式（Strategy）

## 一、解决的问题

促销优惠存在满减、折扣、立减等多种算法，且会随业务持续新增。若用 `if-else`/`switch` 在一处集中判断金额并内联各算法，会导致：

- 调用方与具体算法强耦合，新增一种优惠就要改动同一段判断逻辑，违反开闭原则；
- 各算法混杂在一个方法里，难以单独测试与复用，维护成本随算法数量增长而恶化。

策略模式把「依据促销上下文计算优惠」这一会变化的算法抽象为统一接口，每种优惠规则各自独立成一个策略类，由上下文按类型标识选取并委派计算，使算法可在不改动调用方的前提下自由替换与扩展。

## 二、遵循的设计原则

- **开闭原则（OCP）**：新增一种优惠只需新增一个 `PromotionStrategy` 实现并声明唯一类型标识，上下文服务与调用方代码均无需修改。
- **依赖倒置原则（DIP）**：上下文服务与调用方仅依赖抽象 `PromotionStrategy`，不依赖任何具体策略实现。
- **单一职责原则（SRP）**：每个策略类只负责一种优惠算法；上下文服务只负责按类型路由与委派；结果对象只负责钳制并维持促销不变式。

## 三、优点、适用与不适用场景

**优点**

- 算法可插拔、可替换，扩展时对既有代码零侵入；
- 消除大量条件分支，各算法独立内聚、便于单独测试；
- 借助 Spring 以 `List<PromotionStrategy>` 注入并在启动时构建「类型标识 → 策略」路由表，新增策略自动接入。

**适用场景**

- 同一业务存在多种可互换的算法/规则，且需要在运行期按条件选择其一；
- 算法种类会持续增加，期望以新增类而非修改既有判断的方式扩展。

**不适用场景**

- 算法极少且基本稳定不变，引入策略接口反而增加类的数量与理解成本；
- 各「算法」之间差异极小，可用参数化配置直接表达，无需独立成类。

## 四、参与角色与对应类

| 角色 | 说明 | 对应类 |
| --- | --- | --- |
| 抽象策略（Strategy） | 抽象出可替换的优惠算法族，声明计算方法与类型标识 | `strategy.PromotionStrategy` |
| 具体策略（ConcreteStrategy） | 满减优惠算法 | `strategy.FullReductionStrategy` |
| 具体策略（ConcreteStrategy） | 折扣优惠算法 | `strategy.DiscountStrategy` |
| 具体策略（ConcreteStrategy） | 立减优惠算法 | `strategy.DirectReductionStrategy` |
| 上下文（Context） | 按类型标识选取策略并委派计算，维护路由表 | `service.PromotionCalculateService` |
| 入参对象 | 承载原始金额与各策略所需参数，映射为上下文 | `domain.PromotionContext`、`domain.PromotionCalculateRequest` |
| 结果对象 | 钳制优惠额并维持促销不变式 | `domain.PromotionResult` |
| 演示入口 | 暴露促销计算 HTTP 接口 / 贡献启动清单条目 | `controller.PromotionStrategyController`、`StrategyDemoEntryContributor` |
