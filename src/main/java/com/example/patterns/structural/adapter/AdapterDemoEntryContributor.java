package com.example.patterns.structural.adapter;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 适配器模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献适配器模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：适配器模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class AdapterDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：结构型。
     */
    private static final String CATEGORY = "结构型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "适配器 Adapter";

    /**
     * 统一短信发送演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/adapter/sendSms";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交服务商标识、手机号与短信内容，演示以统一短信接口对接阿里云、腾讯云等签名互不相同的第三方 SDK，按服务商标识路由到对应适配器完成发送";

    /**
     * 贡献适配器模式的演示入口条目。
     *
     * <p>仅包含统一短信发送这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 适配器模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
