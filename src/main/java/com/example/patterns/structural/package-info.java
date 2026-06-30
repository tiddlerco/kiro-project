/**
 * 结构型设计模式（Structural Patterns）示例包。
 *
 * <p>本包用于演示 GoF 结构型模式在企业级真实业务场景中的工程化落地，
 * 其核心关注点是「类与对象的组合方式」，通过组合而非继承构建出更灵活、
 * 更易扩展的结构。</p>
 *
 * <p>计划纳入实现的模式及其业务场景（括号内为优先级）：</p>
 * <ul>
 *     <li>代理 Proxy（P0）：结合 Spring AOP 的接口缓存与限流</li>
 *     <li>适配器 Adapter（P0）：统一短信发送接口对接多服务商</li>
 *     <li>装饰器 Decorator（P0）：通知发送能力增强叠加</li>
 *     <li>外观 Facade（P0）：下单流程编排多个子系统</li>
 *     <li>桥接 Bridge（P1）：推送渠道与消息类型双维度解耦</li>
 *     <li>组合 Composite（P1）：组织架构/审批节点树统一处理</li>
 *     <li>享元 Flyweight（P1）：风控规则/数据字典共享复用</li>
 * </ul>
 *
 * <p>每个模式以独立子包组织，子包内部按 controller、service、角色类、domain、doc 分层，
 * 以满足「一类一文件」「文档随模块」「演示入口可独立触发」等工程约束。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.structural;
