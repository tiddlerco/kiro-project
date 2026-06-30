# 抽象工厂模式 Abstract Factory

业务场景：多云对象存储产品族的创建。系统对接多个云厂商（阿里云 OSS、AWS S3 等），每个厂商提供一整族相互关联的对象存储能力——存储客户端（上传对象）与 URL 签名器（生成带时效的签名访问 URL）。上层按厂商选取工厂后，所得产品须始终来自同一厂商、相互配套，绝不能出现「阿里云客户端 + AWS 签名器」的错配。

## 一、解决的问题

在多云存储场景中，上层需要为同一笔操作同时使用「存储客户端」与「URL 签名器」两件相互关联的产品，且这两件产品必须归属同一云厂商才能正确协作。若不使用抽象工厂模式，常见做法是在调用方按厂商逐件 `new` 或逐件挑选产品：

```
if ("aliyun".equals(vendor)) {
    client = new AliyunStorageClient();
    signer = new AwsUrlSigner();   // 极易混入异厂商产品，造成错配
}
```

这种写法会带来如下设计问题：

- **产品族一致性无法保证**：客户端与签名器分别挑选，稍有疏忽就会混入异厂商产品，错配难以在编译期发现，往往到运行期才暴露。
- **违反开闭原则**：每新增一个云厂商，都要在调用方追加一段 `if-else` 创建逻辑，改动面大、回归风险高。
- **职责耦合**：「具体用哪个厂商的哪些产品」与「拿到产品后怎么用」全部挤在调用方，调用方被迫感知所有具体实现类。

抽象工厂模式把「创建一整族相互关联产品」的职责收敛到抽象工厂接口，每个云厂商以独立的具体工厂只生产本厂商产品族。调用方仅依赖抽象工厂与抽象产品接口，按厂商选取工厂后即可获得整族配套产品，从而把「用哪个厂商的产品族」与「怎么使用产品」彻底解耦，并由「同一工厂只产出同族产品」从结构上保证一致性。

## 二、遵循的设计原则

- **开闭原则（OCP）**：新增一个云厂商只需新增其产品族实现与一个具体工厂 `@Component`，即可经路由表自动接入，无需修改抽象工厂 `CloudStorageFactory`、客户端 `CloudStorageService` 与调用方 `CloudStorageController`。体现位置：`CloudStorageService#initFactoryRouting()` 以注入的 `List<CloudStorageFactory>` 自动建立「厂商 → 工厂」路由表。
- **依赖倒置原则（DIP）**：客户端与调用方仅依赖抽象工厂 `CloudStorageFactory` 与抽象产品 `StorageClient`、`UrlSigner`，不感知任何具体厂商实现。体现位置：`CloudStorageService` 字段 `List<CloudStorageFactory>` 面向接口注入；各具体工厂内部亦以 `@Resource` 注入抽象产品而非 `new`。
- **单一职责原则（SRP）**：具体工厂只负责「生产并返回本族产品」，存储客户端只负责「上传对象」，URL 签名器只负责「生成签名 URL」，客户端服务只负责「按厂商选工厂、驱动同族产品协作」，各司其职、互不混杂。
- **里氏替换原则（LSP）**：所有具体工厂均可作为 `CloudStorageFactory` 被统一选取与驱动，所有具体产品均可作为对应抽象产品被统一调用，替换任一厂商实现都不影响上层逻辑。

## 三、优点与适用及不适用场景

**优点：**

- 从结构上保证产品族一致性：同一工厂只产出同族产品，杜绝跨厂商错配。
- 解耦调用方与具体产品族，调用方无需感知任何具体实现类，仅面向抽象编程。
- 新增产品族（云厂商）符合开闭原则，扩展成本低，对既有代码零侵入。

**适用场景：**

- 系统需要创建一系列「相互关联、必须配套使用」的产品，且这些产品存在多个可切换的「族」（如多云厂商、多套 UI 皮肤、多数据库方言）。
- 希望将产品族的具体实现与使用方解耦，使切换或新增整族实现时调用方无感。

**不适用场景：**

- 产品族中只有单一产品、或产品之间并无「必须配套」的关联约束时，使用工厂方法乃至简单工厂即可，引入抽象工厂反而增加抽象成本。
- 产品族的「产品种类」需要频繁增减（如本要新增第三类产品）时，抽象工厂接口及其所有实现都要随之改动，扩展成本较高，需谨慎权衡。

## 四、参与角色与对应类

| 标准角色 | 职责 | 本示例对应类 |
| --- | --- | --- |
| AbstractFactory（抽象工厂） | 声明创建一整族相互关联产品的接口 | `com.example.patterns.creational.abstractfactory.CloudStorageFactory` |
| ConcreteFactory（具体工厂） | 只生产本厂商产品族，保证同族配套 | `AliyunStorageFactory`、`AwsStorageFactory` |
| AbstractProductA（抽象产品 A） | 抽象出「上传对象」的统一能力 | `com.example.patterns.creational.abstractfactory.StorageClient` |
| AbstractProductB（抽象产品 B） | 抽象出「生成签名访问 URL」的统一能力 | `com.example.patterns.creational.abstractfactory.UrlSigner` |
| ConcreteProduct（具体产品） | 实现单一厂商的某件产品 | `AliyunStorageClient`、`AliyunUrlSigner`、`AwsStorageClient`、`AwsUrlSigner` |
| Client（客户端） | 按厂商选工厂并驱动同族产品协作 | `com.example.patterns.creational.abstractfactory.service.CloudStorageService` |
| Request / Result（请求与结果对象） | 承载上传输入与「上传结果 + 签名 URL」输出 | `CloudUploadRequest`、`CloudUploadResult`、`domain.UploadResult` |

> 关于与 Spring 结合的说明：经典抽象工厂中，具体工厂通常以 `new` 在内部创建具体产品。本示例为契合 Spring 容器自动装配，将具体产品以 `@Component` 交由容器管理，具体工厂改以 `@Resource` 按字段名精确注入本族产品（不在内部 `new`），并在 `@PostConstruct` 阶段校验同族产品归属一致；客户端服务则以注入的 `List<CloudStorageFactory>` 自动建立厂商路由表。此为对经典结构的有意调整，目的是让产品与工厂均可被容器统一管理，并使新增厂商对既有代码零侵入。
