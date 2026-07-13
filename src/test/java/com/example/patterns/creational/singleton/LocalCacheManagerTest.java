package com.example.patterns.creational.singleton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 经典单例 {@link LocalCacheManager} 核心行为单元测试（对应任务 3.3，验证需求 11.4 与需求 2.2）。
 *
 * <p>覆盖经典手写单例（双重检查锁 DCL）的核心行为：</p>
 * <ul>
 *     <li>多次调用静态 {@code getInstance()} 返回同一实例（引用相等），体现单例性；</li>
 *     <li>写入缓存项后可按键取回对应值（{@code put} 后 {@code get} 保真）；</li>
 *     <li>缓存项数量 {@code size()} 随写入正确累计，覆盖同键覆盖不增长的边界。</li>
 * </ul>
 *
 * <p>经典单例经其静态入口获取即可，无需启动 Spring 上下文，简洁可靠。</p>
 *
 * @since 1.0.0
 */
class LocalCacheManagerTest {

    /**
     * 验证多次调用 {@code getInstance()} 返回同一实例（引用相等）。
     *
     * <p>连续两次获取实例，断言二者为同一对象引用，证明双重检查锁单例全进程唯一。</p>
     */
    @Test
    @DisplayName("多次获取经典单例返回同一实例（引用相等）")
    void shouldReturnSameInstanceOnMultipleGetInstance() {
        LocalCacheManager firstAcquired = LocalCacheManager.getInstance();
        LocalCacheManager secondAcquired = LocalCacheManager.getInstance();

        // 单例性的本质是「是否为同一实例」，必须使用引用比较
        assertThat(secondAcquired).isSameAs(firstAcquired);
    }

    /**
     * 验证写入缓存项后可按键取回对应值（{@code put} 后 {@code get} 保真）。
     *
     * <p>向唯一实例写入一个键值对，断言按该键读取到的值与写入值相等（引用相等）。</p>
     */
    @Test
    @DisplayName("写入缓存后按键可取回对应值")
    void shouldReturnStoredValueAfterPut() {
        LocalCacheManager cacheManager = LocalCacheManager.getInstance();
        String cacheKey = "hotDict:city:" + System.nanoTime();
        Object cacheValue = "北京";

        cacheManager.put(cacheKey, cacheValue);

        assertThat(cacheManager.get(cacheKey)).isSameAs(cacheValue);
    }

    /**
     * 验证缓存项数量随写入正确累计，且同键覆盖不使数量增长。
     *
     * <p>以唯一键前缀避免与其它用例相互干扰：先记录写入前数量作为基线，写入两个不同键后断言数量增加 2，
     * 再以已存在的键覆盖写入，断言数量不变，验证 {@code size()} 统计的是不同键的数量。</p>
     */
    @Test
    @DisplayName("size 随不同键写入递增且同键覆盖不增长")
    void shouldCountDistinctKeysForSize() {
        LocalCacheManager cacheManager = LocalCacheManager.getInstance();
        String keyPrefix = "sizeCase:" + System.nanoTime() + ":";
        int baselineSize = cacheManager.size();

        cacheManager.put(keyPrefix + "a", "valueA");
        cacheManager.put(keyPrefix + "b", "valueB");
        assertThat(cacheManager.size()).isEqualTo(baselineSize + 2);

        // 以已存在的键覆盖写入，键数量不应增长
        cacheManager.put(keyPrefix + "a", "valueA2");
        assertThat(cacheManager.size()).isEqualTo(baselineSize + 2);
    }
}
