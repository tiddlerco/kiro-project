package com.example.patterns.common.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示入口启动清单打印器。
 *
 * <p>监听 {@link ApplicationReadyEvent}，在应用完全就绪后从 {@link DemoEntryRegistry}
 * 获取聚合的演示入口清单，并以 SLF4J 日志按模式类别分组输出，每条至少包含
 * 「设计模式名称 + 触发方式 + 触发入口」（满足需求 1.5）。</p>
 *
 * @since 1.0.0
 */
@Component
public class DemoEntryPrinter {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(DemoEntryPrinter.class);

    /** 类别展示顺序（创建型 → 结构型 → 行为型）；不在此列的类别按出现顺序追加其后 */
    private static final List<String> CATEGORY_ORDER = Arrays.asList("创建型", "结构型", "行为型");

    /** 类别为空时的兜底归类名称 */
    private static final String UNCATEGORIZED = "未分类";

    /** 演示入口注册表 */
    @Resource
    private DemoEntryRegistry demoEntryRegistry;

    /**
     * 应用就绪后打印演示入口清单。
     *
     * @param event 应用就绪事件
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        List<DemoEntry> entries = demoEntryRegistry.getAllEntries();
        if (CollectionUtils.isEmpty(entries)) {
            log.info("设计模式演示入口清单：当前尚未注册任何演示入口（等待各模式贡献者就位）。");
            return;
        }
        printEntries(entries);
    }

    /**
     * 按类别分组格式化打印演示入口清单。
     *
     * @param entries 演示入口聚合清单（非空）
     */
    private void printEntries(List<DemoEntry> entries) {
        Map<String, List<DemoEntry>> grouped = groupByCategory(entries);
        log.info("==================== 设计模式演示入口清单（共 {} 条） ====================", entries.size());
        for (Map.Entry<String, List<DemoEntry>> categoryGroup : grouped.entrySet()) {
            printCategoryGroup(categoryGroup.getKey(), categoryGroup.getValue());
        }
        log.info("=======================================================================");
    }

    /**
     * 打印单个类别下的全部演示入口。
     *
     * @param category        模式类别名称
     * @param categoryEntries 该类别下的演示入口列表
     */
    private void printCategoryGroup(String category, List<DemoEntry> categoryEntries) {
        log.info("【{}】（{} 条）", category, categoryEntries.size());
        for (DemoEntry entry : categoryEntries) {
            log.info("    - {} | 触发方式: {} | 入口: {}{}",
                    entry.getPatternName(),
                    entry.getTriggerType(),
                    entry.getEndpoint(),
                    formatDescription(entry.getDescription()));
        }
    }

    /**
     * 将演示入口按类别分组，并按期望顺序（创建型 → 结构型 → 行为型 → 其它）排列。
     *
     * @param entries 演示入口聚合清单
     * @return 维持类别展示顺序的分组结果
     */
    private Map<String, List<DemoEntry>> groupByCategory(List<DemoEntry> entries) {
        Map<String, List<DemoEntry>> grouped = new LinkedHashMap<>();
        for (DemoEntry entry : entries) {
            String category = resolveCategory(entry.getCategory());
            grouped.computeIfAbsent(category, key -> new ArrayList<>()).add(entry);
        }
        return applyPreferredOrder(grouped);
    }

    /**
     * 将分组结果按预定义类别顺序重排，未预定义的类别保持原出现顺序追加其后。
     *
     * @param grouped 按出现顺序分组的原始结果
     * @return 按期望类别顺序重排后的分组结果
     */
    private Map<String, List<DemoEntry>> applyPreferredOrder(Map<String, List<DemoEntry>> grouped) {
        Map<String, List<DemoEntry>> ordered = new LinkedHashMap<>();
        for (String category : CATEGORY_ORDER) {
            List<DemoEntry> categoryEntries = grouped.get(category);
            if (!CollectionUtils.isEmpty(categoryEntries)) {
                ordered.put(category, categoryEntries);
            }
        }
        for (Map.Entry<String, List<DemoEntry>> categoryGroup : grouped.entrySet()) {
            ordered.putIfAbsent(categoryGroup.getKey(), categoryGroup.getValue());
        }
        return ordered;
    }

    /**
     * 解析演示入口的类别名称，空白类别归入兜底类别。
     *
     * @param category 原始类别名称（可能为空）
     * @return 规范化后的类别名称
     */
    private String resolveCategory(String category) {
        return StringUtils.hasText(category) ? category : UNCATEGORIZED;
    }

    /**
     * 将可选的入口描述格式化为日志后缀。
     *
     * @param description 入口补充描述（可能为空）
     * @return 形如「 (描述)」的后缀；无描述时返回空字符串
     */
    private String formatDescription(String description) {
        return StringUtils.hasText(description) ? "  (" + description + ")" : "";
    }
}
