# 外观模式 Facade

业务场景：电商下单。一次下单在对外看来只是「提交订单」，背后却要依次完成扣减库存、计算优惠、发起支付三个相互独立子系统的协作。外观模式以单一入口 `OrderPlacementFacade#placeOrder` 编排整套流程，调用方只需提交下单请求即可，无需感知任何子系统。

## 一、解决的问题

下单流程牵涉库存、优惠、支付三个独立子系统，且彼此存在固定的调用顺序与数据传递。若不使用外观模式，调用方（如 Controller）就必须自己依次调用各子系统、自己维护编排顺序与中间结果：

```
int stock = inventoryService.deduct(商品, 数量);
BigDecimal discount = promotionService.calculateDiscount(原价);
String txId = paymentService.pay(渠道, 实付, 用户);
// 调用方还要自己拼装订单号、金额、结果……
```

这种写法会带来如下设计问题：

- **调用方与多个子系统强耦合**：调用方需直接依赖三个子系统接口，任一子系统的接口或编排顺序变化，都会波及每一处调用点。
- **编排逻辑散落且重复**：「扣库存 → 算优惠 → 发支付」的顺序与中间金额计算被复制到每个调用方，难以统一维护，易出现顺序错乱或步骤遗漏。
- **调用方职责过重**：路由分发方（Controller）被迫承担跨子系统的业务编排，违背单一职责，难以测试与复用。

外观模式引入一个「外观」对象，将多个子系统的协作收敛到单一、简化的入口之后。调用方只依赖外观这一个类，子系统的数量、顺序与交互细节全部被封装在外观内部，从而把「如何编排子系统」与「谁来发起下单」彻底解耦。

## 二、遵循的设计原则

- **迪米特法则 / 最少知识原则（LoD）**：调用方 `OrderFacadeController` 只与外观 `OrderPlacementFacade` 一个对象交互，不认识、也不直接依赖库存、优惠、支付任何一个子系统。体现位置：控制器仅注入 `OrderPlacementFacade` 并对其「一行委派」。
- **单一职责原则（SRP）**：外观只负责「编排子系统的调用顺序与中间数据传递」，每个子系统只负责自身领域逻辑（`InventoryService` 管库存、`PromotionSubSystemService` 管优惠、`PaymentSubSystemService` 管支付），职责互不交叠。
- **依赖倒置原则（DIP）**：外观依赖三个子系统的接口而非具体实现，面向抽象编排。体现位置：`OrderPlacementFacade` 以 `InventoryService`、`PromotionSubSystemService`、`PaymentSubSystemService` 接口类型注入。
- **开闭原则（OCP）**：子系统内部实现的替换（如更换支付渠道实现）对调用方透明，调用方无需任何改动；新增编排步骤也内聚在外观内部完成。

## 三、优点与适用及不适用场景

**优点：**

- 为复杂子系统群提供统一、简化的入口，显著降低调用方的使用与理解成本。
- 调用方与子系统解耦，子系统的实现演进、接口调整被隔离在外观之后，影响面可控。
- 编排逻辑集中在外观一处，顺序与中间计算统一维护，避免散落重复。

**适用场景：**

- 一个业务操作需要按固定顺序编排多个子系统，且希望对调用方屏蔽这些子系统的存在（如下单、注册开户、复杂报表生成）。
- 需要为遗留或复杂的子系统群提供一个清晰、稳定的对外接口层。
- 分层架构中希望明确层与层之间的入口边界，降低跨层耦合。

**不适用场景：**

- 子系统本身极简单、调用方直接调用即可时，引入外观反而增加一层无谓的间接。
- 调用方确需对子系统做精细化、差异化控制时，过度封装的外观会限制灵活性（外观应作为「便捷入口」而非「唯一通道」，必要时仍可保留对子系统的直接访问）。
- 当「外观」不断膨胀、把所有业务逻辑都堆进来时，会退化为臃肿的「上帝对象」，需警惕其只做编排、不做具体业务。

> 风险提示：本示例聚焦外观「编排多子系统」这一核心职责，未引入跨子系统的补偿/回滚。当库存扣减成功而后续支付失败时，已扣库存不会在本流程内自动回补；生产场景应结合本地事务消息、TCC 或可靠事件等手段保证最终一致性，此处从略以突出模式本身。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Facade（外观） | 为子系统群提供单一简化入口，编排各子系统协作 | `com.example.patterns.structural.facade.OrderPlacementFacade` |
| SubSystem（子系统） | 实现各自领域的独立功能，不感知外观存在 | `InventoryService`、`PromotionSubSystemService`、`PaymentSubSystemService`（及其实现类） |
| Client（调用方） | 仅依赖外观发起业务操作，不直接接触子系统 | `com.example.patterns.structural.facade.controller.OrderFacadeController` |
| Request（请求对象） | 承载一次下单所需的全部业务要素 | `com.example.patterns.structural.facade.domain.PlaceOrderRequest` |
| Result（处理结果） | 承载下单结果及各项金额、剩余库存、支付流水等可观察信息 | `com.example.patterns.structural.facade.domain.PlaceOrderResult` |
