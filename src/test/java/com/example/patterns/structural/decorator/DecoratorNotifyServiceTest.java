package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyCapability;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;
import com.example.patterns.structural.decorator.service.DecoratorNotifyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 装饰器模式核心行为单元测试（对应任务 11.3，验证需求 11.4 / 3.3）。
 *
 * <p>验证 {@link DecoratorNotifyService} 按指定能力列表组装装饰链发送通知后：
 * 发送结果的「已应用能力列表（appliedCapabilities）」包含对应能力，且最终正文
 * （finalContent）体现各装饰器的增强效果（签名追加、加密 Base64 编码）。覆盖单一能力、
 * 多能力组合、空能力三类正常路径。</p>
 *
 * <p>采用「手动装配」方式构造被测服务：直接 new 出 {@link BaseNotifySender} 作为装饰链最内层
 * 内核，经 {@link ReflectionTestUtils} 注入服务，无需启动 Spring 上下文，简洁可靠。</p>
 *
 * @since 1.0.0
 */
class DecoratorNotifyServiceTest {

    /**
     * 构造一个已注入基础发送器的装饰器通知组装服务。
     *
     * <p>不启动 Spring 上下文，直接 new 基础组件并反射注入，作为各用例的统一被测对象来源。</p>
     *
     * @return 已完成依赖装配、可直接发送的装饰器通知组装服务
     */
    private DecoratorNotifyService newService() {
        DecoratorNotifyService service = new DecoratorNotifyService();
        ReflectionTestUtils.setField(service, "baseNotifySender", new BaseNotifySender());
        return service;
    }

    /**
     * 构造一条用于测试的通知内容。
     *
     * @param body 通知正文
     * @return 接收人、标题固定、正文为入参的通知内容
     */
    private NotifyContent buildContent(String body) {
        NotifyContent content = new NotifyContent();
        content.setReceiver("13800000000");
        content.setTitle("订单状态变更提醒");
        content.setBody(body);
        return content;
    }

    /**
     * 场景①：仅叠加「签名」单一能力，结果应标记签名能力且正文末尾追加签名信息。
     */
    @Test
    @DisplayName("仅签名能力_结果标记签名且正文追加签名信息")
    void send_withSignatureOnly_shouldMarkSignatureAndAppendSignature() {
        DecoratorNotifyService service = newService();
        String originalBody = "您的订单已发货";

        SendResult result = service.send(buildContent(originalBody),
                Collections.singletonList(NotifyCapability.SIGNATURE));

        assertNotNull(result);
        assertTrue(result.isSuccess(), "发送应成功");
        // 已应用能力恰为签名一项
        assertEquals(Collections.singletonList(NotifyCapability.SIGNATURE), result.getAppliedCapabilities());
        // 最终正文以原始正文开头，并在其后追加可观察的签名标记
        assertTrue(result.getFinalContent().startsWith(originalBody), "最终正文应保留原始正文");
        assertTrue(result.getFinalContent().contains("【签名】"), "最终正文应体现签名追加效果");
    }

    /**
     * 场景②：叠加「签名+加密+日志」多能力组合，结果应包含全部三项能力，
     * 且正文体现加密（Base64 编码）与签名（追加签名信息）双重增强效果。
     *
     * <p>按服务的组装规则：能力列表 [SIGNATURE, ENCRYPT, LOG] 由内到外包装为
     * Log(Encrypt(Signature(Base))). 执行时加密装饰器先对原始正文做 Base64 编码，签名装饰器
     * 再在该编码串之后追加签名，故最终正文以「原始正文的 Base64」开头并包含签名标记。</p>
     */
    @Test
    @DisplayName("签名加密日志组合_结果含全部能力且正文体现加密与签名")
    void send_withSignatureEncryptLog_shouldApplyAllCapabilitiesAndEnhanceContent() {
        DecoratorNotifyService service = newService();
        String originalBody = "您的账户存在风险，请及时处理";
        String expectedBase64 = Base64.getEncoder()
                .encodeToString(originalBody.getBytes(StandardCharsets.UTF_8));

        SendResult result = service.send(buildContent(originalBody),
                Arrays.asList(NotifyCapability.SIGNATURE, NotifyCapability.ENCRYPT, NotifyCapability.LOG));

        assertNotNull(result);
        assertTrue(result.isSuccess(), "发送应成功");
        // 已应用能力包含且仅包含签名、加密、日志三项
        assertEquals(3, result.getAppliedCapabilities().size(), "应叠加三项能力且不重复");
        assertTrue(result.getAppliedCapabilities().contains(NotifyCapability.SIGNATURE), "应含签名能力");
        assertTrue(result.getAppliedCapabilities().contains(NotifyCapability.ENCRYPT), "应含加密能力");
        assertTrue(result.getAppliedCapabilities().contains(NotifyCapability.LOG), "应含日志能力");
        // 加密效果：最终正文以原始正文的 Base64 编码开头
        assertTrue(result.getFinalContent().startsWith(expectedBase64), "最终正文应体现 Base64 加密效果");
        // 签名效果：最终正文包含签名标记
        assertTrue(result.getFinalContent().contains("【签名】"), "最终正文应体现签名追加效果");
    }

    /**
     * 场景③：空能力（仅基础发送），结果应无任何增强能力且正文保持原样。
     */
    @Test
    @DisplayName("空能力_仅基础发送_无增强且正文原样")
    void send_withNoCapability_shouldSendPlainWithoutEnhancement() {
        DecoratorNotifyService service = newService();
        String originalBody = "欢迎注册示例通知中心";

        SendResult result = service.send(buildContent(originalBody), Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isSuccess(), "发送应成功");
        // 未叠加任何能力
        assertTrue(result.getAppliedCapabilities().isEmpty(), "空能力时不应应用任何增强能力");
        // 正文保持原样，无任何增强痕迹
        assertEquals(originalBody, result.getFinalContent(), "空能力时最终正文应与原始正文完全一致");
    }
}
