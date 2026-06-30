package com.example.patterns.structural.proxy;

import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;

/**
 * 报表查询服务接口（代理模式中的「抽象主题 Subject」角色）。
 *
 * <p>定义报表查询的统一契约。代理（由 Spring AOP 切面
 * {@link CacheRateLimitAspect} 充当）与真实主题
 * {@link ReportQueryServiceImpl} 实现同一接口，从而在不修改真实主题代码的前提下，
 * 由切面透明地织入缓存与限流等横切逻辑（满足需求 3.1、10.2）。</p>
 *
 * <p>调用方仅依赖本抽象接口而不感知缓存、限流的存在，符合依赖倒置原则；
 * 后续如需替换或增强代理逻辑，无需改动调用方与真实主题。</p>
 *
 * @since 1.0.0
 */
public interface ReportQueryService {

    /**
     * 按查询条件查询报表数据。
     *
     * <p>真实主题据此执行报表统计计算并返回结果；当被代理切面拦截时，相同查询条件可命中缓存
     * 直接复用既有结果，且整体调用速率受切面限流约束。</p>
     *
     * @param req 报表查询请求，承载报表类型、统计起止日期与可选地区维度
     * @return 报表数据，包含订单总数、销售总额、生成时间与计算耗时等可观察结果
     */
    ReportData query(ReportQueryRequest req);
}
