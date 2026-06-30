package com.example.patterns.creational.singleton.service;

import com.example.patterns.creational.singleton.GlobalConfigManager;
import com.example.patterns.creational.singleton.LocalCacheManager;
import com.example.patterns.creational.singleton.domain.SingletonComparisonResult;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 单例模式演示服务。
 *
 * <p>承载「Spring 单例 Bean」与「经典手写单例」两种实现的对比逻辑，对外提供：</p>
 * <ul>
 *     <li>对全局配置管理器（Spring 单例）的读写委派，支撑配置设置 / 读取演示入口；</li>
 *     <li>验证「同一入口多次获取返回同一实例（引用相等）」并对比两种单例的获取方式，
 *         产出 {@link SingletonComparisonResult}（满足需求 2.2）。</li>
 * </ul>
 *
 * <p>协作关系：经 {@code @Resource} 注入 Spring 单例 Bean {@link GlobalConfigManager}
 * 与 {@link ApplicationContext}（满足需求 10.1 的容器注入约定）；经典单例
 * {@link LocalCacheManager} 则经其静态 {@code getInstance()} 获取——二者获取方式的差异
 * 正是本演示要对比的核心。</p>
 *
 * @since 1.0.0
 */
@Service
public class SingletonDemoService {

    /**
     * 全局配置管理器（Spring 单例方式）。
     *
     * <p>由 Spring 容器注入，容器默认的单例作用域保证其全应用唯一。</p>
     */
    @Resource
    private GlobalConfigManager globalConfigManager;

    /**
     * Spring 应用上下文。
     *
     * <p>用于演示「自容器多次获取同一 Bean 均返回同一实例」，以真实体现容器对单例的保证。</p>
     */
    @Resource
    private ApplicationContext applicationContext;

    /**
     * 设置一项全局配置。
     *
     * @param key   配置键，不可为 {@code null}
     * @param value 配置值，不可为 {@code null}
     */
    public void setGlobalConfig(String key, String value) {
        globalConfigManager.setConfig(key, value);
    }

    /**
     * 读取一项全局配置。
     *
     * @param key 配置键，不可为 {@code null}
     * @return 配置值；当该键尚未设置时返回 {@code null}
     */
    public String getGlobalConfig(String key) {
        return globalConfigManager.getConfig(key);
    }

    /**
     * 对比两种单例实现，验证「同一入口多次获取返回同一实例」并汇总二者获取方式的差异。
     *
     * @return 两种单例实现的对比结果
     */
    public SingletonComparisonResult compareSingletonAcquisition() {
        SingletonComparisonResult result = new SingletonComparisonResult();
        result.setSpringBeanName(GlobalConfigManager.class.getSimpleName());
        result.setSpringBeanSameInstance(isSpringBeanSameInstance());
        result.setSpringAcquireWay("由 Spring 容器托管，经依赖注入或 ApplicationContext#getBean 获取，容器的单例作用域保证全应用唯一");
        result.setClassicSingletonName(LocalCacheManager.class.getSimpleName());
        result.setClassicSingletonSameInstance(isClassicSingletonSameInstance());
        result.setClassicAcquireWay("经静态方法 LocalCacheManager#getInstance 获取，双重检查锁（DCL）保证全进程唯一");
        return result;
    }

    /**
     * 验证 Spring 单例 Bean 自同一容器多次获取是否为同一实例。
     *
     * @return 两次获取引用相等返回 {@code true}，否则返回 {@code false}
     */
    private boolean isSpringBeanSameInstance() {
        GlobalConfigManager firstAcquired = applicationContext.getBean(GlobalConfigManager.class);
        GlobalConfigManager secondAcquired = applicationContext.getBean(GlobalConfigManager.class);
        // 单例性的本质是「是否为同一实例」，必须使用引用比较（==）；此处不可用 Objects.equals（其表达的是值相等）
        return firstAcquired == secondAcquired;
    }

    /**
     * 验证经典单例多次调用 {@code getInstance()} 是否为同一实例。
     *
     * @return 两次获取引用相等返回 {@code true}，否则返回 {@code false}
     */
    private boolean isClassicSingletonSameInstance() {
        LocalCacheManager firstAcquired = LocalCacheManager.getInstance();
        LocalCacheManager secondAcquired = LocalCacheManager.getInstance();
        // 同上：验证是否为同一实例必须使用引用比较（==）
        return firstAcquired == secondAcquired;
    }
}
