package com.example.patterns.structural.proxy.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 报表查询请求。
 *
 * <p>承载一次报表查询所需的业务条件（报表类型、统计起止日期、可选的地区维度），
 * 作为 {@link com.example.patterns.structural.proxy.ReportQueryService#query} 的入参，
 * 在调用方与报表查询服务之间传递查询要素。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何查询业务逻辑；getter/setter/equals/hashCode 由
 * Lombok 的 {@link Data} 注解生成。其上的 {@code @NotBlank} 校验注解供后续演示控制器
 * 以 {@code @Validated} 触发参数校验复用（与项目统一的请求对象校验规范一致）。</p>
 *
 * <p>代理模式相关：本对象额外提供 {@link #cacheKey()} 方法，由缓存限流切面
 * {@link com.example.patterns.structural.proxy.CacheRateLimitAspect} 据此判定相同查询条件
 * 是否命中缓存，从而复用既有计算结果、避免重复执行高成本查询。</p>
 *
 * @since 1.0.0
 */
@Data
public class ReportQueryRequest {

    /**
     * 报表类型。
     *
     * <p>标识所要统计的报表种类，如 {@code "SALES"}（销售报表）、{@code "ORDER"}（订单报表）。</p>
     */
    @NotBlank(message = "报表类型不能为空")
    private String reportType;

    /**
     * 统计开始日期。
     *
     * <p>以字符串表示（如 {@code "2024-01-01"}），界定报表统计区间的起点。</p>
     */
    @NotBlank(message = "统计开始日期不能为空")
    private String startDate;

    /**
     * 统计结束日期。
     *
     * <p>以字符串表示（如 {@code "2024-01-31"}），界定报表统计区间的终点。</p>
     */
    @NotBlank(message = "统计结束日期不能为空")
    private String endDate;

    /**
     * 统计地区维度（可选）。
     *
     * <p>用于将报表按地区进一步细分；为空时表示不区分地区的全量统计。</p>
     */
    private String region;

    /**
     * 生成用于缓存命中判定的缓存键。
     *
     * <p>将全部查询条件按固定顺序拼接为单一字符串，使「条件相同」与「缓存键相同」一一对应：
     * 切面据此在 {@link java.util.Map} 中查找既有结果，命中即复用、未命中再执行真实查询。
     * 采用稳定的字符串键而非直接以本可变对象作键，可避免对象被修改后影响缓存查找的隐患。
     * 各字段经 {@link String#valueOf(Object)} 处理以兼容空值。</p>
     *
     * @return 由全部查询条件拼接而成、可唯一标识一次查询条件的缓存键
     */
    public String cacheKey() {
        return String.join("|",
                String.valueOf(reportType),
                String.valueOf(startDate),
                String.valueOf(endDate),
                String.valueOf(region));
    }
}
