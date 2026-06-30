package com.example.patterns.structural.facade;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 外观模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献外观模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：外观模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class FacadeDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：结构型。
     */
    private static final String CATEGORY = "结构型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "外观 Facade";

    /**
     * 下单演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/facade/placeOrder";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交商品、数量、单价、下单用户与支付渠道，演示外观以单一入口编排库存/优惠/支付三个子系统完成整套下单";

    /**
     * 贡献外观模式的演示入口条目。
     *
     * <p>仅包含下单这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 外观模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
