package com.example.patterns.creational.abstractfactory;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 抽象工厂模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献抽象工厂模式自身的演示入口条目，由演示入口注册表
 * 统一聚合后输出到启动清单。遵循开闭原则：抽象工厂模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * @since 1.0.0
 */
@Component
public class AbstractFactoryDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：创建型。
     */
    private static final String CATEGORY = "创建型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "抽象工厂 Abstract Factory";

    /**
     * 云存储上传演示接口路径。
     */
    private static final String ENDPOINT = "POST /pattern/abstractfactory/upload";

    /**
     * 演示入口补充描述。
     */
    private static final String DESCRIPTION = "提交云厂商标识、对象键、文本内容与签名有效期，演示按厂商选取具体工厂后，由同族存储客户端与 URL 签名器协作完成上传并生成签名访问 URL，所得产品始终相互配套、归属一致";

    /**
     * 贡献抽象工厂模式的演示入口条目。
     *
     * <p>仅包含云存储上传这一个 HTTP 演示入口，返回不可变的单元素列表，避免被外部修改。</p>
     *
     * @return 抽象工厂模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, ENDPOINT, DESCRIPTION);
        return Collections.singletonList(entry);
    }
}
