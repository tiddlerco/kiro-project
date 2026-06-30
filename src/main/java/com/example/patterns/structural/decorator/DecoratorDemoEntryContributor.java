package com.example.patterns.structural.decorator;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 装饰器模式演示入口贡献者。
 *
 * <p>采用「各模式自贡献独立 Bean + 注册表聚合」机制，仅贡献装饰器模式自身的演示入口条目，
 * 由演示入口注册表统一聚合，避免与其他模式争抢同一集中清单文件并遵循开闭原则。</p>
 *
 * @since 1.0.0
 */
@Component
public class DecoratorDemoEntryContributor implements DemoEntryContributor {

    /**
     * 贡献装饰器模式的演示入口条目。
     *
     * <p>返回一个 HTTP 触发方式的入口，指向按能力列表叠加签名、加密、日志后发送通知的接口。</p>
     *
     * @return 仅含装饰器模式 HTTP 演示入口的条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        DemoEntry entry = DemoEntry.ofHttp(
                "装饰器 Decorator",
                "结构型",
                "POST /pattern/decorator/send",
                "按 capabilities 指定的能力列表（SIGNATURE/ENCRYPT/LOG）逐层叠加增强后发送通知");
        return Collections.singletonList(entry);
    }
}
