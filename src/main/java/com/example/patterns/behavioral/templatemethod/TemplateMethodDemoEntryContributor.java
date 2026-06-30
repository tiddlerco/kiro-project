package com.example.patterns.behavioral.templatemethod;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 模板方法模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献模板方法模式自身的演示入口条目，
 * 由演示入口注册表统一聚合后在启动清单中展示。模板方法模式通过对账场景演示
 * 「拉取 → 解析 → 比对 → 生成报告」骨架不变、各渠道步骤各异的设计意图，
 * 其触发入口为 {@code POST /pattern/template/reconcile}。</p>
 *
 * @since 1.0.0
 */
@Component
public class TemplateMethodDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：行为型。
     */
    private static final String CATEGORY = "行为型";

    /**
     * 模式名称：模板方法。
     */
    private static final String PATTERN_NAME = "模板方法 Template Method";

    /**
     * 对账演示入口的 HTTP 接口路径。
     */
    private static final String RECONCILE_ENDPOINT = "POST /pattern/template/reconcile";

    /**
     * 对账演示入口的补充描述。
     */
    private static final String RECONCILE_DESCRIPTION =
            "按渠道触发对账（如 channel=alipay/wechat），演示对账骨架不变、各渠道拉取与解析步骤各异";

    /**
     * 贡献模板方法模式的演示入口条目。
     *
     * <p>返回唯一的 HTTP 演示入口，描述对账接口的触发方式与渠道用法。</p>
     *
     * @return 仅含对账演示入口的不可变列表
     */
    @Override
    public List<DemoEntry> contribute() {
        return Collections.singletonList(
                DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, RECONCILE_ENDPOINT, RECONCILE_DESCRIPTION));
    }
}
