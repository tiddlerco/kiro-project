/**
 * 行为型设计模式（Behavioral Patterns）示例包。
 *
 * <p>本包用于演示 GoF 行为型模式在企业级真实业务场景中的工程化落地，
 * 其核心关注点是「对象之间的职责分配与协作方式」，通过合理划分职责降低
 * 对象间耦合并提升可扩展性。</p>
 *
 * <p>计划纳入实现的模式及其业务场景（括号内为优先级）：</p>
 * <ul>
 *     <li>策略 Strategy（P0）：促销优惠计算</li>
 *     <li>模板方法 Template Method（P0）：对账/数据导入流程</li>
 *     <li>观察者 Observer（P0）：结合 Spring 事件机制的订单状态变更通知</li>
 *     <li>责任链 Chain of Responsibility（P0）：风控规则校验链</li>
 *     <li>状态 State（P0）：订单状态机流转</li>
 *     <li>命令 Command（P0）：后台操作封装、撤销与操作历史</li>
 *     <li>迭代器 Iterator（P1）：自定义分页结果集遍历</li>
 *     <li>中介者 Mediator（P1）：售后工单多方协作</li>
 *     <li>备忘录 Memento（P1）：草稿内容保存与恢复</li>
 * </ul>
 *
 * <p>每个模式以独立子包组织，子包内部按 controller、service、角色类、domain、doc 分层，
 * 以满足「一类一文件」「文档随模块」「演示入口可独立触发」等工程约束。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.behavioral;
