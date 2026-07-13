package com.example.patterns.creational.builder;

import com.example.patterns.creational.builder.domain.NotificationAttachment;
import com.example.patterns.creational.builder.domain.NotificationPriority;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 建造者构建保真属性测试（对应任务 6.4，Property 4，验证需求 2.6）。
 *
 * <p>随机决定各可选部件（标题/正文/附件/优先级）是否设置及其取值，必选部件「接收人」恒被设置。
 * 断言构建出的 {@link NotificationMessage} 恰好包含已设置部件的取值：已设置部件与设置值一致
 * （附件保持追加顺序），未设置部件保持默认（标题/正文/优先级为 {@code null}、附件为空列表），
 * 从而验证「分步设置后构建出恰好包含已设置部件的完整对象」这一建造者保真性质。</p>
 *
 * @since 1.0.0
 */
class NotificationBuilderFidelityPropertyTest {

    /**
     * 生成必选部件「接收人」：恒为非空白文本，以确保构建校验必然通过。
     *
     * @return 产出非空白接收人标识的 Arbitrary
     */
    @Provide
    Arbitrary<String> receivers() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
    }

    /**
     * 生成可选文本部件（标题或正文）：以一定概率注入 {@code null} 表示「未设置」。
     *
     * @return 产出可能为 {@code null} 的可选文本的 Arbitrary
     */
    @Provide
    Arbitrary<String> optionalTexts() {
        return Arbitraries.strings().ofMaxLength(30).injectNull(0.3);
    }

    /**
     * 生成单个通知附件：由随机文件名、地址与大小组合而成。
     *
     * @return 产出通知附件值对象的 Arbitrary
     */
    @Provide
    Arbitrary<NotificationAttachment> attachments() {
        Arbitrary<String> fileNames = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> urls = Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
        Arbitrary<Long> sizes = Arbitraries.longs().between(0L, 1_048_576L);
        return Combinators.combine(fileNames, urls, sizes)
                .as((fileName, url, size) -> NotificationAttachment.of(fileName + ".pdf", "https://oss.example.com/" + url, size));
    }

    /**
     * 生成附件列表：长度 0~4 的随机列表（空列表表示「未设置附件」），元素均非空。
     *
     * @return 产出附件列表的 Arbitrary
     */
    @Provide
    Arbitrary<List<NotificationAttachment>> attachmentLists() {
        return attachments().list().ofMinSize(0).ofMaxSize(4);
    }

    /**
     * 生成可选优先级部件：以一定概率注入 {@code null} 表示「未设置」。
     *
     * @return 产出可能为 {@code null} 的优先级枚举的 Arbitrary
     */
    @Provide
    Arbitrary<NotificationPriority> optionalPriorities() {
        return Arbitraries.of(NotificationPriority.values()).injectNull(0.3);
    }

    // Feature: design-patterns-showcase, Property 4: 建造者构建保真
    /**
     * Property 4：建造者构建保真。
     *
     * <p><b>Validates: Requirements 2.6</b></p>
     *
     * <p>对任意「必选接收人 + 可选部件是否设置及取值」的随机组合，仅在可选部件取值非 {@code null}
     * （附件列表逐个追加）时调用对应设置方法以忠实模拟「是否设置」，随后构建并断言：接收人与设置值
     * 一致；已设置的标题/正文/优先级与设置值一致、未设置者为 {@code null}；附件列表按追加顺序与
     * 所设置附件完全一致、未设置附件时为空列表。</p>
     *
     * <p>说明：jqwik 引擎禁止 {@code @Property} 方法携带 JUnit 的 {@code @DisplayName} 注解，
     * 故此处改用 jqwik 提供的等价注解 {@link Label} 标注中文描述。</p>
     *
     * @param receiver    随机生成的必选接收人（恒被设置）
     * @param title       随机生成的标题，{@code null} 表示未设置
     * @param content     随机生成的正文，{@code null} 表示未设置
     * @param attachments 随机生成的附件列表，空列表表示未设置附件
     * @param priority    随机生成的优先级，{@code null} 表示未设置
     */
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    @Label("构建对象恰好包含已设置部件的取值")
    void buildResultExactlyReflectsSetParts(@ForAll("receivers") String receiver,
                                            @ForAll("optionalTexts") String title,
                                            @ForAll("optionalTexts") String content,
                                            @ForAll("attachmentLists") List<NotificationAttachment> attachments,
                                            @ForAll("optionalPriorities") NotificationPriority priority) {
        NotificationMessage message = buildMessage(receiver, title, content, attachments, priority);

        assertThat(message.getReceiver()).isEqualTo(receiver);
        assertThat(message.getTitle()).isEqualTo(title);
        assertThat(message.getContent()).isEqualTo(content);
        assertThat(message.getPriority()).isEqualTo(priority);
        assertThat(message.getAttachments()).containsExactlyElementsOf(attachments);
    }

    /**
     * 依据「可选部件是否为 null」决定是否调用对应设置方法，忠实模拟部件的「设置 / 未设置」。
     *
     * <p>必选接收人恒被设置；标题、正文、优先级仅在非 {@code null} 时设置；附件按列表顺序逐个追加。</p>
     *
     * @param receiver    必选接收人（恒被设置）
     * @param title       标题，{@code null} 时不设置
     * @param content     正文，{@code null} 时不设置
     * @param attachments 附件列表，逐个追加（空列表则不追加任何附件）
     * @param priority    优先级，{@code null} 时不设置
     * @return 依据上述设置构建出的通知消息
     */
    private NotificationMessage buildMessage(String receiver, String title, String content,
                                             List<NotificationAttachment> attachments, NotificationPriority priority) {
        NotificationMessageBuilder builder = NotificationMessage.builder().to(receiver);
        if (title != null) {
            builder.title(title);
        }
        if (content != null) {
            builder.content(content);
        }
        for (NotificationAttachment attachment : attachments) {
            builder.attach(attachment);
        }
        if (priority != null) {
            builder.priority(priority);
        }
        return builder.build();
    }
}
