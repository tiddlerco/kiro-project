/**
 * 单例（Singleton）模式示例模块。
 *
 * <p>业务场景：全局配置与本地缓存管理器。本模块刻意提供两种单例实现以对比差异，
 * 帮助学习者理解「框架托管单例」与「语言机制手写单例」的取舍：</p>
 * <ul>
 *     <li>Spring 单例方式：{@code GlobalConfigManager} 作为 Spring 默认作用域（singleton）Bean，
 *         由容器保证全应用唯一，经依赖注入或 {@code ApplicationContext#getBean} 获取。</li>
 *     <li>经典单例方式：{@code LocalCacheManager} 以双重检查锁（DCL）+ {@code volatile} 手写实现，
 *         经静态方法 {@code getInstance()} 获取。</li>
 * </ul>
 *
 * <p>{@code service.SingletonDemoService} 承载二者的对比逻辑，验证「同一入口多次获取返回同一实例
 * （引用相等）」，并产出 {@code domain.SingletonComparisonResult} 对比结果，供演示入口观察。</p>
 *
 * <p>对应需求：2.1（两种单例实现以对比差异）、2.2（多次获取引用相等）、10.1（容器注入协作对象）。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.creational.singleton;
