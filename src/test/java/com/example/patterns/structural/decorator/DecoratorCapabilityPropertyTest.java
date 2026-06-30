package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyCapability;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;
import com.example.patterns.structural.decorator.service.DecoratorNotifyService;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 装饰器能力任意叠加属性测试（对应任务 11.4，Property 8，验证需求 3.3）。
 *
 * <p>随机生成「装饰能力子集（签名/加密/日志的任意子集）」与「叠加顺序」，断言最终装饰链发送
 * 结果体现所有已叠加能力：已应用能力列表与所选能力集合完全一致（不重、不漏、不多）。</p>
 *
 * <p>采用「手动装配」方式构造被测服务，不启动 Spring 上下文，作为本工程首个属性测试以最小
 * 依赖确保 jqwik 引擎可被 Surefire 正常发现并运行。</p>
 *
 * @since 1.0.0
 */
class DecoratorCapabilityPropertyTest {

    /**
     * 构造一个已注入基础发送器的装饰器通知组装服务。
     *
     * <p>不启动 Spring 上下文，直接 new 基础组件并反射注入，供属性测试每次迭代独立使用。</p>
     *
     * @return 已完成依赖装配、可直接发送的装饰器通知组装服务
     */
    private DecoratorNotifyService newService() {
        DecoratorNotifyService service = new DecoratorNotifyService();
        ReflectionTestUtils.setField(service, "baseNotifySender", new BaseNotifySender());
        return service;
    }

    /**
     * 生成「随机能力子集 + 随机叠加顺序」的能力列表。
     *
     * <p>先从全部能力中取任意子集（含空集），再对子集元素做随机洗牌以模拟任意叠加顺序，
     * 从而覆盖「任意子集 × 任意顺序」的输入空间。</p>
     *
     * @return 产出无重复元素、顺序随机的能力列表的 Arbitrary
     */
    @Provide
    Arbitrary<List<NotifyCapability>> capabilitySubsets() {
        return Arbitraries.subsetOf(NotifyCapability.values())
                .flatMap(subset -> Arbitraries.shuffle(new ArrayList<>(subset)));
    }

    // Feature: design-patterns-showcase, Property 8: 装饰器能力任意叠加
    /**
     * Property 8：装饰器能力任意叠加。
     *
     * <p><b>Validates: Requirements 3.3</b></p>
     *
     * <p>对任意装饰能力子集与任意叠加顺序，断言：发送成功，且发送结果的已应用能力列表与所选
     * 能力集合一致——数量相等（不重复）、且作为集合完全相同（不漏、不多）。</p>
     *
     * @param capabilities 随机生成的能力子集（顺序随机，可能为空）
     */
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    void appliedCapabilitiesMatchSelectedSubset(@ForAll("capabilitySubsets") List<NotifyCapability> capabilities) {
        DecoratorNotifyService service = newService();
        NotifyContent content = new NotifyContent();
        content.setReceiver("13900000000");
        content.setTitle("属性测试通知");
        content.setBody("属性测试正文内容");

        SendResult result = service.send(content, capabilities);

        // 装饰链发送应始终成功
        assertThat(result.isSuccess()).isTrue();
        // 不重复：已应用能力数量等于所选能力数量
        assertThat(result.getAppliedCapabilities()).hasSize(capabilities.size());
        // 不漏不多：已应用能力集合与所选能力集合完全一致
        Set<NotifyCapability> applied = result.getAppliedCapabilities().isEmpty()
                ? EnumSet.noneOf(NotifyCapability.class)
                : EnumSet.copyOf(result.getAppliedCapabilities());
        assertThat(applied).isEqualTo(new HashSet<>(capabilities));
    }
}
