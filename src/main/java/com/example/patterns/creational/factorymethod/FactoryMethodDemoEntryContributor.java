package com.example.patterns.creational.factorymethod;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 工厂方法模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献工厂方法模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：工厂方法模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class FactoryMethodDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：创建型。
     */
    private static final String CATEGORY = "创建型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "工厂方法 Factory Method";

    /**
     * 按渠道支付演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/factory/pay";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交支付渠道标识及订单号、金额、付款用户等支付要素，演示由工厂依据渠道标识创建对应支付处理器并执行支付，未知渠道由工厂抛出业务异常返回错误";

    /**
     * 贡献工厂方法模式的演示入口条目。
     *
     * <p>仅包含按渠道支付这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 工厂方法模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
