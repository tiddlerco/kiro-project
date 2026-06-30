package com.example.patterns.behavioral.strategy;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 策略模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献策略模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：策略模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class StrategyDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：行为型。
     */
    private static final String CATEGORY = "行为型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "策略 Strategy";

    /**
     * 促销计算演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/strategy/calculate";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "按策略类型标识（满减/折扣/立减）计算促销优惠结果，演示上下文按类型路由并委派具体策略";

    /**
     * 贡献策略模式的演示入口条目。
     *
     * <p>仅包含促销计算这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 策略模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
