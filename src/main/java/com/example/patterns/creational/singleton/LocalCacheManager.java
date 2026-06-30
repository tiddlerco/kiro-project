package com.example.patterns.creational.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存管理器（单例模式 —— 经典手写单例实现方式：双重检查锁 DCL）。
 *
 * <p>业务场景：在单机进程内维护一份共享的本地缓存（如热点字典、临时计算结果等），
 * 要求全进程唯一、随处可取且线程安全。本类不依赖任何框架，纯以 Java 语言机制实现单例，
 * 用以与 Spring 容器托管的单例 {@link GlobalConfigManager} 形成对照。</p>
 *
 * <p>单例实现：采用「双重检查锁（Double-Checked Locking, DCL）」。</p>
 * <ul>
 *     <li>{@link #instance} 以 {@code volatile} 修饰：既保证多线程间的可见性，
 *         又禁止「分配内存 → 赋值引用 → 执行构造」的指令重排，避免其它线程读到尚未完成初始化的半成品对象；</li>
 *     <li>{@link #getInstance()} 中两次判空：第一次避免实例已就绪后仍进入同步块的性能损耗，
 *         第二次在持有锁后再次确认，防止并发下重复创建实例。</li>
 * </ul>
 *
 * <p>设计权衡（教学提示）：DCL 写法能清晰展示线程安全的关键考量，但相较枚举单例，
 * 它无法天然抵御「反射强行调用私有构造方法」与「序列化/反序列化生成新实例」对单例性的破坏；
 * 若业务对此类场景敏感，应改用枚举单例。本示例聚焦演示线程安全细节，故采用 DCL。</p>
 *
 * <p>线程安全：实例创建由 DCL 保证；内部缓存以 {@link ConcurrentHashMap} 存储，保证并发读写安全。
 * 需注意 {@link ConcurrentHashMap} 不允许 {@code null} 键或值。</p>
 *
 * @since 1.0.0
 */
public class LocalCacheManager {

    /**
     * 全局唯一实例。
     *
     * <p>以 {@code volatile} 修饰，保证可见性并禁止指令重排，是双重检查锁正确性的关键所在。</p>
     */
    private static volatile LocalCacheManager instance;

    /**
     * 本地缓存存储。
     *
     * <p>采用 {@link ConcurrentHashMap} 支持并发读写；声明为 {@code final}，
     * 保证实例创建后此存储引用不再变更。</p>
     */
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * 私有构造方法。
     *
     * <p>限定为私有以禁止外部经 {@code new} 直接实例化，从而把实例创建的唯一入口收敛到
     * {@link #getInstance()}，这是单例模式得以成立的前提。</p>
     */
    private LocalCacheManager() {
    }

    /**
     * 获取本地缓存管理器的全局唯一实例（双重检查锁实现，线程安全）。
     *
     * @return 全进程唯一的本地缓存管理器实例
     */
    public static LocalCacheManager getInstance() {
        if (instance == null) {
            synchronized (LocalCacheManager.class) {
                if (instance == null) {
                    instance = new LocalCacheManager();
                }
            }
        }
        return instance;
    }

    /**
     * 写入（新增或覆盖）一个缓存项。
     *
     * @param key   缓存键，不可为 {@code null}
     * @param value 缓存值，不可为 {@code null}
     */
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    /**
     * 读取指定缓存项的值。
     *
     * @param key 缓存键，不可为 {@code null}
     * @return 缓存值；当该键不存在时返回 {@code null}
     */
    public Object get(String key) {
        return cache.get(key);
    }

    /**
     * 获取当前缓存项的数量。
     *
     * @return 当前缓存中键值对的数量
     */
    public int size() {
        return cache.size();
    }
}
