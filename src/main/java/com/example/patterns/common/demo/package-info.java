/**
 * 演示入口聚合与启动清单展示包（Demo Entry）。
 *
 * <p>承载「各模式自贡献独立 Bean + 注册表聚合」的演示入口机制，满足需求 1.5：
 * 应用启动完成后在日志输出全部演示入口清单，每条至少含「设计模式名称 + 触发方式
 * （HTTP 路径或单测标识）」。</p>
 *
 * <p>该机制将清单来源拆分为「各模式自实现的贡献者 Bean」，由注册表通过 Spring 自动聚合，
 * 既避免并行开发时争抢修改同一集中清单文件，又遵循开闭原则——新增模式仅需新增贡献者实现。</p>
 *
 * <ul>
 *     <li>{@link com.example.patterns.common.demo.DemoEntry}：演示入口条目数据模型</li>
 *     <li>{@link com.example.patterns.common.demo.DemoEntryContributor}：各模式自实现的入口贡献者接口</li>
 *     <li>{@link com.example.patterns.common.demo.DemoEntryRegistry}：聚合全部贡献者条目的注册表</li>
 *     <li>{@link com.example.patterns.common.demo.DemoEntryPrinter}：监听应用就绪事件并打印清单的启动监听器</li>
 * </ul>
 *
 * @since 1.0.0
 */
package com.example.patterns.common.demo;
