package com.example.patterns.creational.singleton;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置管理器（单例模式 —— Spring 单例 Bean 实现方式）。
 *
 * <p>业务场景：承载应用运行期的全局配置项（如功能开关、阈值、文案模板等），供应用各处读写、
 * 共享同一份配置。本类不编写任何单例控制代码，而是依托 Spring 容器默认的单例作用域
 * （singleton scope）保证「全应用唯一实例」——这正是单例模式在 Spring 生态中最自然的落地方式。</p>
 *
 * <p>与经典手写单例 {@link LocalCacheManager} 的对照：</p>
 * <ul>
 *     <li>实例化时机与生命周期由 Spring 容器托管，无需私有构造方法与静态持有者；</li>
 *     <li>经依赖注入（{@code @Resource}）或 {@code ApplicationContext#getBean} 获取，
 *         而非静态 {@code getInstance()}；</li>
 *     <li>天然支持依赖注入、AOP 增强、生命周期回调等容器能力。</li>
 * </ul>
 *
 * <p>线程安全：内部以 {@link ConcurrentHashMap} 存储配置项，保证多线程并发读写的安全性。
 * 需注意 {@link ConcurrentHashMap} 不允许 {@code null} 键或值，调用方须保证传入的键、值非空。</p>
 *
 * @since 1.0.0
 */
@Component
public class GlobalConfigManager {

    /**
     * 全局配置存储。
     *
     * <p>采用 {@link ConcurrentHashMap} 支持多线程并发读写；声明为 {@code final}，
     * 保证容器构造该单例 Bean 后此存储引用不再变更。</p>
     */
    private final Map<String, String> configMap = new ConcurrentHashMap<>();

    /**
     * 读取指定配置项的值。
     *
     * @param key 配置键，不可为 {@code null}
     * @return 配置值；当该键尚未设置时返回 {@code null}
     */
    public String getConfig(String key) {
        return configMap.get(key);
    }

    /**
     * 设置（新增或覆盖）一项全局配置。
     *
     * @param key   配置键，不可为 {@code null}
     * @param value 配置值，不可为 {@code null}
     */
    public void setConfig(String key, String value) {
        configMap.put(key, value);
    }
}
