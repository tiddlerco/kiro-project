package com.example.patterns.structural.proxy;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 代理 AOP 织入与限流集成测试（对应任务 9.5，验证需求 10.2、3.1）。
 *
 * <p>以 {@link SpringBootTest} 启动真实 Spring 上下文，通过注入的 {@link ReportQueryService}
 * （已被切面 {@link CacheRateLimitAspect} 动态代理）验证两项横切能力真实生效：</p>
 * <ol>
 *     <li><b>缓存命中复用</b>：相同参数第二次调用命中缓存，复用首次结果对象，报表生成时间不变。</li>
 *     <li><b>限流拒绝</b>：单个时间窗口内高频调用超过阈值后抛出 {@link ServiceException}（提示「请求过于频繁」）。</li>
 * </ol>
 *
 * <p>限流断言的稳定构造：切面遵循「先限流后缓存」，缓存命中虽不穿透到真实查询，但仍先经过限流计数。
 * 因此以「相同参数连续调用」触发限流——首次调用未命中缓存承担一次真实计算，其余调用命中缓存近乎瞬时返回，
 * 使全部调用稳定落在同一时间窗口内累计计数，直至越过阈值触发限流。此法既稳定可复现，又直观体现
 * 「限流置于最外层、缓存命中亦受限流约束」的织入顺序。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProxyAspectIntegrationTest {

    /** 切面缓存字段名，用于测试前重置缓存以隔离用例。 */
    private static final String CACHE_FIELD = "reportCache";

    /** 切面限流计数字段名，用于测试前重置计数以隔离用例。 */
    private static final String REQUEST_COUNT_FIELD = "requestCountInWindow";

    /** 切面窗口起始时刻字段名，用于测试前重置窗口以隔离用例。 */
    private static final String WINDOW_START_FIELD = "windowStartMillis";

    /** 超过限流阈值（5）所需的连续调用次数。 */
    private static final int OVER_LIMIT_CALL_COUNT = 6;

    /** 被测报表查询服务（由 Spring AOP 织入缓存与限流后的代理对象）。 */
    @Resource
    private ReportQueryService reportQueryService;

    /** 缓存限流切面 Bean，用于每个用例前重置内部状态，保证断言稳定可复现。 */
    @Resource
    private CacheRateLimitAspect cacheRateLimitAspect;

    /**
     * 每个用例执行前重置切面内部状态。
     *
     * <p>清空结果缓存、归零限流计数并重开时间窗口，使各用例互不干扰、限流与缓存断言稳定可复现。</p>
     */
    @BeforeEach
    void resetAspectState() {
        Object cache = ReflectionTestUtils.getField(cacheRateLimitAspect, CACHE_FIELD);
        if (cache instanceof Map) {
            ((Map<?, ?>) cache).clear();
        }
        ReflectionTestUtils.setField(cacheRateLimitAspect, REQUEST_COUNT_FIELD, 0);
        ReflectionTestUtils.setField(cacheRateLimitAspect, WINDOW_START_FIELD, System.currentTimeMillis());
    }

    /**
     * 构造一个报表查询请求。
     *
     * @param reportType 报表类型
     * @param startDate  统计开始日期
     * @param endDate    统计结束日期
     * @return 各字段填充完成的报表查询请求
     */
    private ReportQueryRequest newRequest(String reportType, String startDate, String endDate) {
        ReportQueryRequest request = new ReportQueryRequest();
        request.setReportType(reportType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }

    @Test
    @DisplayName("相同参数第二次调用命中缓存，复用首次结果且生成时间不变")
    void shouldHitCacheOnSecondQuery() {
        ReportQueryRequest request = newRequest("SALES", "2024-01-01", "2024-01-31");

        ReportData first = reportQueryService.query(request);
        ReportData second = reportQueryService.query(request);

        assertThat(second.getGeneratedAt()).isEqualTo(first.getGeneratedAt());
        assertThat(second.getComputeCostMillis()).isEqualTo(first.getComputeCostMillis());
        assertThat(second.getOrderCount()).isEqualTo(first.getOrderCount());
        assertThat(second.getTotalSales()).isEqualByComparingTo(first.getTotalSales());
    }

    @Test
    @DisplayName("单窗口内高频调用超过阈值抛出请求过于频繁异常")
    void shouldRejectWhenExceedRateLimit() {
        ReportQueryRequest request = newRequest("TRAFFIC", "2024-05-01", "2024-05-31");

        assertThatThrownBy(() -> {
            for (int i = 0; i < OVER_LIMIT_CALL_COUNT; i++) {
                reportQueryService.query(request);
            }
        }).isInstanceOf(ServiceException.class)
                .hasMessageContaining("请求过于频繁");
    }
}
