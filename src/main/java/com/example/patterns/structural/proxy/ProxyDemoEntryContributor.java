package com.example.patterns.structural.proxy;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 代理模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献代理模式自身的演示入口条目，由演示入口注册表统一聚合后
 * 输出到启动清单。遵循开闭原则：代理模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class ProxyDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：结构型。
     */
    private static final String CATEGORY = "结构型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "代理 Proxy";

    /**
     * 报表查询演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/proxy/queryReport";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交报表查询条件，由 Spring AOP 代理透明织入缓存与限流：重复调用观察缓存命中（生成时间不变），高频调用观察限流（超阈值返回错误）";

    /**
     * 贡献代理模式的演示入口条目。
     *
     * <p>仅包含报表查询这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 代理模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
