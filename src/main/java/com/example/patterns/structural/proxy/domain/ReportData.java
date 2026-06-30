package com.example.patterns.structural.proxy.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报表数据（查询结果）。
 *
 * <p>承载报表查询服务一次计算后的可观察结果，包括报表类型、统计维度描述、订单总数、
 * 销售总额、报表生成时间与本次真实计算耗时。作为
 * {@link com.example.patterns.structural.proxy.ReportQueryService#query} 的返回值，
 * 供调用方据此展示报表并观察代理的缓存效果。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时提供 {@link #of} 静态工厂方法，便于查询服务以表达力更强的方式构造结果。</p>
 *
 * <p>代理模式相关：{@link #generatedAt} 与 {@link #computeCostMillis} 是观察缓存是否命中的
 * 关键信号——重复以相同条件查询时，若代理命中缓存复用同一结果对象，则二者保持不变，
 * 说明并未重新执行高成本计算；反之每次重新计算都会刷新生成时间。</p>
 *
 * @since 1.0.0
 */
@Data
public class ReportData {

    /**
     * 报表类型。
     *
     * <p>取自查询请求所声明的报表类型，如 {@code "SALES"}、{@code "ORDER"}。</p>
     */
    private String reportType;

    /**
     * 统计维度描述。
     *
     * <p>对本次报表统计范围的可读描述，如「2024-01-01 ~ 2024-01-31」或叠加地区后的细分维度。</p>
     */
    private String dimension;

    /**
     * 订单总数。
     *
     * <p>本次报表统计区间内的订单数量。</p>
     */
    private long orderCount;

    /**
     * 销售总额（单位：元）。
     *
     * <p>采用 {@link BigDecimal} 以保证金额精度，避免浮点运算误差。</p>
     */
    private BigDecimal totalSales;

    /**
     * 报表生成时间。
     *
     * <p>记录该结果首次被真实计算出来的时刻；缓存命中复用同一对象时该时间不变，
     * 是观察缓存是否生效的可观察信号。</p>
     */
    private LocalDateTime generatedAt;

    /**
     * 本次真实计算耗时（毫秒）。
     *
     * <p>反映一次真实报表查询的计算成本；缓存命中时复用该值，借此体现缓存对高成本查询的优化价值。</p>
     */
    private long computeCostMillis;

    /**
     * 构建一份报表数据，并自动以当前时刻填充生成时间。
     *
     * @param reportType        报表类型
     * @param dimension         统计维度描述
     * @param orderCount        订单总数
     * @param totalSales        销售总额（单位：元）
     * @param computeCostMillis 本次真实计算耗时（毫秒）
     * @return 各字段填充完成、生成时间为当前时刻的报表数据
     */
    public static ReportData of(String reportType, String dimension, long orderCount,
                                BigDecimal totalSales, long computeCostMillis) {
        ReportData data = new ReportData();
        data.setReportType(reportType);
        data.setDimension(dimension);
        data.setOrderCount(orderCount);
        data.setTotalSales(totalSales);
        data.setComputeCostMillis(computeCostMillis);
        data.setGeneratedAt(LocalDateTime.now());
        return data;
    }
}
