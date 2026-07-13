package com.example.patterns.structural.proxy;

import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 代理模式真实主题核心行为单元测试（对应任务 9.3，验证需求 11.4、3.1）。
 *
 * <p>直接针对真实主题 {@link ReportQueryServiceImpl}（不经 AOP 切面），
 * 验证其纯查询的确定性核心行为：相同查询条件恒返回一致的订单总数与销售总额（同参幂等）。
 * 这一确定性正是代理缓存「命中复用与首次一致」得以成立的业务前提。</p>
 */
class ReportQueryServiceImplTest {

    /** 被测真实主题（纯查询逻辑，不含缓存与限流）。 */
    private final ReportQueryServiceImpl reportQueryService = new ReportQueryServiceImpl();

    /**
     * 构造一个报表查询请求。
     *
     * @param reportType 报表类型
     * @param startDate  统计开始日期
     * @param endDate    统计结束日期
     * @param region     统计地区维度（可为 {@code null}）
     * @return 各字段填充完成的报表查询请求
     */
    private ReportQueryRequest newRequest(String reportType, String startDate, String endDate, String region) {
        ReportQueryRequest request = new ReportQueryRequest();
        request.setReportType(reportType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRegion(region);
        return request;
    }

    @Test
    @DisplayName("相同查询条件两次查询返回一致的订单数与销售额（同参幂等）")
    void shouldReturnConsistentResultForSameCondition() {
        ReportQueryRequest request = newRequest("SALES", "2024-01-01", "2024-01-31", null);

        ReportData first = reportQueryService.query(request);
        ReportData second = reportQueryService.query(request);

        assertThat(second.getOrderCount()).isEqualTo(first.getOrderCount());
        assertThat(second.getTotalSales()).isEqualByComparingTo(first.getTotalSales());
        assertThat(second.getReportType()).isEqualTo(first.getReportType());
        assertThat(second.getDimension()).isEqualTo(first.getDimension());
    }

    @Test
    @DisplayName("带地区维度的相同条件同样保持确定性一致")
    void shouldReturnConsistentResultWhenRegionPresent() {
        ReportQueryRequest request = newRequest("ORDER", "2024-02-01", "2024-02-29", "华东");

        ReportData first = reportQueryService.query(request);
        ReportData second = reportQueryService.query(request);

        assertThat(second.getOrderCount()).isEqualTo(first.getOrderCount());
        assertThat(second.getTotalSales()).isEqualByComparingTo(first.getTotalSales());
        assertThat(first.getDimension()).contains("华东");
    }

    @Test
    @DisplayName("查询结果字段与请求条件对应且订单数销售额非负")
    void shouldBuildResultFieldsFromRequest() {
        ReportQueryRequest request = newRequest("SALES", "2024-03-01", "2024-03-31", null);

        ReportData data = reportQueryService.query(request);

        assertThat(data.getReportType()).isEqualTo("SALES");
        assertThat(data.getDimension()).isEqualTo("2024-03-01 ~ 2024-03-31");
        assertThat(data.getOrderCount()).isGreaterThanOrEqualTo(0L);
        assertThat(data.getTotalSales().signum()).isGreaterThanOrEqualTo(0);
        assertThat(data.getGeneratedAt()).isNotNull();
    }
}
