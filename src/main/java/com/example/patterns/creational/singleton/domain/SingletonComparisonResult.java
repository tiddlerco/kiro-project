package com.example.patterns.creational.singleton.domain;

import lombok.Data;

/**
 * 单例获取方式对比结果。
 *
 * <p>承载「Spring 单例 Bean」与「经典手写单例」两种实现的对比信息，用以直观展示二者在
 * 「同一入口多次获取是否返回同一实例（引用相等）」上的一致表现，以及二者获取方式的差异。
 * 作为单例模式演示入口的可观察输出，支撑需求 2.2 的引用相等验证。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载业务逻辑，使用 Lombok {@code @Data} 生成
 * getter/setter/equals/hashCode/toString。</p>
 *
 * @since 1.0.0
 */
@Data
public class SingletonComparisonResult {

    /** Spring 单例方式的角色类名（如 {@code GlobalConfigManager}） */
    private String springBeanName;

    /** Spring 单例：自同一容器多次获取是否为同一实例（引用相等） */
    private boolean springBeanSameInstance;

    /** Spring 单例的获取方式说明 */
    private String springAcquireWay;

    /** 经典单例方式的角色类名（如 {@code LocalCacheManager}） */
    private String classicSingletonName;

    /** 经典单例：多次调用 {@code getInstance()} 是否为同一实例（引用相等） */
    private boolean classicSingletonSameInstance;

    /** 经典单例的获取方式说明 */
    private String classicAcquireWay;
}
