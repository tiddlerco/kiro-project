package com.example.patterns.common.demo;

import java.util.List;

/**
 * 演示入口贡献者接口。
 *
 * <p>采用「各模式自贡献独立 Bean + 注册表聚合」机制：每个设计模式模块各自实现一个
 * 贡献者 Bean，仅贡献自身的演示入口条目，由 {@link DemoEntryRegistry} 统一聚合。</p>
 *
 * <p>该设计避免多个模式并行开发时争抢修改同一集中清单文件，同时遵循开闭原则——
 * 新增模式只需新增贡献者实现，无需改动既有聚合逻辑。</p>
 *
 * @since 1.0.0
 */
public interface DemoEntryContributor {

    /**
     * 贡献本模块的全部演示入口条目。
     *
     * @return 本模块的演示入口条目列表；无入口时应返回空列表而非 {@code null}
     */
    List<DemoEntry> contribute();
}
