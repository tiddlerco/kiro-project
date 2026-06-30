/**
 * 抽象工厂（Abstract Factory）模式示例包。
 *
 * <p>业务场景：多云对象存储产品族的创建。系统对接多个云厂商（阿里云 OSS、AWS S3 等），
 * 每个厂商提供一整族相互关联的对象存储能力——存储客户端与 URL 签名器。抽象工厂确保按厂商
 * 选取工厂后，所得产品始终来自同一厂商、相互配套，不会出现「阿里云客户端 + AWS 签名器」的
 * 错配（对应需求 2.5、10.1）。</p>
 *
 * <p>角色与对应类：</p>
 * <ul>
 *     <li>抽象工厂 AbstractFactory：{@link com.example.patterns.creational.abstractfactory.CloudStorageFactory}</li>
 *     <li>具体工厂 ConcreteFactory：{@link com.example.patterns.creational.abstractfactory.AliyunStorageFactory}、
 *         {@link com.example.patterns.creational.abstractfactory.AwsStorageFactory}</li>
 *     <li>抽象产品 AbstractProductA：{@link com.example.patterns.creational.abstractfactory.StorageClient}</li>
 *     <li>抽象产品 AbstractProductB：{@link com.example.patterns.creational.abstractfactory.UrlSigner}</li>
 *     <li>具体产品 ConcreteProduct：{@link com.example.patterns.creational.abstractfactory.AliyunStorageClient}、
 *         {@link com.example.patterns.creational.abstractfactory.AliyunUrlSigner}、
 *         {@link com.example.patterns.creational.abstractfactory.AwsStorageClient}、
 *         {@link com.example.patterns.creational.abstractfactory.AwsUrlSigner}</li>
 *     <li>领域数据对象：{@link com.example.patterns.creational.abstractfactory.domain.UploadResult}</li>
 * </ul>
 *
 * <p>与 Spring 的结合：各具体产品以 {@code @Component} 交由容器管理，各具体工厂以
 * {@code @Resource} 按字段名精确注入本族产品（不在内部 {@code new}，满足需求 10.1），
 * 并在 {@code @PostConstruct} 阶段校验同族产品归属一致；新增云厂商只需新增其产品族与具体工厂，
 * 无需改动既有代码（开闭原则）。同族一致性以各产品自声明的 {@code vendor} 标识表达与校验。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.creational.abstractfactory;
