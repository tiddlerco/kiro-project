package com.example.patterns.creational.builder;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.creational.builder.domain.NotificationAttachment;
import com.example.patterns.creational.builder.domain.NotificationPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 建造者核心行为单元测试（对应任务 6.3，验证需求 11.4 与需求 2.6）。
 *
 * <p>覆盖建造者模式产品 {@link NotificationMessage} 的三条核心行为：</p>
 * <ul>
 *     <li>仅设置必选部件「接收人」时，可选部件保持默认（标题/正文为 {@code null}、附件为空列表、优先级为 {@code null}）；</li>
 *     <li>分步设置全部部件（含多个附件）时，各字段与设置值一致且附件顺序与追加顺序一致；</li>
 *     <li>未设置必选部件「接收人」直接构建时，拒绝构建并抛出 {@link ServiceException}（边界路径）。</li>
 * </ul>
 *
 * @since 1.0.0
 */
class NotificationMessageBuilderTest {

    /**
     * 演示用接收人标识（手机号形态）。
     */
    private static final String RECEIVER = "13800000000";

    /**
     * 验证仅设置必选部件「接收人」时构建成功，且全部可选部件保持默认值。
     *
     * <p>断言接收人为设置值，标题与正文为 {@code null}，附件为空列表（而非 {@code null}），
     * 优先级为 {@code null}。</p>
     */
    @Test
    @DisplayName("仅设置必选接收人时构建成功且可选部件为默认值")
    void shouldBuildWithDefaultsWhenOnlyReceiverSet() {
        NotificationMessage message = NotificationMessage.builder()
                .to(RECEIVER)
                .build();

        assertThat(message.getReceiver()).isEqualTo(RECEIVER);
        assertThat(message.getTitle()).isNull();
        assertThat(message.getContent()).isNull();
        assertThat(message.getAttachments()).isEmpty();
        assertThat(message.getPriority()).isNull();
    }

    /**
     * 验证分步设置全部部件（含多个附件）后，各字段与设置值一致且附件顺序保持一致。
     *
     * <p>依次设置接收人、标题、正文、两个附件与优先级，断言构建对象各字段与设置值相等，
     * 且附件列表按追加顺序恰好为 [第一个附件, 第二个附件]。</p>
     */
    @Test
    @DisplayName("设置全部部件时各字段与附件顺序均与设置值一致")
    void shouldBuildWithAllPartsAndPreserveAttachmentOrder() {
        NotificationAttachment firstAttachment = NotificationAttachment.of("对账单_202406.pdf",
                "https://oss.example.com/bill_202406.pdf", 1024L);
        NotificationAttachment secondAttachment = NotificationAttachment.of("营销海报.png",
                "https://oss.example.com/poster.png", 2048L);

        NotificationMessage message = NotificationMessage.builder()
                .to(RECEIVER)
                .title("对账提醒")
                .content("您的六月对账单已生成，请查收附件")
                .attach(firstAttachment)
                .attach(secondAttachment)
                .priority(NotificationPriority.HIGH)
                .build();

        assertThat(message.getReceiver()).isEqualTo(RECEIVER);
        assertThat(message.getTitle()).isEqualTo("对账提醒");
        assertThat(message.getContent()).isEqualTo("您的六月对账单已生成，请查收附件");
        assertThat(message.getPriority()).isEqualTo(NotificationPriority.HIGH);
        assertThat(message.getAttachments()).containsExactly(firstAttachment, secondAttachment);
    }

    /**
     * 验证未设置必选部件「接收人」直接构建时抛出 {@link ServiceException}（边界路径）。
     *
     * <p>仅设置可选部件而缺失接收人时调用 {@code build()}，断言抛出业务异常且异常信息含「接收人」提示。</p>
     */
    @Test
    @DisplayName("未设置接收人构建时抛出 ServiceException")
    void shouldThrowServiceExceptionWhenReceiverMissing() {
        assertThatThrownBy(() -> NotificationMessage.builder()
                .title("缺少接收人的通知")
                .content("正文内容")
                .build())
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("接收人");
    }
}
