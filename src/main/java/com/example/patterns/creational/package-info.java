/**
 * 创建型设计模式（Creational Patterns）示例包。
 *
 * <p>本包用于演示 GoF 创建型模式在企业级真实业务场景中的工程化落地，
 * 其核心关注点是「对象的创建过程」与「创建逻辑」的解耦，让调用方无需关心
 * 具体类的实例化细节。</p>
 *
 * <p>计划纳入实现的模式及其业务场景（括号内为优先级）：</p>
 * <ul>
 *     <li>单例 Singleton（P0）：全局配置与本地缓存管理器</li>
 *     <li>工厂方法 Factory Method（P0）：按支付渠道创建支付处理器</li>
 *     <li>抽象工厂 Abstract Factory（P0）：多云对象存储产品族</li>
 *     <li>建造者 Builder（P0）：订单/通知消息分步构建</li>
 *     <li>原型 Prototype（P1）：营销活动模板克隆复制</li>
 * </ul>
 *
 * <p>每个模式以独立子包组织，子包内部按 controller、service、角色类、domain、doc 分层，
 * 以满足「一类一文件」「文档随模块」「演示入口可独立触发」等工程约束。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.creational;
