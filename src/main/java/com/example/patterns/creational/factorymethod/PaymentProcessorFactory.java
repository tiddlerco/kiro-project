package com.example.patterns.creational.factorymethod;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付处理器工厂。
 *
 * <p>工厂方法模式中的「工厂（Factory）」角色，负责依据传入的支付渠道标识返回对应的
 * {@link PaymentProcessor} 实现，使调用方无需感知具体处理器类，仅依赖抽象接口与渠道标识。</p>
 *
 * <p>与 Spring 的结合方式：本工厂以 {@link List} 形式注入容器中全部 {@link PaymentProcessor}
 * 实现，并在初始化阶段基于各实现自身声明的 {@link PaymentProcessor#channel()} 建立
 * 「渠道标识 → 处理器」路由表。相较于直接注入以 bean 名为键的 {@code Map<String,PaymentProcessor>}，
 * 该方式不依赖 bean 命名约定，渠道标识完全由实现类自治声明，新增渠道只需新增实现类即可
 * 自动接入（满足开闭原则，对应需求 10.4）。</p>
 *
 * @since 1.0.0
 */
@Component
public class PaymentProcessorFactory {

    /**
     * 容器中注入的全部支付处理器实现。
     *
     * <p>以 {@link List} 注入而非按 bean 名注入 {@code Map}，便于在初始化阶段基于各实现
     * 自身声明的渠道标识建立路由表，避免对 bean 名称的隐式依赖。</p>
     */
    @Resource
    private List<PaymentProcessor> paymentProcessors;

    /**
     * 「渠道标识 → 支付处理器」路由表。
     *
     * <p>在容器完成依赖注入后由 {@link #initProcessorMap()} 一次性构建，运行期只读。</p>
     */
    private final Map<String, PaymentProcessor> processorMap = new HashMap<>();

    /**
     * 在依赖注入完成后构建「渠道标识 → 处理器」路由表。
     *
     * <p>遍历全部注入的支付处理器，以各自 {@link PaymentProcessor#channel()} 为键登记入表。
     * 若出现重复的渠道标识，说明存在配置冲突（同一渠道有多个实现），此处快速失败抛出
     * {@link IllegalStateException}，将问题暴露在应用启动阶段而非运行期。</p>
     */
    @PostConstruct
    public void initProcessorMap() {
        for (PaymentProcessor processor : paymentProcessors) {
            String channel = processor.channel();
            PaymentProcessor existing = processorMap.put(channel, processor);
            if (existing != null) {
                throw new IllegalStateException("存在重复的支付渠道标识：" + channel);
            }
        }
    }

    /**
     * 按渠道标识创建（获取）对应的支付处理器。
     *
     * <p>当渠道标识为空或不受支持时，抛出 {@link ServiceException}，并且不创建任何处理器实例
     * （处理器均为容器预先管理的单例，本方法仅做路由查找，对应需求 2.4、10.5）。</p>
     *
     * @param channel 支付渠道标识，如 {@code "wechat"}、{@code "alipay"}
     * @return 与渠道标识匹配的支付处理器实例
     */
    public PaymentProcessor create(String channel) {
        if (!StringUtils.hasText(channel)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "支付渠道标识不能为空");
        }
        PaymentProcessor processor = processorMap.get(channel);
        if (processor == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "不支持的支付渠道：" + channel);
        }
        return processor;
    }
}
