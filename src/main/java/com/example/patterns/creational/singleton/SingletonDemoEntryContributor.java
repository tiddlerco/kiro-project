package com.example.patterns.creational.singleton;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单例模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献单例模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：单例模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class SingletonDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：创建型。
     */
    private static final String CATEGORY = "创建型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "单例 Singleton";

    /**
     * 设置全局配置演示接口路径。
     */
    private static final String SET_CONFIG_ENDPOINT = "POST /pattern/singleton/setConfig";

    /**
     * 设置全局配置演示入口补充描述。
     */
    private static final String SET_CONFIG_DESCRIPTION = "提交配置键与配置值，向 Spring 单例 Bean（全局配置管理器）写入配置，演示容器托管单例全应用共享同一份配置";

    /**
     * 单例对比演示接口路径。
     */
    private static final String SAME_INSTANCE_ENDPOINT = "GET /pattern/singleton/sameInstance";

    /**
     * 单例对比演示入口补充描述。
     */
    private static final String SAME_INSTANCE_DESCRIPTION = "对比 Spring 单例 Bean 与经典手写单例，验证两者多次获取均返回同一实例（引用相等），并展示二者获取方式的差异";

    /**
     * 贡献单例模式的演示入口条目。
     *
     * <p>包含「设置全局配置」与「单例对比」两个 HTTP 演示入口，返回不可变列表，避免被外部修改。</p>
     *
     * @return 单例模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        List<DemoEntry> entries = new ArrayList<>();
        entries.add(DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, SET_CONFIG_ENDPOINT, SET_CONFIG_DESCRIPTION));
        entries.add(DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, SAME_INSTANCE_ENDPOINT, SAME_INSTANCE_DESCRIPTION));
        return Collections.unmodifiableList(entries);
    }
}
