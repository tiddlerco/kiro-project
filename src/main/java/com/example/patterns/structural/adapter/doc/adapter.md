# 适配器模式 Adapter

业务场景：统一短信发送。系统需要向用户下发短信，但接入了阿里云、腾讯云等多家第三方短信服务商，各服务商 SDK 的方法签名、入参结构、回执格式互不相同。调用方希望面向一个统一的短信发送接口编程，无需感知具体服务商的差异。

## 一、解决的问题

业务侧只需要「发一条短信」这一统一能力，但各服务商 SDK 的调用方式各不相同：阿里云是 `sendSms(signName, phoneNumbers, templateParam)` 三段式、腾讯云是 `pushMessage(mobile, content, appId)` 且应用 ID 为 `int`。若不使用适配器模式，调用方往往要直接耦合各 SDK：

```
if ("aliyun".equals(vendor)) {
    aliyunClient.sendSms(signName, phone, content);
} else if ("tencent".equals(vendor)) {
    tencentClient.pushMessage(phone, content, appId);
}
```

这种写法会带来如下设计问题：

- **调用方与第三方 SDK 强耦合**：业务代码直接散落各服务商 SDK 的差异化签名，SDK 升级或更换会直接波及调用方。
- **违反开闭原则**：每新增一家服务商，都要回到调用方修改 `if-else` 分支，改动面大、回归风险高。
- **缺乏统一抽象**：入参与回执格式各异，调用方需自行处理差异，难以统一判定发送结果与展示回执。

适配器模式定义一个与服务商无关的统一目标接口 `SmsSender`，为每家服务商提供一个适配器，把统一请求 `SmsRequest` 适配到各 SDK 的差异化签名，并把各自的回执归一化为统一的 `SmsSendResult`。调用方仅依赖 `SmsSender`，彻底屏蔽 SDK 差异。

## 二、遵循的设计原则

- **开闭原则（OCP）**：新增一家服务商只需新增一个实现 `SmsSender` 的适配器 `@Component` 并声明唯一 `vendor()` 标识即可自动接入路由，无需修改调用方 `SmsService` 与既有适配器。体现位置：`SmsService#initVendorRouter()` 以注入的 `List<SmsSender>` 自动构建「服务商标识 → 适配器」路由表。
- **依赖倒置原则（DIP）**：调用方仅依赖抽象接口 `SmsSender`，不感知任何具体服务商 SDK。体现位置：`SmsService` 字段 `List<SmsSender>` 面向接口注入。
- **组合优于继承**：各适配器以「对象组合」方式持有被适配的第三方客户端（`AliyunSmsClient`、`TencentSmsClient`）并经容器注入，而非通过继承复用其能力。体现位置：`AliyunSmsAdapter`、`TencentSmsAdapter` 以 `@Resource` 注入对应客户端。
- **单一职责原则（SRP）**：每个适配器只负责一家服务商的入参/回执适配，「按标识路由」的职责统一收敛到 `SmsService`，适配器之间互不引用。

## 三、优点与适用及不适用场景

**优点：**

- 解耦调用方与第三方 SDK，调用方仅面向统一接口编程，SDK 差异被适配器完全屏蔽。
- 新增服务商只需新增一个适配器，符合开闭原则，扩展成本低。
- 入参与回执被归一化，调用方可统一判定发送结果、展示回执，无需关心服务商差异。

**适用场景：**

- 需要复用一个或多个已有类，但其接口与当前所需接口不一致（典型如对接多家第三方 SDK）。
- 希望以统一接口接入多个签名各异、且无法或不便修改的外部组件。

**不适用场景：**

- 各被适配对象的接口本就一致、或可直接修改源接口使其统一时，引入适配器属于多余抽象。
- 仅对接单一且稳定的实现、未来不会扩展时，直接调用更直观，适配器反而增加层级。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Target（目标接口） | 定义调用方期望的、与服务商无关的统一发送接口 | `com.example.patterns.structural.adapter.SmsSender` |
| Adaptee（被适配者） | 接口签名各异、需被适配的第三方 SDK | `AliyunSmsClient`、`TencentSmsClient` |
| Adapter（适配器） | 实现目标接口，把统一请求适配到对应 SDK 签名并归一化回执 | `AliyunSmsAdapter`、`TencentSmsAdapter` |
| Client（调用方） | 仅依赖目标接口，按服务商标识路由到对应适配器 | `com.example.patterns.structural.adapter.service.SmsService` |
| Request / Result（数据对象） | 统一的发送入参与归一化的发送结果 | `com.example.patterns.structural.adapter.domain.SmsRequest`、`SmsSendResult` |

> 关于路由的说明：经典适配器模式不涉及「多适配器选择」，本示例为演示「以统一接口对接多家服务商」，在调用方 `SmsService` 中基于各适配器自身声明的 `vendor()` 建立「服务商标识 → 适配器」路由表，并在启动期对重复标识快速失败。此为面向 Spring 容器自动装配的有意扩展，目的是让新增服务商无需改动调用方即可自动接入。
