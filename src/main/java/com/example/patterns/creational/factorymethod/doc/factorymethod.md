# 工厂方法模式 Factory Method

业务场景：按支付渠道创建支付处理器。系统支持微信、支付宝等多种支付渠道，一次支付请求携带渠道标识与支付要素（订单号、金额、付款用户等），由工厂依据渠道标识创建对应的支付处理器并执行支付，未知渠道则返回错误且不创建任何处理器实例。

## 一、解决的问题

在多支付渠道场景中，调用方需要依据渠道标识获得对应的支付实现并执行支付。若不使用工厂方法模式，常见做法是在业务代码里用 `if-else` / `switch` 直接 new 出具体处理器：

```
if ("wechat".equals(channel)) { processor = new WechatPaymentProcessor(); }
else if ("alipay".equals(channel)) { processor = new AlipayPaymentProcessor(); }
else { throw ...; }
processor.pay(ctx);
```

这种写法会带来如下设计问题：

- **违反开闭原则**：每新增一个支付渠道，都要改动这段渠道选择逻辑，改动面大、回归风险高。
- **紧耦合具体实现**：调用方直接 `new` 具体处理器类，与各渠道实现强绑定，难以替换与测试，违反依赖倒置。
- **创建逻辑散落**：相同的渠道选择逻辑可能在多处重复，渠道一变需多处同步修改。

工厂方法模式将「对象的创建」收敛到工厂，调用方仅依赖抽象产品接口与渠道标识，不感知也不直接构造任何具体产品，从而把「要哪种处理器、如何获得」与「处理器怎么实现支付」彻底解耦。

## 二、遵循的设计原则

- **开闭原则（OCP）**：新增一个支付渠道只需新增一个实现 `PaymentProcessor` 的 `@Component` 并声明唯一渠道标识即可自动接入，无需修改工厂 `PaymentProcessorFactory`、服务 `PaymentService` 与调用方 `PaymentController`。体现位置：`PaymentProcessorFactory#initProcessorMap()` 以注入的 `List<PaymentProcessor>` 基于各实现自身声明的 `channel()` 自动构建路由表。
- **依赖倒置原则（DIP）**：工厂与调用方仅依赖抽象接口 `PaymentProcessor`，不感知任何具体渠道实现。体现位置：`PaymentProcessorFactory` 字段 `List<PaymentProcessor>` 面向接口注入，`PaymentService` 仅持有工厂引用。
- **单一职责原则（SRP）**：每个具体产品只负责单一渠道的支付逻辑（`WechatPaymentProcessor` 负责微信、`AlipayPaymentProcessor` 负责支付宝），「按渠道路由获取处理器」的职责统一收敛到工厂，`PaymentService` 仅负责编排「选处理器 + 支付」，`PaymentController` 仅负责路由分发。
- **里氏替换原则（LSP）**：所有具体产品均可作为 `PaymentProcessor` 被工厂统一返回与调用，约定 `pay` 必返回非空的 `PayResult`，调用方对任意渠道实现一视同仁。

## 三、优点与适用及不适用场景

**优点：**

- 解耦调用方与具体产品的创建过程，调用方无需知道也无需直接构造具体处理器类。
- 渠道可独立增减，新增渠道仅新增实现类即可自动接入，符合开闭原则，扩展成本低。
- 创建逻辑集中于工厂，渠道选择规则单点维护，避免散落重复。

**适用场景：**

- 一类对象有多个可替换的具体实现，且具体使用哪个实现在运行期才由参数（如渠道标识、类型枚举）确定。
- 实现集合需要动态扩展、希望新增实现时不改动既有选择逻辑的场景（如支付渠道、消息通道、文件解析器）。

**不适用场景：**

- 具体实现只有一种且基本不会扩展时，引入工厂反而增加抽象成本，直接构造更直观。
- 创建逻辑极其简单、无分支选择时，工厂带来的额外间接层得不偿失。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Product（抽象产品） | 定义产品的统一接口 | `com.example.patterns.creational.factorymethod.PaymentProcessor` |
| ConcreteProduct（具体产品） | 实现单一渠道的支付逻辑 | `WechatPaymentProcessor`、`AlipayPaymentProcessor` |
| Factory（工厂） | 依据渠道标识创建（路由获取）对应产品 | `com.example.patterns.creational.factorymethod.PaymentProcessorFactory` |
| Client / 业务服务（调用方） | 编排「选处理器 + 支付」，对外提供业务能力 | `com.example.patterns.creational.factorymethod.service.PaymentService` |
| Request（请求对象） | 承载支付要素，映射为支付上下文 | `domain.PaymentPayRequest`、`domain.PaymentContext` |
| Result（处理结果） | 承载支付结论及渠道、流水号、金额等 | `com.example.patterns.creational.factorymethod.domain.PayResult` |

> 关于角色取舍的说明：经典工厂方法中，工厂通常以 `if-else` / `switch` 依据类型直接 `new` 具体产品。本示例为契合 Spring 容器自动装配与开闭原则，将具体产品以 `@Component` 交由容器管理，工厂以 `List<PaymentProcessor>` 注入全部实现并基于各实现自身声明的 `channel()` 构建「渠道标识 → 处理器」路由表。此为对经典结构的有意调整：渠道标识由实现类自治声明而非工厂硬编码，新增渠道无需改动工厂，且处理器均为容器预管理的单例，工厂仅做路由查找而不重复实例化。
