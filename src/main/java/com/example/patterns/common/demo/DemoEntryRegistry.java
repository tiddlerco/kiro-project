package com.example.patterns.common.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 演示入口注册表。
 *
 * <p>通过 Spring 按类型注入容器内全部 {@link DemoEntryContributor} 贡献者 Bean，
 * 聚合各贡献者提供的演示入口条目，对外提供只读的聚合清单（满足需求 1.5 的集中清单来源）。</p>
 *
 * <p>当容器中尚无任何贡献者实现时，注入为空列表，聚合结果为空列表，不抛出异常，
 * 以容忍「基础设施先行、各模式贡献者后续陆续就位」的增量开发节奏。</p>
 *
 * @since 1.0.0
 */
@Component
public class DemoEntryRegistry {

    /**
     * 容器内全部演示入口贡献者。
     *
     * <p>按类型注入：Spring 会收集所有 {@link DemoEntryContributor} 类型的 Bean 形成列表；
     * 当不存在任何实现时注入空列表，故使用前需判空。</p>
     */
    @Autowired(required = false)
    private List<DemoEntryContributor> demoEntryContributors = new ArrayList<>();

    /**
     * 聚合容器内全部贡献者提供的演示入口条目。
     *
     * @return 不可修改的演示入口聚合清单；无任何条目时返回空列表而非 {@code null}
     */
    public List<DemoEntry> getAllEntries() {
        if (CollectionUtils.isEmpty(demoEntryContributors)) {
            return Collections.emptyList();
        }
        List<DemoEntry> allEntries = new ArrayList<>();
        for (DemoEntryContributor contributor : demoEntryContributors) {
            collectFrom(contributor, allEntries);
        }
        return Collections.unmodifiableList(allEntries);
    }

    /**
     * 从单个贡献者收集演示入口条目并汇入聚合清单。
     *
     * <p>对贡献者返回的空清单做容错（直接跳过），避免空集合污染聚合结果。</p>
     *
     * @param contributor 演示入口贡献者
     * @param target      聚合清单（收集目标）
     */
    private void collectFrom(DemoEntryContributor contributor, List<DemoEntry> target) {
        List<DemoEntry> entries = contributor.contribute();
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }
        target.addAll(entries);
    }
}
