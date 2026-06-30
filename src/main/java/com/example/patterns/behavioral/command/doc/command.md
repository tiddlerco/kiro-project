# 命令模式 Command

业务场景：商品的增删改操作与撤销。每一次商品操作（新增 / 修改 / 删除）被封装为一个命令对象，由统一的调用者触发执行并落库为命令历史，且支持「撤销最近一次操作」将数据恢复至执行前状态。

## 一、解决的问题

在「操作 + 历史 + 撤销」的场景中，若不使用命令模式，常见做法是把「如何执行某操作」「如何记录历史」「如何撤销」全部塞进一个服务方法里：

```
if (类型 == 新增) { 新增; 记历史; }
if (类型 == 修改) { 存旧值; 修改; 记历史; }
if (类型 == 删除) { 存旧值; 删除; 记历史; }
// 撤销时再写一套对称的 if-else 逆向逻辑
```

这种写法会带来如下设计问题：

- **请求发起与执行强耦合**：调用方必须知道每种操作如何执行、如何记录、如何回滚，难以复用与扩展。
- **撤销逻辑分散且易错**：正向与逆向逻辑分处两地、靠类型判断对应，新增一种操作要同时改正向与逆向两套分支，回归风险高。
- **难以统一处理历史与排队**：历史记录、操作排队、批量撤销等横切诉求散落在各分支中，无法集中收敛。

命令模式把「每一种操作」抽象为一个命令对象，命令对象自身既知道如何 `execute()`（正向），也知道如何 `undo()`（逆向），并对外暴露执行前后的数据快照。调用者只面向命令接口编程，统一触发执行、落库历史、压栈以支持撤销，从而把「请求的发起」与「请求的执行 / 撤销 / 记录」彻底解耦。

## 二、遵循的设计原则

- **单一职责原则（SRP）**：具体命令只负责一种操作的正向与逆向逻辑（`AddProductCommand` / `UpdateProductCommand` / `DeleteProductCommand`）；调用者 `CommandInvoker` 只负责触发执行、落库历史与撤销编排；接收者 `ProductService` 只负责真正的数据增删改查；编排层 `ProductCommandService` 只负责按请求构造命令；控制器 `CommandController` 只负责路由分发。各角色职责清晰、互不越界。
- **开闭原则（OCP）**：新增一种商品操作只需新增一个实现 `OperationCommand` 的具体命令类，并在 `ProductCommandService` 的分发中接入，无需改动调用者与接收者。
- **依赖倒置原则（DIP）**：调用者 `CommandInvoker` 仅依赖抽象 `OperationCommand`，不感知任何具体命令实现；执行与撤销均通过接口方法完成。
- **里氏替换原则（LSP）**：所有具体命令均可作为 `OperationCommand` 被调用者统一驱动与压栈撤销，约定 `execute()` 完成正向操作并维护前后快照、`undo()` 执行对称的逆向恢复。

## 三、优点与适用及不适用场景

**优点：**

- 解耦请求发起方与执行方，调用方无需了解操作如何落地。
- 天然支持撤销 / 重做、操作历史、操作排队与批量执行等扩展。
- 每个命令职责单一、可独立测试，新增操作符合开闭原则。

**适用场景：**

- 需要将操作参数化、排队、记录日志或支持撤销 / 重做的场景（如编辑器、事务性批操作、审计留痕）。
- 请求发起方与执行方需要解耦、或操作需要延迟执行 / 重放的场景。

**不适用场景：**

- 操作种类极少且永不需要撤销、历史、排队等能力时，引入命令对象反而增加类数量与抽象成本，直接调用更直观。
- 逆向操作难以定义或代价过高（如不可逆的外部副作用）时，`undo()` 难以可靠实现，需另行设计补偿机制。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| Command（命令抽象） | 定义执行、撤销及前后快照的统一接口 | `com.example.patterns.behavioral.command.command.OperationCommand` |
| ConcreteCommand（具体命令） | 实现单种操作的正向执行与逆向撤销 | `AddProductCommand`、`UpdateProductCommand`、`DeleteProductCommand` |
| Invoker（调用者） | 触发命令执行、落库历史、压栈并支持撤销 | `com.example.patterns.behavioral.command.command.CommandInvoker` |
| Receiver（接收者） | 真正执行商品增删改查 | `com.example.patterns.behavioral.command.service.ProductService` |
| Client / 编排层 | 按请求构造对应命令并委派调用者执行 | `com.example.patterns.behavioral.command.service.ProductCommandService` |
| 演示入口（控制器） | 路由分发，暴露执行 / 撤销 / 删除 HTTP 入口 | `com.example.patterns.behavioral.command.controller.CommandController` |

## 五、删除接口为何用 GET + 显式确认标识 + 权限校验

删除接口 `GET /pattern/command/deleteProduct` 的设计是「团队 URL 约定」与「安全缓解」的折中产物：

- **为何用 GET**：本项目统一采用传统 URL 路径风格约定，删除操作刻意使用 `GET` 映射（而非 RESTful 的 `DELETE` 方法），以与团队既有接口风格保持一致（需求 9.7）。
- **GET 暴露删除的固有风险**：`GET` 请求可能被浏览器预取、被链接爬虫抓取或被用户无意点击，从而误触发删除这类有副作用的操作。
- **缓解一：必填的显式删除标识**：接口强制携带非空的删除目标标识 `productId` 与确认标识 `confirmDelete`。其中参数缺失由框架的必填参数约束直接拒绝（方法不执行、数据不变）；`confirmDelete` 是否为真的业务校验下沉到 `ProductCommandService.executeDelete`，非真即拒绝并保持数据不变（需求 9.10、9.11）。
- **缓解二：调用者权限校验**：方法标注 `@RequiresPermission("pattern:product:remove")`，由权限切面 `PermissionAspect` 在方法执行前校验当前用户（经 `MockUserContext` 解析）是否具备该权限；不具备时抛出 `PermissionDeniedException`，经全局异常处理器转换为错误响应，删除不执行、数据不变（需求 9.10、9.11）。

两道防线（确认标识为真 + 具备权限）必须同时满足，删除命令才会真正执行，从而在沿用团队 GET 删除约定的同时，将误触发与越权删除的风险降到可控范围。
