package com.example.patterns.behavioral.state;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 状态模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献状态模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：状态模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class StateDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：行为型。
     */
    private static final String CATEGORY = "行为型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "状态 State";

    /**
     * 订单状态流转演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/order/changeStatus";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "对订单执行 pay/ship/complete/cancel，合法流转转入目标状态，非法流转被拒绝且状态不变";

    /**
     * 贡献状态模式的演示入口条目。
     *
     * <p>仅包含订单状态流转这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 状态模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
