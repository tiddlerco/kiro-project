package com.example.patterns.common.demo;

import lombok.Data;

/**
 * 演示入口条目模型。
 *
 * <p>描述单个设计模式的一个可触发演示入口，是启动清单（满足需求 1.5）的最小数据单元。
 * 每个条目至少携带「设计模式名称 + 触发方式 + 触发入口」，可附带可选的补充描述。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载业务逻辑，使用 Lombok {@code @Data} 生成
 * getter/setter/equals/hashCode/toString。各模式贡献者可借助 {@link #ofHttp} /
 * {@link #ofTest} 静态工厂方法构造条目，避免散落的触发方式魔法字符串。</p>
 *
 * @since 1.0.0
 */
@Data
public class DemoEntry {

    /** 触发方式取值：HTTP 接口 */
    public static final String TRIGGER_TYPE_HTTP = "HTTP";

    /** 触发方式取值：单元测试 */
    public static final String TRIGGER_TYPE_TEST = "TEST";

    /** 所属设计模式名称（如「策略模式 Strategy」） */
    private String patternName;

    /** 模式所属类别：创建型 / 结构型 / 行为型 */
    private String category;

    /** 触发方式：HTTP 或 TEST，取值参见本类 {@code TRIGGER_TYPE_*} 常量 */
    private String triggerType;

    /** 触发入口：HTTP 接口路径（如 {@code POST /pattern/strategy/calculate}）或单元测试标识 */
    private String endpoint;

    /** 入口补充描述（可选，允许为空） */
    private String description;

    /**
     * 构造一个「HTTP 接口」触发方式的演示入口条目。
     *
     * @param patternName 所属设计模式名称
     * @param category    模式所属类别（创建型 / 结构型 / 行为型）
     * @param endpoint    HTTP 接口路径
     * @param description 入口补充描述（可选，允许为空）
     * @return 触发方式为 HTTP 的演示入口条目
     */
    public static DemoEntry ofHttp(String patternName, String category, String endpoint, String description) {
        return build(patternName, category, TRIGGER_TYPE_HTTP, endpoint, description);
    }

    /**
     * 构造一个「单元测试」触发方式的演示入口条目。
     *
     * @param patternName 所属设计模式名称
     * @param category    模式所属类别（创建型 / 结构型 / 行为型）
     * @param endpoint    单元测试标识（如测试类全限定名或测试方法名）
     * @param description 入口补充描述（可选，允许为空）
     * @return 触发方式为 TEST 的演示入口条目
     */
    public static DemoEntry ofTest(String patternName, String category, String endpoint, String description) {
        return build(patternName, category, TRIGGER_TYPE_TEST, endpoint, description);
    }

    /**
     * 以全部字段构造演示入口条目（供静态工厂方法复用的内部构造逻辑）。
     *
     * @param patternName 所属设计模式名称
     * @param category    模式所属类别
     * @param triggerType 触发方式（HTTP 或 TEST）
     * @param endpoint    触发入口（HTTP 路径或单元测试标识）
     * @param description 入口补充描述（可选，允许为空）
     * @return 各字段已赋值的演示入口条目
     */
    private static DemoEntry build(String patternName, String category, String triggerType,
                                   String endpoint, String description) {
        DemoEntry entry = new DemoEntry();
        entry.setPatternName(patternName);
        entry.setCategory(category);
        entry.setTriggerType(triggerType);
        entry.setEndpoint(endpoint);
        entry.setDescription(description);
        return entry;
    }
}
