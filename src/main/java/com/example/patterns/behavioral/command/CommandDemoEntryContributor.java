package com.example.patterns.behavioral.command;

import com.example.patterns.common.demo.DemoEntry;
import com.example.patterns.common.demo.DemoEntryContributor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令模式演示入口贡献者。
 *
 * <p>实现 {@link DemoEntryContributor}，仅贡献命令模式自身的演示入口条目，由演示入口注册表统一
 * 聚合后输出到启动清单。遵循开闭原则：命令模式的演示入口变更内聚在本类，无需改动聚合逻辑。</p>
 *
 * <p>共贡献三个 HTTP 演示入口：执行命令、撤销命令、删除商品（刻意以 GET 暴露并叠加确认标识与
 * 权限校验）。</p>
 *
 * @since 1.0.0
 */
@Component
public class CommandDemoEntryContributor implements DemoEntryContributor {

    /**
     * 模式所属类别：行为型。
     */
    private static final String CATEGORY = "行为型";

    /**
     * 设计模式名称。
     */
    private static final String PATTERN_NAME = "命令 Command";

    /**
     * 贡献命令模式的演示入口条目。
     *
     * <p>包含执行命令、撤销命令、删除商品三个 HTTP 演示入口，返回不可变列表，避免被外部修改。</p>
     *
     * @return 命令模式的演示入口条目列表
     */
    @Override
    public List<DemoEntry> contribute() {
        List<DemoEntry> entries = new ArrayList<>(3);
        entries.add(DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, "POST /pattern/command/execute",
                "按命令类型（ADD/UPDATE/DELETE）构造并执行商品操作命令，执行后向命令历史新增一条记录"));
        entries.add(DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, "POST /pattern/command/undo",
                "撤销最近一次已执行的命令，逆向恢复受影响数据并将命令历史标记为已撤销"));
        entries.add(DemoEntry.ofHttp(PATTERN_NAME, CATEGORY, "GET /pattern/command/deleteProduct",
                "删除商品演示：刻意以 GET 暴露，需携带必填确认标识 confirmDelete=true 且当前用户具备 pattern:product:remove 权限方可执行"));
        return Collections.unmodifiableList(entries);
    }
}
