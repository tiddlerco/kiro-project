# 建造者模式 Builder

业务场景：通知消息的分步构建。一条对外发送的通知由必选部件「接收人」与若干可选部件「标题 / 正文 / 附件 / 优先级」组成。不同业务（验证码、对账提醒、营销推送等）所需的部件组合各异，需要一种既能灵活组合部件、又能在构建收尾时校验必选部件、并保证产物不可变的装配方式。

## 一、解决的问题

当一个对象由「少量必选 + 大量可选」部件构成，且不同调用方所需的部件组合各不相同时，常见的两种朴素做法都会带来设计问题：

- **重叠构造方法（telescoping constructor）**：为每种部件组合提供一个构造方法，随可选部件增多，构造方法数量呈爆炸式增长，且多个同类型参数并排时调用方极易传错位置。
- **无参构造 + 一堆 setter**：虽然规避了构造方法爆炸，但对象在「逐个 set」的过程中长期处于不完整的中间态，必选部件是否齐备无从保证，且对外暴露 setter 破坏了不可变性，构建完成后仍可能被旁路篡改。

```
// 重叠构造方法：组合一多即爆炸，且同类型参数易错位
new NotificationMessage(receiver);
new NotificationMessage(receiver, title);
new NotificationMessage(receiver, title, content);
new NotificationMessage(receiver, title, content, attachments, priority);
```

建造者模式将「如何分步装配」的职责从产品中剥离到独立的建造者：调用方经由统一入口 `NotificationMessage.builder()` 获取建造者，按需链式设置必选与可选部件，最终调用 `build()` 在校验必选部件「接收人」后一次性产出**完整且不可变**的产品。装配过程的可变状态被收敛在建造者内部，产品本身自始至终是定型后的不可变对象。

## 二、遵循的设计原则

- **单一职责原则（SRP）**：产品 `NotificationMessage` 只负责「承载装配完成后的不可变结果」，建造者 `NotificationMessageBuilder` 只负责「分步装配与必选部件校验」，构建编排逻辑则进一步上移到 `NotificationBuildService`，三者职责彼此分离。体现位置：产品全字段 `final` 且无 setter；建造者持有可变临时状态并在 `build()` 中校验。
- **迪米特法则 / 封装变化**：调用方仅通过 `builder()`、链式步骤方法与 `build()` 与构建过程交互，无需了解产品如何拼装其内部状态；产品构造方法为包级私有，外部无法直接 `new`，「产品只能经由建造者产出」这一约束被封装强制。
- **不可变性（Immutability）**：产品全部字段为 `final` 且只读，附件列表在构造时做防御性拷贝并包装为不可变视图，杜绝构建后经由外部引用旁路篡改，使产品可被安全共享。
- **非静默失败**：必选部件「接收人」缺失时，`build()` 抛出 `ServiceException` 而非返回半成品对象，该异常经全局异常处理器统一转换为可观察的错误响应（对应需求 2.6）。

## 三、优点与适用及不适用场景

**优点：**

- 同一构建过程可灵活组合任意可选部件，规避重叠构造方法爆炸与参数错位。
- 链式调用语义清晰，构建意图一目了然。
- 必选部件校验集中在 `build()` 收尾完成，避免对象处于不完整中间态；产物不可变，可安全共享。

**适用场景：**

- 对象由「少量必选 + 较多可选」部件构成，且部件组合多样的场景（如通知消息、HTTP 请求、复杂配置对象）。
- 希望产物不可变、且要求「构建完成即合法」的场景。

**不适用场景：**

- 对象字段极少且几乎全为必选时，直接使用构造方法更简洁，引入建造者反而增加样板代码。
- 对象本身要求可变、需要在生命周期内频繁增删字段时，建造者「一次性定型」的特性并不契合。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Product（产品） | 装配完成后的不可变结果，提供统一构建入口 | `com.example.patterns.creational.builder.NotificationMessage`（全字段 `final`，静态 `builder()`） |
| Builder（具体建造者） | 分步设置部件、收尾校验必选部件并产出产品 | `com.example.patterns.creational.builder.NotificationMessageBuilder`（链式 `to/title/content/attach/priority`，`build()` 校验「接收人」） |
| Director / 构建编排者 | 依据请求对象编排建造者的分步调用 | `com.example.patterns.creational.builder.service.NotificationBuildService` |
| Part（部件值对象） | 作为可选部件参与装配 | `com.example.patterns.creational.builder.domain.NotificationAttachment`（附件）、`NotificationPriority`（优先级枚举） |
| Request（请求对象） | 承载演示接口入参 | `com.example.patterns.creational.builder.domain.NotificationBuildRequest`、`NotificationAttachmentRequest` |
| Client / Invoker（调用入口） | 路由分发并委派构建编排 | `com.example.patterns.creational.builder.controller.NotificationBuilderController` |

> 关于角色取舍的说明：经典建造者常包含独立的「指挥者（Director）」来固化某几种典型构建流程。本示例将「指挥」职责落在演示服务 `NotificationBuildService` 上，由它依据请求对象动态编排建造者的分步调用，既契合 Spring 容器的服务分层，又满足「Controller 仅做路由分发、构建编排下沉到 Service」的工程约束。
