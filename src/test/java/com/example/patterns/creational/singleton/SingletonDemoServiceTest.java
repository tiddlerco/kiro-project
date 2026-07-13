package com.example.patterns.creational.singleton;

import com.example.patterns.creational.singleton.domain.SingletonComparisonResult;
import com.example.patterns.creational.singleton.service.SingletonDemoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 单例对比服务 {@link SingletonDemoService} 核心行为单元测试（对应任务 3.3，验证需求 11.4 与需求 2.2）。
 *
 * <p>以 {@link SpringBootTest} 启动真实 Spring 上下文，通过注入的 {@link SingletonDemoService}
 * 调用 {@code compareSingletonAcquisition()}，验证两种单例实现的对比结果均表明「多次获取为同一实例」：</p>
 * <ul>
 *     <li>Spring 单例：自同一容器多次获取同一 Bean 返回同一实例（引用相等标志为 {@code true}）；</li>
 *     <li>经典单例：多次调用 {@code getInstance()} 返回同一实例（引用相等标志为 {@code true}）。</li>
 * </ul>
 *
 * <p>Spring 单例的单例性由容器默认的单例作用域保证，故采用 {@link SpringBootTest} 注入真实 Bean，
 * 以真实容器行为而非人为构造来断言，最能体现该实现方式的本质。</p>
 *
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SingletonDemoServiceTest {

    /** 被测单例对比服务（由 Spring 容器注入）。 */
    @Resource
    private SingletonDemoService singletonDemoService;

    /**
     * 验证对比结果表明两种单例实现多次获取均为同一实例，且角色类名填充正确。
     *
     * <p>断言 Spring 单例与经典单例的引用相等标志均为 {@code true}，并校验对比结果中两类角色类名
     * 分别为 {@link GlobalConfigManager} 与 {@link LocalCacheManager} 的简单类名、获取方式说明非空。</p>
     */
    @Test
    @DisplayName("对比结果表明两种单例多次获取均为同一实例")
    void shouldReportSameInstanceForBothSingletonStyles() {
        SingletonComparisonResult result = singletonDemoService.compareSingletonAcquisition();

        assertThat(result).isNotNull();
        // Spring 单例：同一容器多次获取为同一实例
        assertThat(result.isSpringBeanSameInstance()).isTrue();
        // 经典单例：多次 getInstance() 为同一实例
        assertThat(result.isClassicSingletonSameInstance()).isTrue();
        // 角色类名与获取方式说明填充正确
        assertThat(result.getSpringBeanName()).isEqualTo(GlobalConfigManager.class.getSimpleName());
        assertThat(result.getClassicSingletonName()).isEqualTo(LocalCacheManager.class.getSimpleName());
        assertThat(result.getSpringAcquireWay()).isNotBlank();
        assertThat(result.getClassicAcquireWay()).isNotBlank();
    }
}
