package com.example.patterns.behavioral.observer;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 观察者模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献观察者模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：观察者模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class ObserverDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：行为型。
     */
    private static final String CATEGORY = "行为型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "观察者 Observer";

    /**
     * 订单状态变更演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/observer/changeOrderStatus";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交订单号与状态变更，演示一次状态变更事件被短信通知、积分发放等多个监听者各自独立响应";

    /**
     * 贡献观察者模式的演示入口条目。
     *
     * <p>仅包含订单状态变更这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 观察者模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
