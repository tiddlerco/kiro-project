package com.example.patterns.creational.builder;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 建造者模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献建造者模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：建造者模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class BuilderDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：创建型。
     */
    private static final String CATEGORY = "创建型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "建造者 Builder";

    /**
     * 通知消息构建演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/builder/buildNotification";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交接收人（必选）与标题/正文/附件/优先级（可选），演示经统一入口分步设置部件、收尾校验必选部件后构建出不可变通知消息；缺失接收人时返回错误";

    /**
     * 贡献建造者模式的演示入口条目。
     *
     * <p>仅包含通知消息构建这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 建造者模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
