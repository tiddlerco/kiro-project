package com.example.patterns.structural.proxy;

import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.aspectj.lang.ProceedingJoinPoint;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 代理缓存幂等一致性属性测试（对应任务 9.4，Property 6，验证需求 3.1）。
 *
 * <p>以真实切面 {@link CacheRateLimitAspect} 代理真实主题 {@link ReportQueryServiceImpl}，
 * 借助对 {@link ProceedingJoinPoint} 的模拟驱动切面的环绕通知：随机构造查询参数，
 * 断言缓存有效期内以相同参数重复查询，第二次命中缓存并复用首次结果，核心业务字段与首次完全一致。</p>
 *
 * <p>为聚焦缓存幂等、隔离限流干扰，每次迭代均新建被测切面与真实主题实例（限流窗口全新），
 * 且单一参数的调用次数控制在限流阈值内（每次迭代仅调用两次）。</p>
 */
class ReportQueryCacheIdempotencyPropertyTest {

    /**
     * 生成报表类型候选值。
     *
     * @return 报表类型 Arbitrary
     */
    @Provide
    Arbitrary<String> reportTypes() {
        return Arbitraries.of("SALES", "ORDER", "REFUND", "TRAFFIC");
    }

    /**
     * 生成统计日期字符串候选值。
     *
     * @return 日期字符串 Arbitrary
     */
    @Provide
    Arbitrary<String> dates() {
        return Arbitraries.of("2024-01-01", "2024-01-31", "2024-02-15", "2024-03-31", "2024-06-30");
    }

    /**
     * 生成地区维度候选值（含空值以覆盖不区分地区的场景）。
     *
     * @return 地区维度 Arbitrary
     */
    @Provide
    Arbitrary<String> regions() {
        return Arbitraries.of("华东", "华北", "华南", null);
    }

    /**
     * 依据切面与真实主题执行一次经代理的报表查询。
     *
     * <p>以模拟连接点承载查询入参并将目标调用委派至真实主题，从而在不启动 Spring 容器的前提下
     * 真实触发切面「先限流后缓存」的环绕逻辑。</p>
     *
     * @param aspect  被测缓存限流切面
     * @param subject 真实主题
     * @param request 报表查询请求
     * @return 经代理返回的报表数据（命中缓存时为既有对象，否则为新计算结果）
     * @throws Throwable 环绕通知或目标方法执行过程中抛出的异常
     */
    private ReportData queryViaProxy(CacheRateLimitAspect aspect,
                                     ReportQueryServiceImpl subject,
                                     ReportQueryRequest request) throws Throwable {
        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Mockito.when(joinPoint.getArgs()).thenReturn(new Object[]{request});
        Mockito.when(joinPoint.proceed()).thenAnswer(invocation -> subject.query(request));
        return (ReportData) aspect.aroundQuery(joinPoint);
    }

    // Feature: design-patterns-showcase, Property 6: 代理缓存幂等一致性
    // 说明：jqwik 引擎禁止 @Property 方法携带 JUnit 的 @DisplayName（否则该属性方法会被跳过而不执行），
    // 故此处改用 jqwik 等价注解 @Label 承载中文描述，与工程内其余属性测试保持一致。
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    @Label("同参重复查询命中缓存并返回与首次一致的结果")
    void shouldReturnConsistentResultOnRepeatedQuery(@ForAll("reportTypes") String reportType,
                                                     @ForAll("dates") String startDate,
                                                     @ForAll("dates") String endDate,
                                                     @ForAll("regions") String region) throws Throwable {
        CacheRateLimitAspect aspect = new CacheRateLimitAspect();
        ReportQueryServiceImpl subject = new ReportQueryServiceImpl();

        ReportQueryRequest request = new ReportQueryRequest();
        request.setReportType(reportType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRegion(region);

        ReportData first = queryViaProxy(aspect, subject, request);
        ReportData second = queryViaProxy(aspect, subject, request);

        assertThat(second).isSameAs(first);
        assertThat(second.getOrderCount()).isEqualTo(first.getOrderCount());
        assertThat(second.getTotalSales()).isEqualByComparingTo(first.getTotalSales());
        assertThat(second.getReportType()).isEqualTo(first.getReportType());
        assertThat(second.getDimension()).isEqualTo(first.getDimension());
        assertThat(second.getGeneratedAt()).isEqualTo(first.getGeneratedAt());
    }
}
