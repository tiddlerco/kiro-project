package com.example.patterns.behavioral.chain;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 责任链模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献责任链模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：责任链模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class ChainDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：行为型。
     */
    private static final String CATEGORY = "行为型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "责任链 Chain of Responsibility";

    /**
     * 风控校验演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/chain/riskCheck";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交用户标识、交易金额与近期交易次数，演示请求沿黑名单、金额上限、频次规则链依次传递，直至被某节点拦截短路或通过全部节点";

    /**
     * 贡献责任链模式的演示入口条目。
     *
     * <p>仅包含风控校验这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 责任链模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
