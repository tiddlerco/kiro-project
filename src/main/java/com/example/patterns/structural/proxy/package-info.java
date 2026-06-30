/**
 * 代理（Proxy）模式示例包。
 *
 * <p>业务场景：结合 Spring AOP 的报表查询「缓存 + 限流」代理。代理对象与被代理的真实主题
 * 实现同一接口，在不修改真实主题代码的前提下，由切面透明织入缓存命中复用与请求限流等横切逻辑
 * （满足需求 3.1、10.2）。</p>
 *
 * <p>参与角色与对应类：</p>
 * <ul>
 *     <li>抽象主题 Subject：{@link com.example.patterns.structural.proxy.ReportQueryService}</li>
 *     <li>真实主题 RealSubject：{@link com.example.patterns.structural.proxy.ReportQueryServiceImpl}（纯查询逻辑）</li>
 *     <li>代理 Proxy：{@link com.example.patterns.structural.proxy.CacheRateLimitAspect}（以 Spring AOP 充当，织入缓存与限流）</li>
 *     <li>数据对象：{@link com.example.patterns.structural.proxy.domain.ReportQueryRequest}、
 *         {@link com.example.patterns.structural.proxy.domain.ReportData}（位于 domain 子包）</li>
 * </ul>
 *
 * @since 1.0.0
 */
package com.example.patterns.structural.proxy;
