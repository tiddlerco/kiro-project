# design-patterns-showcase

基于 Spring Boot 的 GoF 设计模式实战示例工程。项目以 HTTP 接口形式演示创建型、结构型、行为型设计模式在业务场景中的落地方式，并配套单元测试、属性测试、H2 内存库和 MyBatis XML 持久化示例。

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Maven
- Spring MVC
- Spring AOP
- MyBatis Spring Boot Starter 2.3.2
- H2 内存数据库
- JUnit 5
- jqwik 属性测试
- Lombok

## 项目结构

```text
.
├── pom.xml
├── run-app-jdk8.sh
├── src
│   ├── main
│   │   ├── java/com/example/patterns
│   │   │   ├── PatternsShowcaseApplication.java
│   │   │   ├── common
│   │   │   ├── creational
│   │   │   ├── structural
│   │   │   └── behavioral
│   │   └── resources
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       ├── data.sql
│   │       └── mapper
│   └── test/java/com/example/patterns
```

### 核心目录说明

- `common`：通用返回对象、基础控制器、异常处理、权限切面、演示入口注册能力。
- `creational`：创建型模式示例，包括单例、建造者、工厂方法、抽象工厂。
- `structural`：结构型模式示例，包括适配器、装饰器、外观、代理。
- `behavioral`：行为型模式示例，包括策略、模板方法、观察者、命令、状态、责任链。
- `resources/mapper`：MyBatis XML SQL 文件，当前覆盖状态模式和命令模式。
- `resources/schema.sql`、`resources/data.sql`：H2 内存库建表和演示数据初始化脚本。
- `src/test/java`：JUnit 5 单元测试和 jqwik 属性测试。

## 已实现模式

| 类型 | 模式 | 示例场景 | 入口路径 |
| --- | --- | --- | --- |
| 创建型 | 单例 Singleton | 全局配置、缓存管理 | `/pattern/singleton` |
| 创建型 | 建造者 Builder | 通知消息构建 | `/pattern/builder/buildNotification` |
| 创建型 | 工厂方法 Factory Method | 支付处理器选择 | `/pattern/factory/pay` |
| 创建型 | 抽象工厂 Abstract Factory | 云存储客户端与签名器族 | `/pattern/abstractfactory/upload` |
| 结构型 | 适配器 Adapter | 多厂商短信发送统一接口 | `/pattern/adapter/sendSms` |
| 结构型 | 装饰器 Decorator | 通知发送能力增强 | `/pattern/decorator/send` |
| 结构型 | 外观 Facade | 下单流程编排 | `/pattern/facade/placeOrder` |
| 结构型 | 代理 Proxy | 报表查询缓存与限流 | `/pattern/proxy/queryReport` |
| 行为型 | 策略 Strategy | 营销优惠计算 | `/pattern/strategy/calculate` |
| 行为型 | 模板方法 Template Method | 支付渠道对账 | `/pattern/template/reconcile` |
| 行为型 | 观察者 Observer | 订单状态变更事件通知 | `/pattern/observer/changeOrderStatus` |
| 行为型 | 命令 Command | 商品增删改与撤销 | `/pattern/command/execute` |
| 行为型 | 状态 State | 订单状态流转 | `/pattern/order/changeStatus` |
| 行为型 | 责任链 Chain of Responsibility | 风控规则链校验 | `/pattern/chain/riskCheck` |

说明：配置和数据库脚本中已预留 `risk_rule_dict` 表及享元模式相关注释，但当前源码中尚未看到独立的 `flyweight` 实现包和接口入口，因此 README 仅将已存在源码的模式列为已实现。

## 运行方式

### 环境要求

- JDK 8
- Maven 3.6+

### 启动应用

```bash
mvn spring-boot:run
```

如果本机存在多个 JDK，可以使用项目中的临时脚本指定 JDK 8：

```bash
./run-app-jdk8.sh
```

应用默认监听端口：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
```

H2 连接信息：

```text
JDBC URL: jdbc:h2:mem:patterns;DB_CLOSE_DELAY=-1;MODE=MySQL
User Name: sa
Password:
```

## 测试

执行全部测试：

```bash
mvn test
```

当前测试重点覆盖：

- 策略模式优惠金额不变量。
- 责任链执行顺序和短路行为。
- 适配器委派行为。
- 装饰器能力组合。

## 数据与持久化

项目启动时会自动执行：

- `src/main/resources/schema.sql`：创建演示表结构。
- `src/main/resources/data.sql`：写入演示数据。

MyBatis 约定：

- Mapper 接口位于各业务模式自己的 `mapper` 包。
- SQL 统一放在 `src/main/resources/mapper/**/*.xml`。
- 已开启下划线列名到驼峰属性名自动映射。

## 文档位置

每个模式目录下都有独立说明文档：

```text
src/main/java/com/example/patterns/**/doc/*.md
```

这些文档适合配合源码阅读，用来理解模式意图、业务场景、核心角色和扩展点。

## 扩展建议

新增设计模式示例时，建议保持现有结构：

1. 在对应类别目录下新增模式包，例如 `creational/newpattern`。
2. 按需拆分 `controller`、`service`、`domain`、`mapper`、`doc` 等子目录。
3. 如果涉及数据库访问，Mapper SQL 必须写在 XML 中，并补充初始化脚本。
4. 新增 `DemoEntryContributor`，让模式出现在统一演示入口中。
5. 为核心行为补充单元测试；涉及规则组合、不变量或边界条件时优先补充属性测试。

## 注意事项

- H2 控制台只适合本地演示，生产环境必须关闭。
- 当前工程定位是设计模式教学与验证，不是完整业务系统。
- `run-app-jdk8.sh` 中写死了本机 JDK 路径，换机器运行前需要确认路径是否存在。
